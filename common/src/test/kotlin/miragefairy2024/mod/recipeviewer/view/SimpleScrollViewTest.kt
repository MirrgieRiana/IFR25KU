package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.register
import mirrg.kotlin.helium.max
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private val SCROLL_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

class SimpleScrollViewTest {

    @Test
    fun `縦スクロールでは割り当て幅に応じて子の高さを評価するのだ`() {
        listOf(true, false).forEach { xFirst ->
            val child: AbstractView = object : AbstractView() {
                override val sizingX = Sizing.FILL
                override val sizingY = Sizing.WRAP
                override fun evaluateContentSizeXImpl() = 0
                override fun evaluateContentSizeYImpl() = 1000 / regionSizeXProvider()
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
            }
            val view = YSimpleScrollView()
            view += child
            view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 25 }, { 10 })

            if (xFirst) child.evaluateActualSizeX() else child.evaluateActualSizeY()

            Assertions.assertEquals(IntPoint(25, 10), view.evaluateActualSize())
            Assertions.assertEquals(IntPoint(25, 40), child.evaluateActualSize())
            Assertions.assertEquals(0, view.evaluateContentSizeX())
            Assertions.assertEquals(0, view.evaluateContentSizeY())
        }
    }

    @Test
    fun `横スクロールでも逆軸の割り当てから子のサイズを評価できるのだ`() {
        val child: AbstractView = object : AbstractView() {
            override val sizingX = Sizing.WRAP
            override val sizingY = Sizing.FILL
            override fun evaluateContentSizeXImpl() = 1000 / regionSizeYProvider()
            override fun evaluateContentSizeYImpl() = 0
            override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
        }
        val view = XSimpleScrollView()
        view += child
        view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 10 }, { 25 })

        Assertions.assertEquals(IntPoint(40, 25), child.evaluateActualSize())
        Assertions.assertEquals(IntPoint(10, 25), view.evaluateActualSize())
        Assertions.assertEquals(0, view.evaluateContentSizeX())
        Assertions.assertEquals(0, view.evaluateContentSizeY())
    }

    @Test
    fun `FILL の子には表示領域とコンテンツの大きい方を割り当てるのだ`() {
        listOf(true, false).forEach { horizontal ->
            listOf(10, 200).forEach { length ->
                val child: AbstractView = object : AbstractView() {
                    override val sizingX = Sizing.FILL
                    override val sizingY = Sizing.FILL
                    override fun evaluateContentSizeXImpl() = length
                    override fun evaluateContentSizeYImpl() = length
                    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
                }
                val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                view += child
                view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 100 }, { 100 })

                val expected = if (horizontal) IntPoint(100 max length, 100) else IntPoint(100, 100 max length)
                Assertions.assertEquals(expected, child.evaluateActualSize())
            }
        }
    }

    @Test
    fun `子を持たないバーは指定された太さをコンテンツ寸法に使うのだ`() {
        listOf(false, true).forEach { horizontal ->
            val view = if (horizontal) XScrollbarView() else YScrollbarView()
            view.scrollbarThickness = 7
            Assertions.assertEquals(if (horizontal) Sizing.FILL else Sizing.WRAP, view.sizingX)
            Assertions.assertEquals(if (horizontal) Sizing.WRAP else Sizing.FILL, view.sizingY)
            view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 100 }, { 80 })
            Assertions.assertEquals(if (horizontal) IntPoint(100, 7) else IntPoint(7, 80), view.evaluateActualSize())
            Assertions.assertEquals(0, view.contentLength)
            Assertions.assertEquals(0, view.viewportLength)
            Assertions.assertThrows(IllegalArgumentException::class.java) { view.contentLength = -1 }
            Assertions.assertThrows(IllegalArgumentException::class.java) { view.viewportLength = -1 }
            Assertions.assertThrows(IllegalArgumentException::class.java) { view.scrollbarThickness = -1 }
            Assertions.assertThrows(IllegalArgumentException::class.java) { view.scrollStep = 0 }
        }
    }

    @Test
    fun `バーの短軸を FILL にしてもコンテンツ寸法は設定した太さを維持するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val view = if (horizontal) XScrollbarView() else YScrollbarView()
            view.scrollbarThickness = 7
            if (horizontal) (view as XScrollbarView).sizingY = Sizing.FILL else (view as YScrollbarView).sizingX = Sizing.FILL
            view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 100 }, { 80 })
            Assertions.assertEquals(if (horizontal) IntPoint(0, 7) else IntPoint(7, 0), view.evaluateContentSize())
            Assertions.assertEquals(IntPoint(100, 80), view.evaluateActualSize())
        }
    }

    @Test
    fun `狭い表示領域でも子へ全幅を割り当てスクロール量には正の値を要求するのだ`() {
        val child: AbstractView = object : AbstractView() {
            override val sizingX = Sizing.FILL
            override val sizingY = Sizing.FILL
            override fun evaluateContentSizeXImpl() = 0
            override fun evaluateContentSizeYImpl() = 0
            override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
        }
        val view = YSimpleScrollView()
        view += child
        view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 2 }, { 10 })

        Assertions.assertEquals(IntPoint(2, 10), child.evaluateActualSize())
        Assertions.assertThrows(IllegalArgumentException::class.java) { view.scrollStep = 0 }
    }

    @Test
    fun `寸法の変更をまとめて通知し負の寸法では状態を変更しないのだ`() {
        val view = XSimpleScrollView()
        val lengths = mutableListOf<Pair<Int, Int>>()
        view.onScrollBoundsChanged.register {
            lengths += Pair(view.contentLength, view.viewportLength)
        }
        view.updateScrollBounds(300, 100)
        view.updateScrollBounds(300, 100)
        Assertions.assertThrows(IllegalArgumentException::class.java) { view.updateScrollBounds(-1, 100) }
        Assertions.assertThrows(IllegalArgumentException::class.java) { view.updateScrollBounds(300, -1) }
        Assertions.assertEquals(300, view.contentLength)
        Assertions.assertEquals(100, view.viewportLength)
        view.updateScrollBounds(120, 100)
        Assertions.assertEquals(listOf(Pair(300, 100), Pair(120, 100)), lengths)
    }

    @Test
    fun `配置時に子の全長と表示領域の長さを通知するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val child: AbstractView = object : AbstractView() {
                override val sizingX = Sizing.WRAP
                override val sizingY = Sizing.WRAP
                override fun evaluateContentSizeXImpl() = 300
                override fun evaluateContentSizeYImpl() = 300
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
            }
            val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
            view += child
            view.initSizeCalculation(SCROLL_RENDERING_PROXY, { 100 }, { 80 })
            view.attachTo(IntPoint.ZERO) { _, _ -> ViewController.NONE }
            Assertions.assertEquals(300, view.contentLength)
            Assertions.assertEquals(if (horizontal) 100 else 80, view.viewportLength)
        }
    }

}
