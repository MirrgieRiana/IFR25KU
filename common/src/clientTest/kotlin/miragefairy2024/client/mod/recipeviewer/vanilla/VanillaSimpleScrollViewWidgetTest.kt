package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.connectScrollbarBounds
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.ObservableValue
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
    fun `領域のホイールとつまみのドラッグで同じ指定位置を更新するのだ`() {
        GuiTestEnvironment().use { environment ->
            val scrollOffset = ObservableValue(80.25)
            val view = YSimpleScrollView(scrollOffset)
            view += SimpleScrollTestView()
            view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
            val bar = YScrollbarView(scrollOffset)
            val connection = connectScrollbarBounds(view, bar)
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            var childOffset = IntPoint.ZERO
            registry.register { _, _: SimpleScrollTestView, bounds ->
                childOffset = IntPoint(bounds.x, bounds.y)
                object : ViewController {
                    override fun setOffset(offset: IntPoint) {
                        childOffset = offset
                    }

                    override fun detach() = Unit
                }
            }
            val widget = VanillaYSimpleScrollViewWidget(view, IntRectangle(0, 0, 100, 100), context, registry)
            val barWidget = VanillaYScrollbarViewWidget(bar, IntRectangle(100, 0, 5, 120))
            try {
                Assertions.assertEquals(IntPoint(0, -80), childOffset)

                Assertions.assertTrue(widget.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                Assertions.assertEquals(IntPoint(0, -95), childOffset)
                barWidget.render(environment.graphics, 102, 40, 0.0F)
                Assertions.assertEquals(IntRectangle(100, 38, 5, 40), environment.fills.last())
                Assertions.assertEquals(95.25, scrollOffset.value)

                Assertions.assertTrue(barWidget.mouseClicked(102.0, 40.0, 0))
                Assertions.assertTrue(barWidget.mouseDragged(102.0, 60.0, 0, 0.0, 20.0))
                Assertions.assertTrue(barWidget.mouseReleased(102.0, 60.0, 0))
                Assertions.assertEquals(145.25, scrollOffset.value, 1e-9)
                Assertions.assertEquals(IntPoint(0, -145), childOffset)

                scrollOffset.value = 999.75
                Assertions.assertEquals(IntPoint(0, -200), childOffset)
                widget.setHeight(250)
                Assertions.assertEquals(IntPoint(0, -50), childOffset)
                barWidget.render(environment.graphics, 102, 40, 0.0F)
                Assertions.assertEquals(IntRectangle(100, 20, 5, 100), environment.fills.last())
                Assertions.assertEquals(999.75, scrollOffset.value)
            } finally {
                widget.dispose()
                widget.detach()
                barWidget.dispose()
                connection.remove()
            }
            Assertions.assertTrue(scrollOffset.toList().isEmpty())
        }
    }

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
}
