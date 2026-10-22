package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.SpaceView
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** 文字の寸法を測らないViewだけを扱うため、どの関数も呼ばれないのだ～🌱 */
private val TEST_RENDERING_PROXY = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

class SpaceViewTest {

    @Test
    fun `空間のViewの寸法が指定の通りになるのだ`() {
        val view = SpaceView(IntPoint(10, 20))
        view.calculateContentSize(TEST_RENDERING_PROXY)
        view.calculateActualSize(IntPoint(100, 100))
        assertEquals(IntPoint(10, 20), view.actualSize)
    }

    @Test
    fun `空間のViewは親の空間が狭くても縮まないのだ`() {
        val view = SpaceView(IntPoint(10, 20))
        view.calculateContentSize(TEST_RENDERING_PROXY)
        view.calculateActualSize(IntPoint(5, 5))
        assertEquals(IntPoint(10, 20), view.actualSize)
    }

}
