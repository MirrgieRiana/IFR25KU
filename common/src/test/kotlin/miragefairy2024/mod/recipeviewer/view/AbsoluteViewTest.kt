package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AbsoluteViewTest {

    @Test
    fun `Fillは各軸の割り当て領域を使用するのだ`() {
        val position = AbsoluteView.Fill
        assertEquals(101, position.getSizeX(17, 101))
        assertEquals(203, position.getSizeY(31, 203))
    }

    @Test
    fun `Offsetは各軸のコンテンツサイズを使用するのだ`() {
        val position = AbsoluteView.Offset(IntPoint(3, 5))
        assertEquals(17, position.getSizeX(17, 101))
        assertEquals(31, position.getSizeY(31, 203))
    }

    @Test
    fun `Boundsは各軸に指定されたサイズを使用するのだ`() {
        val position = AbsoluteView.Bounds(IntRectangle(3, 5, 29, 43))
        assertEquals(29, position.getSizeX(17, 101))
        assertEquals(43, position.getSizeY(31, 203))
    }

}
