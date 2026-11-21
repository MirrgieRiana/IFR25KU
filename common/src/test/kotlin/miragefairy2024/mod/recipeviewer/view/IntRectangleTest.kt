package miragefairy2024.mod.recipeviewer.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class IntRectangleTest {

    @Test
    fun `負の幅と高さを拒否し寸法ゼロと負の座標を許すのだ`() {
        assertThrows(IllegalArgumentException::class.java) { IntRectangle(0, 0, -1, 2) }
        assertThrows(IllegalArgumentException::class.java) { IntRectangle(0, 0, 2, -1) }
        assertEquals(IntPoint(0, 2), IntRectangle(-3, -4, 0, 2).size)
        assertEquals(IntPoint(2, 0), IntRectangle(-3, -4, 2, 0).size)
    }

    @Test
    fun `縮小し過ぎた軸と交差しない軸は寸法ゼロになるのだ`() {
        val bounds = IntRectangle(10, 20, 4, 6)
        assertEquals(IntRectangle(13, 20, 0, 6), bounds.grow(-3, 0))
        assertEquals(IntRectangle(15, 22, 0, 3), bounds.intersect(IntRectangle(15, 22, 4, 3)))
    }

}
