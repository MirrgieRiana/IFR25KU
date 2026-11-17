package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW
import org.mockito.Mockito

private class SimpleScrollTestView(size: IntPoint) : FixedWidgetView(size)

class VanillaSimpleScrollViewWidgetTest {
    @Test
    fun `バーを削除した領域でも子のクリックとスクロールと破棄を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                view += SimpleScrollTestView(IntPoint(300, 300))
                view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
                val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
                Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
                val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
                lateinit var child: AbstractWidget
                var disposed = false
                var releasedInput: Triple<Double, Double, Int>? = null
                registry.register { childContext, _: SimpleScrollTestView, bounds ->
                    child = object : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
                        override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
                        override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
                        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = button == 0 && isMouseOver(mouseX, mouseY)
                        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
                            releasedInput = Triple(mouseX, mouseY, button)
                            return false
                        }
                    }
                    val remover = childContext.addRenderableWidget(child)
                    object : ViewController {
                        override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                        override fun detach() = remover.remove()
                        override fun dispose() {
                            disposed = true
                        }
                    }
                }
                val bounds = IntRectangle(10, 20, 100, 100)
                val widget = if (horizontal) VanillaXSimpleScrollViewWidget(view as XSimpleScrollView, bounds, context, registry) else VanillaYSimpleScrollViewWidget(view as YSimpleScrollView, bounds, context, registry)
                Assertions.assertTrue(widget.mouseClicked(109.0, 119.0, 0))
                Assertions.assertSame(child, widget.focused)
                Assertions.assertNull(releasedInput)
                Assertions.assertTrue(widget.mouseReleased(109.0, 119.0, 0))
                Assertions.assertEquals(Triple(109.0, 119.0, 0), releasedInput)
                Assertions.assertTrue(widget.mouseScrolled(109.0, 119.0, 0.0, -1.0))
                Assertions.assertEquals(if (horizontal) IntPoint(-5, 20) else IntPoint(10, 5), IntPoint(child.x, child.y))
                widget.render(environment.graphics, 109, 119, 0.0F)
                Assertions.assertTrue(environment.fills.isEmpty())
                widget.dispose()
                Assertions.assertTrue(disposed)
                Assertions.assertFalse(widget.mouseClicked(109.0, 119.0, 0))
            }
        }
    }

    @Test
    fun `表示領域が広がったあとは範囲外の位置から少しずつ戻れるのだ`() {
        listOf(false, true).forEach { horizontal ->
            val offsets = mutableListOf<IntPoint>()
            val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
            view += object : FixedView(IntPoint(300, 300)) {
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
                    return object : ViewController {
                        override fun setOffset(offset: IntPoint) {
                            offsets += offset
                        }
                        override fun detach() = Unit
                        override fun dispose() = Unit
                    }
                }
            }
            view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            val bounds = IntRectangle(10, 20, 100, 100)
            val widget = if (horizontal) VanillaXSimpleScrollViewWidget(view as XSimpleScrollView, bounds, context, registry) else VanillaYSimpleScrollViewWidget(view as YSimpleScrollView, bounds, context, registry)
            try {
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                widget.setSize(300, 300)
                Assertions.assertEquals(listOf(if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180)), offsets)
                Assertions.assertFalse(widget.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                Assertions.assertEquals(1, offsets.size)
                Assertions.assertTrue(widget.mouseScrolled(50.0, 50.0, 0.0, 1.0))
                Assertions.assertEquals(if (horizontal) IntPoint(-175, 20) else IntPoint(10, -165), offsets.last())
                val backwardKey = if (horizontal) GLFW.GLFW_KEY_LEFT else GLFW.GLFW_KEY_UP
                Assertions.assertTrue(widget.keyPressed(backwardKey, 0, 0))
                Assertions.assertEquals(if (horizontal) IntPoint(-160, 20) else IntPoint(10, -150), offsets.last())
            } finally {
                widget.dispose()
                widget.detach()
            }
        }
    }

    @Test
    fun `範囲外から逆 Tab で子に移ると子が収まる分だけスクロールするのだ`() {
        GuiTestEnvironment().use {
            val view = YSimpleScrollView()
            view += YListView().also {
                it += YSpaceView(220)
                it += SimpleScrollTestView(IntPoint(20, 20))
                it += YSpaceView(10)
                it += SimpleScrollTestView(IntPoint(20, 20))
                it += YSpaceView(30)
            }
            view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 70 })
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            val children = mutableListOf<AbstractWidget>()
            registry.register { childContext, _: SimpleScrollTestView, bounds ->
                val child: AbstractWidget = object : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
                    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
                    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
                }
                children += child
                val remover = childContext.addRenderableWidget(child)
                object : ViewController {
                    override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                    override fun detach() = remover.remove()
                    override fun dispose() = Unit
                }
            }
            val widget = VanillaYSimpleScrollViewWidget(view, IntRectangle(10, 20, 100, 70), context, registry)
            try {
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                widget.setHeight(100)
                Assertions.assertEquals(listOf(10, 40), children.map { it.y })

                fun focusPrevious() {
                    val path = widget.nextFocusPath(FocusNavigationEvent.TabNavigation(false))
                    Assertions.assertNotNull(path)
                    widget.currentFocusPath?.applyFocus(false)
                    path!!.applyFocus(true)
                }

                focusPrevious()
                Assertions.assertTrue(children[1].isFocused)
                Assertions.assertEquals(listOf(10, 40), children.map { it.y })

                focusPrevious()
                Assertions.assertTrue(children[0].isFocused)
                Assertions.assertEquals(listOf(20, 50), children.map { it.y })
            } finally {
                widget.dispose()
                widget.detach()
            }
        }
    }
}
