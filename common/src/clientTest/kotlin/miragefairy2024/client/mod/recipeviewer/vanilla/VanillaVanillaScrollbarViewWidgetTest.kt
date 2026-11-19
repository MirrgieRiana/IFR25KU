package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.XVanillaScrollbarView
import miragefairy2024.mod.recipeviewer.views.YVanillaScrollbarView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.joml.Vector3f
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

private class ScrollbarSpriteGraphics : GuiGraphics(Minecraft.getInstance(), null) {
    val sprites = mutableListOf<Pair<String, IntRectangle>>()

    override fun blitSprite(sprite: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        val matrix: Matrix4f = pose().last().pose()
        val start: Vector3f = matrix.transformPosition(x.toFloat(), y.toFloat(), 0F, Vector3f())
        val end: Vector3f = matrix.transformPosition((x + width).toFloat(), (y + height).toFloat(), 0F, Vector3f())
        val left = minOf(start.x, end.x).roundToInt()
        val top = minOf(start.y, end.y).roundToInt()
        val right = maxOf(start.x, end.x).roundToInt()
        val bottom = maxOf(start.y, end.y).roundToInt()
        sprites += Pair(sprite.toString(), IntRectangle(left, top, right - left, bottom - top))
    }
}

class VanillaVanillaScrollbarViewWidgetTest {
    @Test
    fun `両軸でバニラの背景とつまみを描きスクロール位置を読み書きするのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use {
                val view = if (horizontal) XVanillaScrollbarView() else YVanillaScrollbarView()
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 6) else IntRectangle(10, 20, 6, 100)
                val widget = if (view is XVanillaScrollbarView) VanillaXVanillaScrollbarViewWidget(view, bounds) else VanillaYVanillaScrollbarViewWidget(view as YVanillaScrollbarView, bounds)
                val scrollOffset = if (view is XVanillaScrollbarView) view.scrollOffsetX else (view as YVanillaScrollbarView).scrollOffsetY
                view.contentLength = 300
                view.viewportLength = 100
                val mouseX = if (horizontal) 23.0 else 12.0
                val mouseY = if (horizontal) 22.0 else 33.0
                val graphics = ScrollbarSpriteGraphics()

                fun assertSprites(position: Int) {
                    graphics.sprites.clear()
                    widget.render(graphics, mouseX.toInt(), mouseY.toInt(), 0F)
                    val thumb = if (horizontal) IntRectangle(10 + position, 20, 33, 6) else IntRectangle(10, 20 + position, 6, 33)
                    Assertions.assertEquals(listOf(Pair("minecraft:widget/scroller_background", bounds), Pair("minecraft:widget/scroller", thumb)), graphics.sprites)
                    Assertions.assertTrue(graphics.pose().clear())
                }

                assertSprites(0)
                Assertions.assertTrue(widget.mouseScrolled(mouseX, mouseY, 0.0, -1.0))
                Assertions.assertEquals(15.0, scrollOffset.value)
                assertSprites(5)

                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(if (horizontal) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_DOWN, 0, 0))
                Assertions.assertEquals(30.0, scrollOffset.value)
                assertSprites(10)

                Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                val dragX = if (horizontal) 67.0 else 0.0
                val dragY = if (horizontal) 0.0 else 67.0
                Assertions.assertTrue(widget.mouseDragged(mouseX + dragX, mouseY + dragY, 0, dragX, dragY))
                Assertions.assertEquals(200.0, scrollOffset.value)
                assertSprites(67)
                Assertions.assertTrue(widget.mouseReleased(mouseX + dragX, mouseY + dragY, 0))

                scrollOffset.value = 100.0
                assertSprites(34)
                widget.dispose()
            }
        }
    }

    @Test
    fun `両軸で空内容と軸長ゼロは描画せずつまみが全長を占めるとドラッグしないのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use {
                val view = if (horizontal) XVanillaScrollbarView() else YVanillaScrollbarView()
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 6) else IntRectangle(10, 20, 6, 100)
                val widget = if (horizontal) VanillaXVanillaScrollbarViewWidget(view as XVanillaScrollbarView, bounds) else VanillaYVanillaScrollbarViewWidget(view as YVanillaScrollbarView, bounds)
                val graphics = ScrollbarSpriteGraphics()
                view.viewportLength = 100
                widget.render(graphics, 12, 22, 0F)
                Assertions.assertTrue(graphics.sprites.isEmpty())
                Assertions.assertFalse(widget.mouseClicked(12.0, 22.0, 0))

                view.contentLength = 300
                widget.setSize(if (horizontal) 0 else 6, if (horizontal) 6 else 0)
                widget.render(graphics, 12, 22, 0F)
                Assertions.assertTrue(graphics.sprites.isEmpty())
                Assertions.assertFalse(widget.mouseClicked(12.0, 22.0, 0))

                widget.setSize(6, 6)
                widget.render(graphics, 12, 22, 0F)
                val square = IntRectangle(10, 20, 6, 6)
                Assertions.assertEquals(listOf(Pair("minecraft:widget/scroller_background", square), Pair("minecraft:widget/scroller", square)), graphics.sprites)
                Assertions.assertFalse(widget.mouseClicked(12.0, 22.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                widget.dispose()
            }
        }
    }
}
