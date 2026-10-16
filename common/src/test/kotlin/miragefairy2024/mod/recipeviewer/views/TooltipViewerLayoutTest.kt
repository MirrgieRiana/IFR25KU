package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `TooltipViewerClientModule.kt`の画面の骨組みが、既にあるViewで組めるかを確認するのだ～🌱
 * 中身の無い部分は、同じ寸法を占めるSpaceViewで代えているのだ～🌱
 */
class TooltipViewerLayoutTest {

    /** 寸法の計算だけを試すため、文字の計測は呼ばれないのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
        override fun getTextHeight() = throw UnsupportedOperationException()
        override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
    }

    /** 画面の寸法なのだ～🌱 */
    private val screenSize = IntPoint(400, 300)

    /** 検索欄の高さは、バニラのテキスト入力と同じなのだ～🌱 */
    private val textBoxSizeY = 20

    private class Layout(val outerView: MarginView, val innerView: YListView, val scrollView: ScrollView)

    /** 実際の画面と同じ入れ子を組むのだ～🌱 */
    private fun buildRoot(scrollContentSizeY: Int): Layout {
        // 検索欄なのだ～🌱
        val textBoxView = TextBoxView(textBoxSizeY)

        // スクロールの中身なのだ～🌱
        val scrollContentView = YListView().also { it.sizingX = Sizing.FILL }
        scrollContentView += SpaceView(IntPoint(0, scrollContentSizeY))

        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0000000.toInt()))
        scrollView += Child(Unit, scrollContentView).margin(0, 5, 0, 0)

        // 内枠なのだ～🌱 横幅は、外側のminContentSizeXが240へ固定するのだ～🌱
        val innerView = YListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        innerView += textBoxView
        innerView += YSpaceView(2)
        // 余った空間を取るのがweightなのだ～🌱
        innerView += Child(innerView.createDefaultPosition(), scrollView).also { it.position.weight = 1.0 }

        // 外枠なのだ～🌱
        val outerView = YListView().also { it.sizingY = Sizing.FILL }
        outerView += Child(outerView.createDefaultPosition(), innerView).minContentSizeX(240).also { it.position.weight = 1.0 }

        val rootView = YListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        val outerMarginView = MarginView(7, 7, 7, 7)
        outerMarginView += outerView
        val rootMarginView = MarginView(4, 4, 4, 4)
        rootMarginView += outerMarginView
        rootView += Child(rootView.createDefaultPosition(), rootMarginView).also {
            it.position.alignmentX = Alignment.CENTER
            it.position.weight = 1.0
        }

        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(screenSize)
        return Layout(outerMarginView, innerView, scrollView)
    }

    @Test
    fun `内枠の横幅は固定の240になるのだ～🌱`() {
        val layout = buildRoot(1000)
        assertEquals(240, layout.innerView.actualSize.x)
    }

    @Test
    fun `外枠は内枠に余白を足した横幅になるのだ～🌱`() {
        val layout = buildRoot(1000)
        // 内枠の240に、左右の余白7ずつを足すのだ～🌱
        assertEquals(254, layout.outerView.actualSize.x)
    }

    @Test
    fun `外枠の縦は画面から外側の余白を引いた高さいっぱいになるのだ～🌱`() {
        val layout = buildRoot(1000)
        // 画面の300から、根の余白4ずつを引くのだ～🌱
        assertEquals(292, layout.outerView.actualSize.y)
    }

    @Test
    fun `スクロール領域は検索欄と空白を引いた残りを受け取るのだ～🌱`() {
        val layout = buildRoot(1000)
        // 画面の300から、根の余白8と外枠の余白14と検索欄の20と空白の2を引くのだ～🌱
        assertEquals(256, layout.scrollView.actualSize.y)
        assertEquals(278, layout.innerView.actualSize.y)
    }

    @Test
    fun `中身が短くてもスクロール領域は残りいっぱいに広がるのだ～🌱`() {
        val shortLayout = buildRoot(10)
        val longLayout = buildRoot(1000)
        assertEquals(longLayout.scrollView.actualSize.y, shortLayout.scrollView.actualSize.y)
    }

    @Test
    fun `中身が溢れた分だけスクロールできるのだ～🌱`() {
        val layout = buildRoot(1000)
        // スクロールの中身の1000から、スクロール領域の256を引くのだ～🌱
        assertEquals(744, layout.scrollView.maxScrollAmount)
    }

    @Test
    fun `中身が収まるならスクロールできないのだ～🌱`() {
        val layout = buildRoot(10)
        assertEquals(0, layout.scrollView.maxScrollAmount)
    }

    @Test
    fun `スクロールの中身はスクロールバーの幅だけ狭まるのだ～🌱`() {
        val layout = buildRoot(1000)
        // 内枠の240から、スクロールバーの5を引くのだ～🌱
        assertEquals(235, (layout.scrollView.childView as MarginView).childView.actualSize.x)
    }

}
