package miragefairy2024.mod.recipeviewer.views

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.evaluateContentSize
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

class ScrollViewTest {

    /** 寸法の計算だけを試すため、文字の計測は呼ばれないのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
        override fun getTextHeight() = throw UnsupportedOperationException()
        override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
    }

    private fun createScrollView(childSize: IntPoint, regionSize: IntPoint): ScrollView {
        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += SpaceView(childSize)
        scrollView.initSizeCalculation(renderingProxy, { regionSize.x }, { regionSize.y })
        return scrollView
    }

    /** 幅を親に合わせる子を持つスクロール領域なのだ～🌱 */
    private fun createFillingScrollView(childSizeY: Int, regionSize: IntPoint): Pair<ScrollView, YListView> {
        val innerView = YListView().also { it.sizingX = Sizing.FILL }
        innerView += SpaceView(IntPoint(0, childSizeY))
        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += innerView
        scrollView.initSizeCalculation(renderingProxy, { regionSize.x }, { regionSize.y })
        return Pair(scrollView, innerView)
    }

    @Test
    fun `縦は溢れてよいため親へ要求する高さは0なのだ～🌱`() {
        val scrollView = createScrollView(IntPoint(100, 300), IntPoint(100, 50))
        assertEquals(IntPoint(100, 0), scrollView.evaluateContentSize())
    }

    @Test
    fun `縦は親が与えた空間を埋めるのだ～🌱`() {
        val scrollView = createScrollView(IntPoint(100, 300), IntPoint(100, 50))
        assertEquals(Sizing.FILL, scrollView.sizingY)
        assertEquals(IntPoint(100, 50), scrollView.evaluateActualSize())
    }

    @Test
    fun `幅を親に合わせる子にはこのViewの幅と子のコンテンツの高さが渡るのだ～🌱`() {
        val (_, innerView) = createFillingScrollView(300, IntPoint(120, 50))
        assertEquals(IntPoint(120, 300), innerView.evaluateActualSize())
    }

    @Test
    fun `幅が中身で決まる子はこのViewの幅まで広がらないのだ～🌱`() {
        val scrollView = createScrollView(IntPoint(100, 300), IntPoint(120, 50))
        assertEquals(IntPoint(100, 300), scrollView.childView.evaluateActualSize())
    }

    @Test
    fun `子が溢れた分だけスクロールできるのだ～🌱`() {
        val scrollView = createScrollView(IntPoint(100, 300), IntPoint(100, 50))
        assertEquals(250, scrollView.maxScrollAmount)
    }

    @Test
    fun `子が収まるならスクロールできないのだ～🌱`() {
        val scrollView = createScrollView(IntPoint(100, 30), IntPoint(100, 50))
        assertEquals(0, scrollView.maxScrollAmount)
    }

    @Test
    fun `スクロール量は0からはみ出した高さまでに収まるのだ～🌱`() {
        val scrollView = createScrollView(IntPoint(100, 300), IntPoint(100, 50))
        scrollView.scrollAmount = 100
        assertEquals(100, scrollView.scrollAmount)
        scrollView.scrollAmount = 1000
        assertEquals(250, scrollView.scrollAmount)
        scrollView.scrollAmount = -10
        assertEquals(0, scrollView.scrollAmount)
    }

    @Test
    fun `スクロールバーの幅はレイアウトから引かれないのだ～🌱`() {
        val (scrollView, innerView) = createFillingScrollView(300, IntPoint(120, 50))
        assertEquals(5, scrollView.scrollbarThickness)
        assertEquals(120, innerView.evaluateActualSize().x)
    }

    @Test
    fun `子を置かず自分自身だけを置くのだ～🌱`() {
        val scrollView = createScrollView(IntPoint(100, 300), IntPoint(100, 50))
        val placedList = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        scrollView.attachTo(IntPoint(7, 11)) { view, bounds ->
            placedList += Pair(view, bounds)
            object : ViewController {
                override fun setOffset(offset: IntPoint) = Unit
                override fun detach() = Unit
            }
        }
        assertEquals(1, placedList.size)
        assertSame(scrollView, placedList.single().first)
        assertEquals(IntRectangle(7, 11, 100, 50), placedList.single().second)
    }

    @Test
    fun `子を余白で包むとスクロールバーの幅だけ中身が狭まるのだ～🌱`() {
        val innerView = YListView().also { it.sizingX = Sizing.FILL }
        innerView += SpaceView(IntPoint(0, 300))
        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += Child(Unit, innerView).margin(0, 5, 0, 0)
        scrollView.initSizeCalculation(renderingProxy, { 120 }, { 50 })
        assertEquals(IntPoint(120, 300), scrollView.childView.evaluateActualSize())
        assertEquals(IntPoint(115, 300), innerView.evaluateActualSize())
    }

    @Test
    fun `折り返しの高さはスクロール領域の幅で決まりリサイズで再計算するのだ～🌱`() {
        val proxy = object : RenderingProxy {
            override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
            override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
            override fun getTextHeight() = 9
            override fun wrapText(text: FormattedText, maxWidth: Int) = text.string.chunked(maxWidth).map { FormattedCharSequence.forward(it, Style.EMPTY) }
        }
        val child = MultiLineTextView()
        child.text.value = Component.literal("abcdefghijkl")
        val scrollView = ScrollView(5, ScrollbarStyle.Vanilla)
        scrollView += Child(Unit, child).margin(0, 5, 0, 0)
        scrollView.initSizeCalculation(proxy, { 9 }, { 10 })
        assertEquals(IntPoint(4, 31), child.evaluateActualSize())
        assertEquals(21, scrollView.maxScrollAmount)

        scrollView.initSizeCalculation(proxy, { 11 }, { 10 })
        assertEquals(IntPoint(6, 20), child.evaluateActualSize())
        assertEquals(10, scrollView.maxScrollAmount)
    }

}
