package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.ScrollBounds
import miragefairy2024.mod.recipeviewer.views.XScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import miragefairy2024.util.register
import net.minecraft.world.inventory.Slot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

private class BoundsTestView : FixedWidgetView(IntPoint(300, 300))

private class BoundsTestFixture(private val horizontal: Boolean, layoutLength: Int) {
    val view = if (horizontal) XScrollContainerView() else YScrollContainerView()
    private val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
    private val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()

    init {
        view += BoundsTestView()
        view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { layoutLength }, { layoutLength })
        Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
        registry.register { _, _: BoundsTestView, _ -> ViewController.NONE }
    }

    fun createWidget(length: Int): VanillaScrollContainerViewWidget {
        val bounds = if (horizontal) IntRectangle(10, 20, length, 100) else IntRectangle(10, 20, 100, length)
        return if (horizontal) VanillaXScrollContainerViewWidget(view as XScrollContainerView, bounds, context, registry) else VanillaYScrollContainerViewWidget(view as YScrollContainerView, bounds, context, registry)
    }
}

class VanillaScrollContainerBoundsTest {
    @Test
    fun `子のコンテキストから親へ Slot の登録を中継するのだ`() {
        GuiTestEnvironment().use {
            val contexts = mutableListOf<VanillaViewPlacerContext>()
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            registry.register { context, _: BoundsTestView, _ ->
                contexts += context
                ViewController.NONE
            }
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
            val view = YScrollContainerView()
            view += BoundsTestView()
            view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
            val slot: Slot = Slot(null, 0, 0, 0)
            val clipBounds = { IntRectangle(10, 20, 30, 40) }
            val remover = Remover { }
            Mockito.`when`(context.registerMenuSlot(slot, clipBounds)).thenReturn(remover)
            val widget = VanillaYScrollContainerViewWidget(view, IntRectangle(0, 0, 100, 100), context, registry)
            try {
                Assertions.assertFalse(contexts.single().canPlaceMenuSlots)
                Assertions.assertSame(remover, contexts.single().registerMenuSlot(slot, clipBounds))
                Mockito.verify(context).registerMenuSlot(slot, clipBounds)
            } finally {
                widget.dispose()
                widget.detach()
            }
        }
    }

    @Test
    fun `バニラの実際の寸法を生成時と非表示中のサイズ変更時に通知するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use {
                val fixture = BoundsTestFixture(horizontal, 50)
                val view = fixture.view
                var changes = 0
                view.scrollBounds.register { _, _ ->
                    changes++
                }
                val widget = fixture.createWidget(100)
                try {
                    Assertions.assertEquals(ScrollBounds(300, 100), view.scrollBounds.value)
                    widget.visible = false
                    widget.setWidth(200)
                    Assertions.assertEquals(if (horizontal) 200 else 100, view.scrollBounds.value.viewportLength)
                    Assertions.assertEquals(if (horizontal) 2 else 1, changes)
                    widget.setHeight(200)
                    Assertions.assertEquals(200, view.scrollBounds.value.viewportLength)
                    Assertions.assertEquals(2, changes)
                    widget.setSize(250, 250)
                    Assertions.assertEquals(250, view.scrollBounds.value.viewportLength)
                    Assertions.assertEquals(3, changes)
                    widget.setWidth(250)
                    widget.setHeight(250)
                    widget.setSize(250, 250)
                    Assertions.assertEquals(3, changes)
                } finally {
                    widget.dispose()
                    widget.detach()
                }
            }
        }
    }

    @Test
    fun `破棄済み Widget のサイズ変更と描画では再配置後の寸法を上書きしないのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val fixture = BoundsTestFixture(horizontal, 300)
                val view = fixture.view
                val old = fixture.createWidget(100)
                old.dispose()
                val current = fixture.createWidget(200)
                try {
                    Assertions.assertEquals(200, view.scrollBounds.value.viewportLength)
                    old.setWidth(300)
                    Assertions.assertEquals(200, view.scrollBounds.value.viewportLength)
                    old.setHeight(300)
                    Assertions.assertEquals(200, view.scrollBounds.value.viewportLength)
                    old.setSize(400, 400)
                    Assertions.assertEquals(200, view.scrollBounds.value.viewportLength)
                    old.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(200, view.scrollBounds.value.viewportLength)
                } finally {
                    current.dispose()
                    current.detach()
                    old.detach()
                }
            }
        }
    }
}
