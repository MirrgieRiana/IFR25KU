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

private const val TRACK_TEXTURE = "minecraft:textures/gui/container/creative_inventory/tab_items.png"
private const val THUMB_TEXTURE = "minecraft:textures/gui/sprites/container/creative_inventory/scroller.png"

private class ScrollbarGraphics : GuiGraphics(Minecraft.getInstance(), null) {
    val blits = mutableListOf<Pair<String, IntRectangle>>()

    override fun blit(atlasLocation: ResourceLocation, x: Int, y: Int, width: Int, height: Int, uOffset: Float, vOffset: Float, uWidth: Int, vHeight: Int, textureWidth: Int, textureHeight: Int) {
        val matrix: Matrix4f = pose().last().pose()
        val start: Vector3f = matrix.transformPosition(x.toFloat(), y.toFloat(), 0F, Vector3f())
        val end: Vector3f = matrix.transformPosition((x + width).toFloat(), (y + height).toFloat(), 0F, Vector3f())
        val left = minOf(start.x, end.x).roundToInt()
        val top = minOf(start.y, end.y).roundToInt()
        val right = maxOf(start.x, end.x).roundToInt()
        val bottom = maxOf(start.y, end.y).roundToInt()
        blits += Pair(atlasLocation.toString(), IntRectangle(left, top, right - left, bottom - top))
    }

    fun assertTrackAndThumb(track: IntRectangle, thumb: IntRectangle, thumbTexture: String) {
        Assertions.assertEquals(listOf(TRACK_TEXTURE, thumbTexture), blits.map { it.first }.distinct())
        val trackBlits = blits.takeWhile { it.first == TRACK_TEXTURE }
        val thumbBlits = blits.drop(trackBlits.size)
        Assertions.assertTrue(thumbBlits.all { it.first == thumbTexture })
        assertPaintedBounds(track, trackBlits.map { it.second })
        assertPaintedBounds(thumb, thumbBlits.map { it.second })
        Assertions.assertTrue(pose().clear())
    }

    private fun assertPaintedBounds(bounds: IntRectangle, rectangles: List<IntRectangle>) {
        Assertions.assertEquals(bounds.x, rectangles.minOf { it.x })
        Assertions.assertEquals(bounds.y, rectangles.minOf { it.y })
        Assertions.assertEquals(bounds.x + bounds.sizeX, rectangles.maxOf { it.x + it.sizeX })
        Assertions.assertEquals(bounds.y + bounds.sizeY, rectangles.maxOf { it.y + it.sizeY })
        Assertions.assertEquals(bounds.sizeX * bounds.sizeY, rectangles.sumOf { it.sizeX * it.sizeY })
    }
}

class VanillaVanillaScrollbarViewWidgetTest {
    @Test
    fun `両軸でバニラの背景とつまみを描きスクロール位置を読み書きするのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use {
                val view = if (horizontal) XVanillaScrollbarView() else YVanillaScrollbarView()
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 18) else IntRectangle(10, 20, 18, 100)
                val widget = if (view is XVanillaScrollbarView) VanillaXVanillaScrollbarViewWidget(view, bounds) else VanillaYVanillaScrollbarViewWidget(view as YVanillaScrollbarView, bounds)
                val scrollOffset = if (view is XVanillaScrollbarView) view.scrollOffsetX else (view as YVanillaScrollbarView).scrollOffsetY
                view.contentLength = 300
                view.viewportLength = 100
                val mouseX = if (horizontal) 23.0 else 12.0
                val mouseY = if (horizontal) 22.0 else 33.0
                val graphics = ScrollbarGraphics()

                fun assertScrollbar(position: Int) {
                    graphics.blits.clear()
                    widget.render(graphics, mouseX.toInt(), mouseY.toInt(), 0F)
                    val thumb = if (horizontal) IntRectangle(11 + position, 21, 31, 16) else IntRectangle(11, 21 + position, 16, 31)
                    graphics.assertTrackAndThumb(bounds, thumb, THUMB_TEXTURE)
                }

                assertScrollbar(0)
                Assertions.assertTrue(widget.mouseScrolled(mouseX, mouseY, 0.0, -1.0))
                Assertions.assertEquals(15.0, scrollOffset.value)
                assertScrollbar(5)

                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(if (horizontal) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_DOWN, 0, 0))
                Assertions.assertEquals(30.0, scrollOffset.value)
                assertScrollbar(10)

                Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                val dragX = if (horizontal) 67.0 else 0.0
                val dragY = if (horizontal) 0.0 else 67.0
                Assertions.assertTrue(widget.mouseDragged(mouseX + dragX, mouseY + dragY, 0, dragX, dragY))
                Assertions.assertEquals(200.0, scrollOffset.value)
                assertScrollbar(67)
                Assertions.assertTrue(widget.mouseReleased(mouseX + dragX, mouseY + dragY, 0))

                scrollOffset.value = 100.0
                assertScrollbar(34)
                widget.dispose()
            }
        }
    }
}
