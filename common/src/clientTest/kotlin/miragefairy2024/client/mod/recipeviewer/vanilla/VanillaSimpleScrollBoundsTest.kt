package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.ScrollBounds
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.register
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

private class BoundsTestView : FixedWidgetView(IntPoint(300, 300))

private class BoundsTestFixture(private val horizontal: Boolean, layoutLength: Int) {
    val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
    private val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
    private val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()

    init {
        view += BoundsTestView()
        view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { layoutLength }, { layoutLength })
        Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
        registry.register { _, _: BoundsTestView, _ -> ViewController.NONE }
    }

    fun createWidget(length: Int): VanillaSimpleScrollViewWidget {
        val bounds = if (horizontal) IntRectangle(10, 20, length, 100) else IntRectangle(10, 20, 100, length)
        return if (horizontal) VanillaXSimpleScrollViewWidget(view as XSimpleScrollView, bounds, context, registry) else VanillaYSimpleScrollViewWidget(view as YSimpleScrollView, bounds, context, registry)
    }
}

class VanillaSimpleScrollBoundsTest {
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
