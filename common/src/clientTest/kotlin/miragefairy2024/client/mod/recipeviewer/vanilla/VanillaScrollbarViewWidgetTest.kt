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
                    view.contentLength = 300
                    val bounds = IntRectangle(10, 20, 100, 100)
                    val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                    val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
                    widget.render(graphics, 50, 50, 0.0F)
                    if (thickness == 0) {
                        Mockito.verify(graphics, Mockito.never()).fill(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())
                        Assertions.assertFalse(widget.mouseClicked(50.0, 50.0, 0))
                        Assertions.assertFalse(widget.hasMouseCapture(0))
                        Assertions.assertFalse(widget.mouseDragged(100.0, 100.0, 0, 0.0, 0.0))
                        val path = widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true))
                        Assertions.assertNotNull(path)
                        path!!.applyFocus(true)
                        Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                        Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                        Assertions.assertTrue(widget.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                        view.scrollbarThickness = 5
                        widget.render(graphics, 50, 50, 0.0F)
                        if (horizontal) {
                            Mockito.verify(graphics).fill(15, 115, 48, 120, view.scrollbarColor)
                        } else {
                            Mockito.verify(graphics).fill(105, 25, 110, 58, view.scrollbarColor)
                        }
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


}
