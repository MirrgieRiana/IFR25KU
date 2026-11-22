package miragefairy2024.mod.recipeviewer.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class IntRectangleTest {

    @Test
    fun `負の寸法から矩形を構築できないのだ`() {
        assertThrows(IllegalArgumentException::class.java) { IntRectangle(-1, -2, -3, 0) }
        assertThrows(IllegalArgumentException::class.java) { IntRectangle(-1, -2, 0, -3) }
        assertThrows(IllegalArgumentException::class.java) { IntPoint(-1, -2).sized(IntPoint(-3, -4)) }
    }

    @Test
    fun `片軸の寸法がゼロでも負の座標と他軸の寸法を受け入れるのだ`() {
        assertEquals(IntPoint(0, 2), IntRectangle(-3, -4, 0, 2).size)
        assertEquals(IntPoint(2, 0), IntRectangle(-3, -4, 2, 0).size)
    }

    @Test
    fun `片軸の負の寸法を補正しても座標と他軸の寸法を維持するのだ`() {
        assertEquals(IntRectangle(-1, -2, 0, 7), IntPoint(-1, -2).sized(IntPoint(-3, 7)))
        assertEquals(IntRectangle(-1, -2, 7, 0), IntPoint(-1, -2).sized(IntPoint(7, -3)))
    }

    @Test
    fun `余分に縮小した軸と交差しない軸の寸法は 0 なのだ`() {
        assertEquals(IntRectangle(6, 6, 0, 0), IntRectangle(0, 0, 10, 10).grow(-6))
        val first = IntRectangle(0, 0, 10, 10)
        val second = IntRectangle(12, 0, 10, 10)
        assertEquals(IntRectangle(12, 0, 0, 10), first.intersect(second))
    }

    @Test
    fun `片軸を縮小し過ぎても他軸の寸法を維持するのだ`() {
        val bounds = IntRectangle(10, 20, 4, 6)
        assertEquals(IntRectangle(13, 20, 0, 6), bounds.grow(-3, 0))
        assertEquals(IntRectangle(10, 24, 4, 0), bounds.grow(0, -4))
    }

    @Test
    fun `片軸が交差しなくても他軸の交差する寸法を維持するのだ`() {
        val bounds = IntRectangle(10, 20, 4, 6)
        assertEquals(IntRectangle(15, 22, 0, 3), bounds.intersect(IntRectangle(15, 22, 4, 3)))
        assertEquals(IntRectangle(11, 27, 2, 0), bounds.intersect(IntRectangle(11, 27, 2, 3)))
    }

}
