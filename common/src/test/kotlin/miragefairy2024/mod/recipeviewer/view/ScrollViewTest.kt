package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import mirrg.kotlin.helium.max
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

private val SCROLL_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

class ScrollViewTest {

    @Test
    fun `縦スクロールでは割り当て幅に応じて子の高さを評価するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val child = object : AbstractView() {
                override val sizingX = Sizing.FILL
                override val sizingY = Sizing.WRAP
                override fun evaluateContentSizeXImpl() = 0
                override fun evaluateContentSizeYImpl() = 1000 / regionSizeXProvider()
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
            }
            val view = YScrollView()
            view += child
            view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 25 }, { 10 })

            if (xFirst) child.evaluateActualSizeX() else child.evaluateActualSizeY()

            assertEquals(IntPoint(25, 10), view.evaluateActualSize())
            assertEquals(IntPoint(20, 50), child.evaluateActualSize())
            assertEquals(5, view.evaluateContentSizeX())
            assertEquals(0, view.evaluateContentSizeY())
        }
    }

    @Test
    fun `横スクロールでも逆軸の割り当てから子のサイズを評価できるのだ`() {
        val child = object : AbstractView() {
            override val sizingX = Sizing.WRAP
            override val sizingY = Sizing.FILL
            override fun evaluateContentSizeXImpl() = 1000 / regionSizeYProvider()
            override fun evaluateContentSizeYImpl() = 0
            override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
        }
        val view = XScrollView()
        view += child
        view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 10 }, { 25 })

        assertEquals(IntPoint(50, 20), child.evaluateActualSize())
        assertEquals(IntPoint(10, 25), view.evaluateActualSize())
        assertEquals(0, view.evaluateContentSizeX())
        assertEquals(5, view.evaluateContentSizeY())
    }

    @Test
    fun `FILL の子には表示領域とコンテンツの大きい方を割り当てるのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(10, 200).forEach { length ->
                val child = object : AbstractView() {
                    override val sizingX = Sizing.FILL
                    override val sizingY = Sizing.FILL
                    override fun evaluateContentSizeXImpl() = length
                    override fun evaluateContentSizeYImpl() = length
                    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
                }
                val view = if (horizontal) XScrollView() else YScrollView()
                view += child
                view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 100 }, { 100 })

                val expected = if (horizontal) IntPoint(100 max length, 95) else IntPoint(95, 100 max length)
                assertEquals(expected, child.evaluateActualSize())
            }
        }
    }

    @Test
    fun `表示領域より太いスクロールバーを指定しても子へ負のサイズを渡さないのだ`() {
        val child = object : AbstractView() {
            override val sizingX = Sizing.FILL
            override val sizingY = Sizing.FILL
            override fun evaluateContentSizeXImpl() = 0
            override fun evaluateContentSizeYImpl() = 0
            override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
        }
        val view = YScrollView()
        view += child
        view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 2 }, { 10 })

        assertEquals(IntPoint(0, 10), child.evaluateActualSize())
        assertThrows(IllegalArgumentException::class.java) { view.scrollbarThickness = -1 }
    }

}
