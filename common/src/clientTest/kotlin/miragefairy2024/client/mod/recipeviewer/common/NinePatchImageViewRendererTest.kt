package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import kotlin.math.floor

class NinePatchImageViewRendererTest {

    private val repeatedX = intArrayOf(0, 1, 2, 3, 4, 2, 3, 4, 2, 3, 4, 2, 5, 6, 7, 8)
    private val stretchedX = intArrayOf(0, 1, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 6, 7, 8)
    private val repeatedY = intArrayOf(0, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5, 6)
    private val stretchedY = intArrayOf(0, 1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 6)

    private fun check(repeatX: Boolean, repeatY: Boolean, expectedX: IntArray, expectedY: IntArray) {
        val textureId: ResourceLocation = ResourceLocation.fromNamespaceAndPath("test", "nine_patch")
        val view = NinePatchImageView(textureId, 9, 7, 2, 3, 4, 1, 4, 2, repeatX, repeatY)
        val bounds = IntRectangle(10, 20, expectedX.size, expectedY.size)
        val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)

        NinePatchImageViewRenderer.render(view, Slot(Unit), bounds, graphics, 0, 0, false, 0.0F)

        val pixels = Array(bounds.sizeY) { arrayOfNulls<IntPoint>(bounds.sizeX) }
        Mockito.mockingDetails(graphics).invocations.forEach { invocation ->
            Assertions.assertEquals("blit", invocation.method.name)
            val arguments: Array<Any> = invocation.arguments
            Assertions.assertEquals(textureId, arguments[0])
            val x = arguments[1] as Int
            val y = arguments[2] as Int
            val stretched = arguments.size == 11
            val width = arguments[if (stretched) 3 else 5] as Int
            val height = arguments[if (stretched) 4 else 6] as Int
            val u = arguments[if (stretched) 5 else 3] as Float
            val v = arguments[if (stretched) 6 else 4] as Float
            val sourceWidth = if (stretched) arguments[7] as Int else width
            val sourceHeight = if (stretched) arguments[8] as Int else height
            Assertions.assertEquals(9, arguments[arguments.size - 2])
            Assertions.assertEquals(7, arguments[arguments.size - 1])

            (0 until height).forEach { dy ->
                (0 until width).forEach { dx ->
                    val targetX = x + dx - bounds.x
                    val targetY = y + dy - bounds.y
                    Assertions.assertTrue(targetX in expectedX.indices && targetY in expectedY.indices)
                    Assertions.assertNull(pixels[targetY][targetX], "Overlapping pixel at $targetX, $targetY")
                    pixels[targetY][targetX] = IntPoint(floor(u + (dx + 0.5) * sourceWidth / width).toInt(), floor(v + (dy + 0.5) * sourceHeight / height).toInt())
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
