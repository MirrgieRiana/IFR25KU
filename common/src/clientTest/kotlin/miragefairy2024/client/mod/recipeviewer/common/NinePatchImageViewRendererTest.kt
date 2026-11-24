package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.createView
import mirrg.kotlin.helium.Slot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.floor
import kotlin.math.roundToInt

class NinePatchImageViewRendererTest {

    private val repeatedX = intArrayOf(0, 1, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 5, 6, 7, 8)
    private val stretchedX = intArrayOf(0, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 6, 7, 8)
    private val repeatedY = intArrayOf(0, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5, 6)
    private val stretchedY = intArrayOf(0, 1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 6)

    private fun check(repeatX: Boolean, repeatY: Boolean, expectedX: IntArray, expectedY: IntArray) {
        val ninePatchTextureCard = NinePatchTextureCard("test/nine_patch", 9, 7, 2, 3, 4, 1, 4, 2, repeatX, repeatY)
        val view = ninePatchTextureCard.createView()
        val bounds = IntRectangle(10, 20, expectedX.size, expectedY.size)

        val draw = GuiTestEnvironment().use { environment ->
            environment.withTexturedDraws { draws ->
                NinePatchImageViewRenderer.render(view, Slot(Unit), bounds, environment.graphics, 0, 0, false, 0.0F)
                Assertions.assertEquals(1, draws.size)
                draws.single()
            }
        }
        Assertions.assertEquals(ninePatchTextureCard.texture, draw.texture)

        val pixels = Array(bounds.sizeY) { arrayOfNulls<IntPoint>(bounds.sizeX) }
        draw.quads.forEach { (topLeft, bottomLeft, bottomRight, topRight) ->
            Assertions.assertEquals(GuiTestEnvironment.TexturedVertex(topLeft.x, bottomRight.y, topLeft.u, bottomRight.v), bottomLeft)
            Assertions.assertEquals(GuiTestEnvironment.TexturedVertex(bottomRight.x, topLeft.y, bottomRight.u, topLeft.v), topRight)
            val x = topLeft.x.roundToInt()
            val y = topLeft.y.roundToInt()
            val width = bottomRight.x.roundToInt() - x
            val height = bottomRight.y.roundToInt() - y
            val u = (topLeft.u * ninePatchTextureCard.textureWidth).roundToInt()
            val v = (topLeft.v * ninePatchTextureCard.textureHeight).roundToInt()
            val sourceWidth = (bottomRight.u * ninePatchTextureCard.textureWidth).roundToInt() - u
            val sourceHeight = (bottomRight.v * ninePatchTextureCard.textureHeight).roundToInt() - v

            (0 until height).forEach { dy ->
                (0 until width).forEach { dx ->
                    val targetX = x + dx - bounds.x
                    val targetY = y + dy - bounds.y
                    Assertions.assertTrue(targetX in expectedX.indices && targetY in expectedY.indices)
                    Assertions.assertNull(pixels[targetY][targetX], "Overlapping pixel at $targetX, $targetY")
                    pixels[targetY][targetX] = IntPoint(floor(u + (dx + 0.5) * sourceWidth / width.toDouble()).toInt(), floor(v + (dy + 0.5) * sourceHeight / height.toDouble()).toInt())
                }
            }
        }

        expectedY.indices.forEach { y ->
            expectedX.indices.forEach { x ->
                Assertions.assertEquals(IntPoint(expectedX[x], expectedY[y]), pixels[y][x], "Source pixel at $x, $y")
            }
        }
    }

    @Test
    fun `両軸の反復描画では端数を切り詰めて枠と中央を埋めるのだ`() {
        check(true, true, repeatedX, repeatedY)
    }

    @Test
    fun `両軸の伸長描画では角の寸法を保って辺と中央を伸ばすのだ`() {
        check(false, false, stretchedX, stretchedY)
    }

    @Test
    fun `X 軸を反復し Y 軸だけを伸長して辺と中央を埋めるのだ`() {
        check(true, false, repeatedX, stretchedY)
    }

    @Test
    fun `Y 軸を反復し X 軸だけを伸長して辺と中央を埋めるのだ`() {
        check(false, true, stretchedX, repeatedY)
    }

    @Test
    fun `中央の幅または高さが0でも残りの辺と角を描画するのだ`() {
        val borderX = intArrayOf(0, 1, 5, 6, 7, 8)
        val borderY = intArrayOf(0, 5, 6)
        listOf(true, false).forEach { repeatX ->
            listOf(true, false).forEach { repeatY ->
                check(repeatX, repeatY, borderX, if (repeatY) repeatedY else stretchedY)
                check(repeatX, repeatY, if (repeatX) repeatedX else stretchedX, borderY)
                check(repeatX, repeatY, borderX, borderY)
            }
        }
    }

}
