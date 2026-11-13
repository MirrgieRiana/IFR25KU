package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.connectScrollbar
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

private class SimpleScrollTestView : FixedWidgetView(IntPoint(300, 300))

class VanillaSimpleScrollViewWidgetTest {
    @Test
    fun `バーを削除した領域でも子のクリックとスクロールと破棄を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                view += SimpleScrollTestView()
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
    fun `範囲の変更をバーへ通知しても描画で指定値と子の位置を変更しないのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                val bar = if (horizontal) XScrollbarView() else YScrollbarView()
                val scrollOffset = if (horizontal) (view as XSimpleScrollView).scrollOffsetX else (view as YSimpleScrollView).scrollOffsetY
                val barOffset = if (horizontal) (bar as XScrollbarView).scrollOffsetX else (bar as YScrollbarView).scrollOffsetY
                val childView: SimpleScrollTestView = Mockito.spy(SimpleScrollTestView())
                view += childView
                scrollOffset.value = 175.25
                val connection = connectScrollbar(view, scrollOffset, bar, barOffset)
                view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
                val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
                Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
                val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
                lateinit var child: AbstractWidget
                registry.register { _, _: SimpleScrollTestView, bounds ->
                    child = Mockito.mock(AbstractWidget::class.java)
                    child.setPosition(bounds.x, bounds.y)
                    object : ViewController {
                        override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                        override fun detach() = Unit
                    }
                }
                val bounds = IntRectangle(10, 20, 100, 100)
                val widget = if (horizontal) VanillaXSimpleScrollViewWidget(view as XSimpleScrollView, bounds, context, registry) else VanillaYSimpleScrollViewWidget(view as YSimpleScrollView, bounds, context, registry)
                val scrollbar = if (horizontal) VanillaXScrollbarViewWidget(bar as XScrollbarView, IntRectangle(10, 200, 100, 5)) else VanillaYScrollbarViewWidget(bar as YScrollbarView, IntRectangle(200, 20, 5, 100))
                try {
                    Mockito.verify(child).setPosition(if (horizontal) -165 else 10, if (horizontal) 20 else -155)
                    Mockito.clearInvocations(child)
                    listOf(200, 50, 300).forEach { contentLength ->
                        Mockito.doReturn(if (horizontal) contentLength else 300).`when`(childView).evaluateActualSizeX()
                        Mockito.doReturn(if (horizontal) 300 else contentLength).`when`(childView).evaluateActualSizeY()
                        widget.render(environment.graphics, 50, 50, 0.0F)
                        scrollbar.render(environment.graphics, 50, 50, 0.0F)
                        Assertions.assertEquals(contentLength, bar.contentLength)
                        Assertions.assertEquals(100, bar.viewportLength)
                        Assertions.assertEquals(175.25, scrollOffset.value)
                        Assertions.assertEquals(175.25, barOffset.value)
                        Mockito.verifyNoInteractions(child)
                    }
                    widget.setSize(if (horizontal) 250 else 100, if (horizontal) 100 else 250)
                    widget.render(environment.graphics, 50, 50, 0.0F)
                    scrollbar.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(250, bar.viewportLength)
                    Assertions.assertEquals(175.25, scrollOffset.value)
                    Mockito.verifyNoInteractions(child)
                    scrollOffset.value = 10.5
                    Mockito.verify(child).setPosition(if (horizontal) -1 else 10, if (horizontal) 20 else 9)
                } finally {
                    widget.dispose()
                    widget.detach()
                    scrollbar.dispose()
                    connection.remove()
                }
                Assertions.assertTrue(scrollOffset.listeners.isEmpty())
                Assertions.assertTrue(barOffset.listeners.isEmpty())
            }
        }
    }

}
