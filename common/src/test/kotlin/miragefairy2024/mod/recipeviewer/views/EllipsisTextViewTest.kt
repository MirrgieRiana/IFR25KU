package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals

class EllipsisTextViewTest {

    /** 1行の高さが10で、与えられた幅に応じて行数が変わる文字を模すのだ～🌱 */
    private fun renderingProxy(lineCount: Int) = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = 0
        override fun getTextHeight() = TEXT_HEIGHT
        override fun wrapText(text: FormattedText, maxWidth: Int) = List(lineCount) { FormattedCharSequence.EMPTY }
    }

    private val text: Component = Component.literal("")

    private fun buildView(lineCount: Int): EllipsisTextView {
        return EllipsisTextView(text).also {
            it.calculateContentSize(renderingProxy(lineCount))
            it.calculateActualSize(IntPoint(REGION_SIZE_X, 0))
        }
    }

    @Test
    fun `折り返す幅が決まっていない段では、横幅を出さないのだ～🌱`() {
        val view = buildView(3)
        assertEquals(0, view.contentSize.x)
    }

    @Test
    fun `高さは、行数にかかわらず1行の分なのだ～🌱`() {
        assertEquals(TEXT_HEIGHT, buildView(1).contentSize.y)
        assertEquals(TEXT_HEIGHT, buildView(5).contentSize.y)
        assertEquals(TEXT_HEIGHT, buildView(1).actualSize.y)
        assertEquals(TEXT_HEIGHT, buildView(5).actualSize.y)
    }

    @Test
    fun `横幅は、親が与えた幅をそのまま使うのだ～🌱`() {
        assertEquals(REGION_SIZE_X, buildView(1).actualSize.x)
        assertEquals(REGION_SIZE_X, buildView(5).actualSize.x)
    }

    @Test
    fun `1行も無い場合でも、高さは1行の分なのだ～🌱`() {
        assertEquals(TEXT_HEIGHT, buildView(0).actualSize.y)
    }

    companion object {
        private const val TEXT_HEIGHT = 10
        private const val REGION_SIZE_X = 112
    }

}
