package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.lwjgl.glfw.GLFW

class VanillaScrollbarViewWidgetTest {
    @Test
    fun `単体のバーは両軸で太さと色を反映し領域を超えて描画しないのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(0, 7, 200).forEach { thickness ->
                GuiTestEnvironment().use {
                    val view = if (horizontal) XScrollbarView() else YScrollbarView()
                    view.scrollbarThickness = thickness
                    view.scrollbarColor = 0xA0FFFFFF.toInt()
                    view.scrollState.setBounds(300, 100)
                    val bounds = IntRectangle(10, 20, 100, 100)
                    val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                    val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
                    widget.render(graphics, 50, 50, 0.0F)
                    if (thickness == 0) {
                        Mockito.verify(graphics, Mockito.never()).fill(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())
                    } else {
                        val actualThickness = thickness.coerceAtMost(100)
                        val length = thickness.coerceIn(33, 100)
                        if (horizontal) {
                            Mockito.verify(graphics).fill(10, 120 - actualThickness, 10 + length, 120, view.scrollbarColor)
                        } else {
                            Mockito.verify(graphics).fill(110 - actualThickness, 20, 110, 20 + length, view.scrollbarColor)
                        }
                    }
                    widget.dispose()
                }
            }
        }
    }

    @Test
    fun `表示領域とは長さも位置も異なるバーを描画して両端までドラッグできるのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XScrollbarView() else YScrollbarView()
                view.scrollState.setBounds(300, 100)
                view.scrollState.value = 100.0
                val bounds = if (horizontal) IntRectangle(10, 200, 200, 5) else IntRectangle(200, 20, 5, 200)
                val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(listOf(if (horizontal) IntRectangle(77, 200, 67, 5) else IntRectangle(200, 87, 5, 67)), environment.fills)
                Assertions.assertTrue(widget.mouseClicked(if (horizontal) 80.0 else 202.0, if (horizontal) 202.0 else 90.0, 0))
                Assertions.assertTrue(widget.hasMouseCapture(0))
                Assertions.assertTrue(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
                Assertions.assertEquals(200.0, view.scrollState.value)
                Assertions.assertTrue(widget.mouseDragged(-100.0, -100.0, 0, 0.0, 0.0))
                Assertions.assertEquals(0.0, view.scrollState.value)
                Assertions.assertTrue(widget.mouseReleased(-100.0, -100.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                widget.dispose()
            }
        }
    }

    @Test
    fun `トラックのクリックとホイールとキーを扱い破棄時にキャプチャを解除するのだ`() {
        GuiTestEnvironment().use {
            val view = YScrollbarView()
            view.scrollState.setBounds(300, 100)
            val widget = VanillaYScrollbarViewWidget(view, IntRectangle(200, 20, 5, 100))
            Assertions.assertFalse(widget.mouseClicked(50.0, 50.0, 0))
            Assertions.assertFalse(widget.mouseClicked(202.0, 25.0, 1))
            Assertions.assertTrue(widget.mouseClicked(202.0, 110.0, 0))
            Assertions.assertEquals(200.0, view.scrollState.value)
            Assertions.assertTrue(widget.mouseReleased(202.0, 110.0, 0))
            Assertions.assertFalse(widget.mouseScrolled(202.0, 25.0, 0.0, -1.0))
            widget.isFocused = true
            Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
            Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
            Assertions.assertEquals(15.0, view.scrollState.value)
            Assertions.assertTrue(widget.mouseScrolled(202.0, 25.0, 0.0, -1.0))
            Assertions.assertEquals(30.0, view.scrollState.value)
            Assertions.assertTrue(widget.mouseClicked(202.0, 40.0, 0))
            widget.dispose()
            Assertions.assertFalse(widget.hasMouseCapture(0))
            Assertions.assertFalse(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
            Assertions.assertFalse(widget.mouseReleased(500.0, 500.0, 0))
            Assertions.assertFalse(widget.mouseClicked(202.0, 40.0, 0))
            Assertions.assertNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
        }
    }

    @Test
    fun `重ね配置のホバーは描画する帯と親の scissor の内側に限定するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XScrollbarView() else YScrollbarView()
                view.scrollState.setBounds(300, 100)
                val bounds = IntRectangle(10, 20, 100, 100)
                val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertFalse(widget.isHovered)
                val mouseX = if (horizontal) 20 else 107
                val mouseY = if (horizontal) 117 else 30
                widget.render(environment.graphics, mouseX, mouseY, 0.0F)
                Assertions.assertTrue(widget.isHovered)
                environment.graphics.enableScissor(10, 20, 100, 110)
                try {
                    widget.render(environment.graphics, mouseX, mouseY, 0.0F)
                    Assertions.assertFalse(widget.isHovered)
                } finally {
                    environment.graphics.disableScissor()
                    widget.dispose()
                }
            }
        }
    }

    @Test
    fun `スクロール不要または寸法がゼロなら描画と操作を行わないのだ`() {
        GuiTestEnvironment().use { environment ->
            val view = YScrollbarView()
            listOf(IntRectangle(10, 20, 5, 100), IntRectangle(10, 20, 0, 100), IntRectangle(10, 20, 5, 0)).forEach { bounds ->
                val widget = VanillaYScrollbarViewWidget(view, bounds)
                widget.render(environment.graphics, 11, 21, 0.0F)
                Assertions.assertFalse(widget.mouseClicked(11.0, 21.0, 0))
                Assertions.assertNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
                view.scrollState.setBounds(300, 100)
                if (bounds.sizeX == 0 || bounds.sizeY == 0) {
                    widget.render(environment.graphics, 11, 21, 0.0F)
                    Assertions.assertFalse(widget.mouseClicked(11.0, 21.0, 0))
                }
                widget.dispose()
                view.scrollState.setBounds(0, 0)
            }
            Assertions.assertTrue(environment.fills.isEmpty())
        }
    }
}
