package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiLineTextViewTest {

    /** 1行の高さが10で、幅20ごとに1行へ折り返す文字を模すのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = 0
        override fun getTextHeight() = 10
        override fun wrapText(text: FormattedText, maxWidth: Int) = List(TOTAL_TEXT_WIDTH / maxWidth) { FormattedCharSequence.EMPTY }
    }

    private val text: Component = Component.literal("")

    private fun createMultiLineTextView(regionSize: IntPoint): MultiLineTextView {
        val multiLineTextView = MultiLineTextView(text)
        multiLineTextView.calculateContentSize(renderingProxy)
        multiLineTextView.calculateActualSize(regionSize)
        return multiLineTextView
    }

    @Test
    fun `折り返す幅が決まっていないため親へ要求する寸法は0なのだ～🌱`() {
        val multiLineTextView = MultiLineTextView(text)
        multiLineTextView.calculateContentSize(renderingProxy)
        assertEquals(IntPoint.ZERO, multiLineTextView.contentSize)
    }

    @Test
    fun `折り返した行数ぶんの高さになるのだ～🌱`() {
        // 幅20なら10行で、1行の高さが10なのだ～🌱
        assertEquals(100, createMultiLineTextView(IntPoint(20, 0)).actualSize.y)
    }

    @Test
    fun `幅が広いほど行数が減って高さも減るのだ～🌱`() {
        // 幅50なら4行なのだ～🌱
        assertEquals(40, createMultiLineTextView(IntPoint(50, 0)).actualSize.y)
    }

    @Test
    fun `幅は親が与えたものになるのだ～🌱`() {
        assertEquals(50, createMultiLineTextView(IntPoint(50, 0)).actualSize.x)
    }

    @Test
    fun `スクロールの中では折り返した分だけスクロールできるのだ～🌱`() {
        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += MultiLineTextView(text)
        scrollView.calculateContentSize(renderingProxy)
        scrollView.calculateActualSize(IntPoint(20, 30))
        // 幅20で10行ぶんの100から、スクロール領域の30を引くのだ～🌱
        assertEquals(70, scrollView.maxScrollAmount)
    }

    @Test
    fun `スクロールとの間に別のViewを挟んでも折り返した分の高さが伝わるのだ～🌱`() {
        val innerView = YListView().also { it.sizingX = Sizing.FILL }
        innerView += MultiLineTextView(text)
        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += innerView
        scrollView.calculateContentSize(renderingProxy)
        scrollView.calculateActualSize(IntPoint(20, 30))
        assertEquals(70, scrollView.maxScrollAmount)
    }

    @Test
    fun `余白のViewを挟むと折り返した高さに余白の分が加わるのだ～🌱`() {
        val innerView = YListView().also { it.sizingX = Sizing.FILL }
        innerView += MultiLineTextView(text)
        val marginView = MarginView(0, 0, 3, 4)
        marginView += innerView
        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += marginView
        scrollView.calculateContentSize(renderingProxy)
        scrollView.calculateActualSize(IntPoint(20, 30))
        // 幅20で10行ぶんの100に、上下の余白の7を足して、スクロール領域の30を引くのだ～🌱
        assertEquals(77, scrollView.maxScrollAmount)
    }

    @Test
    fun `折り返すViewの後ろに並ぶViewは折り返した分だけ下へ送られるのだ～🌱`() {
        val innerView = YListView().also { it.sizingX = Sizing.FILL }
        innerView += MultiLineTextView(text)
        innerView += YSpaceView(13)
        innerView.calculateContentSize(renderingProxy)
        innerView.calculateActualSize(IntPoint(20, 0))
        // 幅20で10行ぶんの100に、後ろに並ぶ13を足すのだ～🌱
        assertEquals(113, innerView.actualSize.y)
    }

    @Test
    fun `縦の上限を掛けたViewは折り返した分が伸びても上限で止まるのだ～🌱`() {
        val maxContentSizeView = MaxContentSizeYView(45)
        maxContentSizeView += MultiLineTextView(text)
        maxContentSizeView.calculateContentSize(renderingProxy)
        maxContentSizeView.calculateActualSize(IntPoint(20, 0))
        assertEquals(45, maxContentSizeView.actualSize.y)
    }

    @Test
    fun `行間のギャップを設定すると行の間だけ空くのだ～🌱`() {
        val multiLineTextView = MultiLineTextView(text).also { it.gap = 2 }
        multiLineTextView.calculateContentSize(renderingProxy)
        multiLineTextView.calculateActualSize(IntPoint(20, 0))
        // 幅20で10行ぶんの100に、行の間の9個のギャップの18を足すのだ～🌱
        assertEquals(118, multiLineTextView.actualSize.y)
    }

    @Test
    fun `行が1個ならギャップは入らないのだ～🌱`() {
        val multiLineTextView = MultiLineTextView(text).also { it.gap = 2 }
        multiLineTextView.calculateContentSize(renderingProxy)
        multiLineTextView.calculateActualSize(IntPoint(TOTAL_TEXT_WIDTH, 0))
        assertEquals(10, multiLineTextView.actualSize.y)
    }

    companion object {
        /** 折り返す前の文字全体の幅なのだ～🌱 */
        private const val TOTAL_TEXT_WIDTH = 200
    }

}
