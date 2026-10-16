package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals

class WrappedTextViewTest {

    /** 1行の高さが10で、幅20ごとに1行へ折り返す文字を模すのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = 0
        override fun getTextHeight() = 10
        override fun wrapText(text: FormattedText, maxWidth: Int) = List(TOTAL_TEXT_WIDTH / maxWidth) { FormattedCharSequence.EMPTY }
    }

    private val text: Component = Component.literal("")

    private fun createWrappedTextView(regionSize: IntPoint): WrappedTextView {
        val wrappedTextView = WrappedTextView(text)
        wrappedTextView.calculateContentSize(renderingProxy)
        wrappedTextView.calculateActualSize(regionSize)
        return wrappedTextView
    }

    @Test
    fun `折り返す幅が決まっていないため親へ要求する寸法は0なのだ～🌱`() {
        val wrappedTextView = WrappedTextView(text)
        wrappedTextView.calculateContentSize(renderingProxy)
        assertEquals(IntPoint.ZERO, wrappedTextView.contentSize)
    }

    @Test
    fun `折り返した行数ぶんの高さになるのだ～🌱`() {
        // 幅20なら10行で、1行の高さが10なのだ～🌱
        assertEquals(100, createWrappedTextView(IntPoint(20, 0)).actualSize.y)
    }

    @Test
    fun `幅が広いほど行数が減って高さも減るのだ～🌱`() {
        // 幅50なら4行なのだ～🌱
        assertEquals(40, createWrappedTextView(IntPoint(50, 0)).actualSize.y)
    }

    @Test
    fun `幅は親が与えたものになるのだ～🌱`() {
        assertEquals(50, createWrappedTextView(IntPoint(50, 0)).actualSize.x)
    }

    @Test
    fun `スクロールの中では折り返した分だけスクロールできるのだ～🌱`() {
        val scrollView = ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += WrappedTextView(text)
        scrollView.calculateContentSize(renderingProxy)
        scrollView.calculateActualSize(IntPoint(20, 30))
        // 幅20で10行ぶんの100から、スクロール領域の30を引くのだ～🌱
        assertEquals(70, scrollView.maxScrollAmount)
    }

    companion object {
        /** 折り返す前の文字全体の幅なのだ～🌱 */
        private const val TOTAL_TEXT_WIDTH = 200
    }

}
