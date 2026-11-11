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
    fun `設定した太さと異なる実寸法でもその矩形で描画とクリックを扱うのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(0, 5, 200).forEach { thickness ->
                GuiTestEnvironment().use {
                    val view = if (horizontal) XScrollbarView() else YScrollbarView()
                    view.scrollbarThickness = thickness
                    view.scrollbarColor = 0xA0FFFFFF.toInt()
                    view.contentLength = 300
                    val bounds = if (horizontal) IntRectangle(10, 20, 100, 7) else IntRectangle(10, 20, 7, 100)
                    val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                    val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
                    widget.render(graphics, 50, 50, 0.0F)
                    if (horizontal) {
                        Mockito.verify(graphics).fill(10, 20, 43, 27, view.scrollbarColor)
                    } else {
                        Mockito.verify(graphics).fill(10, 20, 17, 53, view.scrollbarColor)
                    }
                    Assertions.assertFalse(widget.mouseClicked(if (horizontal) 20.0 else 17.0, if (horizontal) 27.0 else 30.0, 0))
                    val mouseX = if (horizontal) 20.0 else 12.0
                    val mouseY = if (horizontal) 22.0 else 30.0
                    Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                    Assertions.assertTrue(widget.hasMouseCapture(0))
                    Assertions.assertTrue(widget.mouseReleased(mouseX, mouseY, 0))
                    val path = widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true))
                    Assertions.assertNotNull(path)
                    path!!.applyFocus(true)
                    Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                    Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                    Assertions.assertTrue(widget.mouseScrolled(mouseX, mouseY, 0.0, -1.0))
                    widget.render(graphics, mouseX.toInt(), mouseY.toInt(), 0.0F)
                    if (horizontal) {
                        Mockito.verify(graphics).fill(15, 20, 48, 27, view.scrollbarColor)
                    } else {
                        Mockito.verify(graphics).fill(10, 25, 17, 58, view.scrollbarColor)
                    }
                    widget.dispose()
                }
            }
        }
    }

    @Test
    fun `実際の太さが 0 なら描画とクリックをせずキー操作は継続するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XScrollbarView() else YScrollbarView()
                view.contentLength = 300
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 0) else IntRectangle(10, 20, 0, 100)
                val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                widget.render(environment.graphics, 10, 20, 0.0F)
                Assertions.assertTrue(environment.fills.isEmpty())
                Assertions.assertFalse(widget.mouseClicked(10.0, 20.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                widget.dispose()
            }
        }
    }

    @Test
    fun `コンテンツ長が縮小したら描画前に位置を現在範囲へ補正するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XScrollbarView() else YScrollbarView()
                view.contentLength = 300
                val bounds = if (horizontal) IntRectangle(10, 200, 100, 5) else IntRectangle(200, 20, 5, 100)
                val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                view.contentLength = 150
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(43, 200, 67, 5) else IntRectangle(200, 53, 5, 67), environment.fills.last())
                view.contentLength = 100
                environment.fills.clear()
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertTrue(environment.fills.isEmpty())
                view.contentLength = 300
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(listOf(if (horizontal) IntRectangle(10, 200, 33, 5) else IntRectangle(200, 20, 5, 33)), environment.fills)
                widget.dispose()
            }
        }
    }

    @Test
    fun `単体のバーを指定した長さで描画して両端までドラッグできるのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XScrollbarView() else YScrollbarView()
                view.contentLength = 300
                val bounds = if (horizontal) IntRectangle(10, 200, 200, 5) else IntRectangle(200, 20, 5, 200)
                val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(listOf(if (horizontal) IntRectangle(10, 200, 133, 5) else IntRectangle(200, 20, 5, 133)), environment.fills)
                Assertions.assertTrue(widget.mouseClicked(if (horizontal) 80.0 else 202.0, if (horizontal) 202.0 else 90.0, 0))
                Assertions.assertTrue(widget.hasMouseCapture(0))
                Assertions.assertTrue(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 133, 5) else IntRectangle(200, 87, 5, 133), environment.fills.last())
                Assertions.assertTrue(widget.mouseDragged(-100.0, -100.0, 0, 0.0, 0.0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(10, 200, 133, 5) else IntRectangle(200, 20, 5, 133), environment.fills.last())
                Assertions.assertTrue(widget.mouseReleased(-100.0, -100.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                widget.dispose()
            }
        }
    }

    @Test
    fun `ドラッグ中に実際の太さを 0 にしても移動を継続しリリースでキャプチャを解除するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XScrollbarView() else YScrollbarView()
                view.contentLength = 300
                val bounds = if (horizontal) IntRectangle(10, 200, 100, 5) else IntRectangle(200, 20, 5, 100)
                val widget = if (horizontal) VanillaXScrollbarViewWidget(view as XScrollbarView, bounds) else VanillaYScrollbarViewWidget(view as YScrollbarView, bounds)
                Assertions.assertTrue(widget.mouseClicked(if (horizontal) 15.0 else 202.0, if (horizontal) 202.0 else 25.0, 0))
                widget.setSize(if (horizontal) 100 else 0, if (horizontal) 0 else 100)
                Assertions.assertTrue(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
                Assertions.assertTrue(widget.hasMouseCapture(0))
                widget.setSize(if (horizontal) 100 else 5, if (horizontal) 5 else 100)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                widget.setSize(100, 100)
                Assertions.assertTrue(widget.mouseDragged(-100.0, -100.0, 0, 0.0, 0.0))
                widget.setSize(if (horizontal) 100 else 5, if (horizontal) 5 else 100)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                widget.setSize(if (horizontal) 100 else 0, if (horizontal) 0 else 100)
                Assertions.assertTrue(widget.mouseReleased(500.0, 500.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                Assertions.assertFalse(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
                widget.dispose()
            }
        }
    }

    @Test
    fun `トラックのクリックとホイールとキーを扱い破棄時にキャプチャを解除するのだ`() {
        GuiTestEnvironment().use { environment ->
            val view = YScrollbarView()
            view.contentLength = 300
            val widget = VanillaYScrollbarViewWidget(view, IntRectangle(200, 20, 5, 100))
            Assertions.assertFalse(widget.mouseClicked(50.0, 50.0, 0))
            Assertions.assertFalse(widget.mouseClicked(202.0, 25.0, 1))
            Assertions.assertTrue(widget.mouseClicked(202.0, 110.0, 0))
            widget.render(environment.graphics, 202, 25, 0.0F)
            Assertions.assertEquals(IntRectangle(200, 87, 5, 33), environment.fills.last())
            Assertions.assertTrue(widget.mouseReleased(202.0, 110.0, 0))
            Assertions.assertFalse(widget.mouseScrolled(202.0, 25.0, 0.0, -1.0))
            widget.isFocused = true
            Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
            Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
            widget.render(environment.graphics, 202, 25, 0.0F)
            Assertions.assertEquals(IntRectangle(200, 25, 5, 33), environment.fills.last())
            Assertions.assertTrue(widget.mouseScrolled(202.0, 25.0, 0.0, -1.0))
            widget.render(environment.graphics, 202, 25, 0.0F)
            Assertions.assertEquals(IntRectangle(200, 30, 5, 33), environment.fills.last())
            Assertions.assertTrue(widget.mouseClicked(202.0, 40.0, 0))
            widget.dispose()
            Assertions.assertFalse(widget.hasMouseCapture(0))
            Assertions.assertFalse(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
            Assertions.assertFalse(widget.mouseReleased(500.0, 500.0, 0))
            Assertions.assertFalse(widget.mouseClicked(202.0, 40.0, 0))
            Assertions.assertNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
        }
    }


}
