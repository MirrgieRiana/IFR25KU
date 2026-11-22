package miragefairy2024.mod.recipeviewer.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class IntRectangleTest {

    @Test
    fun `負の寸法から矩形を構築できないのだ`() {
        assertThrows(IllegalArgumentException::class.java) { IntRectangle(-1, -2, -3, 0) }
        assertThrows(IllegalArgumentException::class.java) { IntRectangle(-1, -2, 0, -3) }
        assertEquals(IntRectangle(-1, -2, 0, 0), IntPoint(-1, -2).sized(IntPoint(-3, -4)))
    }

    @Test
    fun `余分に縮小した軸と交差しない軸の寸法は 0 なのだ`() {
        assertEquals(IntRectangle(6, 6, 0, 0), IntRectangle(0, 0, 10, 10).grow(-6))
        val first = IntRectangle(0, 0, 10, 10)
        val second = IntRectangle(12, 0, 10, 10)
        assertEquals(IntRectangle(12, 0, 0, 10), first.intersect(second))
    }

}
