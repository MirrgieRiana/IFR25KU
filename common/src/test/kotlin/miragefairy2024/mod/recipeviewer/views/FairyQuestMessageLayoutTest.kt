package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `FairyQuestMessageScreen.kt`の画面の骨組みが、既にあるViewで組めるかを確認するのだ～🌱
 * 折り返す文字は、幅に応じて行数が変わる模擬で代えているのだ～🌱
 */
class FairyQuestMessageLayoutTest {

    /** 1行の高さが10で、幅1あたり1の文字が並ぶのを模すのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = 0
        override fun getTextHeight() = 10
        override fun wrapText(text: FormattedText, maxWidth: Int) = List(TOTAL_TEXT_WIDTH / maxWidth) { FormattedCharSequence.EMPTY }
    }

    private val text: Component = Component.literal("")

    private class Layout(val rootView: YListView, val scrollView: ScrollView, val cardContentView: YListView)

    /** 実際の画面と同じ入れ子を組むのだ～🌱 */
    private fun buildRoot(): Layout {
        // カードの中身なのだ～🌱
        val cardContentView = YListView().also { it.sizingX = Sizing.FILL }
        cardContentView += WrappedTextView(text).also { it.alignmentX = Alignment.CENTER }
        cardContentView += YSpaceView(CARD_MARGIN)
        val messageBackgroundView = StackView().also { it.sizingX = Sizing.FILL }
        messageBackgroundView += NinePatchImageView(DUMMY_TEXTURE, 0, LINE_HEIGHT, 0, 0, LINE_HEIGHT, 0)
        messageBackgroundView += Child(Unit, WrappedTextView(text)).margin(0, 0, 0, LINE_BOTTOM_MARGIN)
        cardContentView += messageBackgroundView
        cardContentView += YSpaceView(CARD_MARGIN)
        cardContentView += WrappedTextView(text).also { it.alignmentX = Alignment.END }

        // カードの外枠なのだ～🌱
        val cardView = StackView()
        cardView += NinePatchImageView(DUMMY_TEXTURE, 22, 22, 22, 22, 22, 22)
        cardView += Child(Unit, cardContentView).minContentSizeX(CARD_WIDTH - CARD_MARGIN * 2).margin(CARD_MARGIN)

        val scrollView = ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(0xA0FFFFFF.toInt()))
        scrollView += Child(Unit, cardView).margin(0, SCROLLBAR_THICKNESS, 0, 0)

        // ボタン列なのだ～🌱
        val buttonRowView = XListView()
        buttonRowView += SpaceView(BUTTON_SIZE)
        buttonRowView += XSpaceView(BUTTON_SPACE)
        buttonRowView += SpaceView(BUTTON_SIZE)

        val rootView = YListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        rootView += Child(rootView.createDefaultPosition(), scrollView).margin(SCROLLBAR_THICKNESS, 0, ROOT_MARGIN_TOP, ROOT_MARGIN_BOTTOM).also {
            it.position.alignmentX = Alignment.CENTER
            it.position.weight = 1.0
        }
        rootView += YSpaceView(CARD_TO_BUTTON_SPACE)
        rootView += Child(rootView.createDefaultPosition(), buttonRowView).also { it.position.alignmentX = Alignment.CENTER }

        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(SCREEN_SIZE)
        return Layout(rootView, scrollView, cardContentView)
    }

    @Test
    fun `カードと左右のスクロールバーを合わせた横幅は移植元と同じなのだ～🌱`() {
        assertEquals(18 * 15, buildRoot().scrollView.actualSize.x + SCROLLBAR_THICKNESS)
    }

    @Test
    fun `カードの横幅はスクロールバーの分を除いた値なのだ～🌱`() {
        assertEquals(CARD_WIDTH, buildRoot().scrollView.actualSize.x - SCROLLBAR_THICKNESS)
    }

    @Test
    fun `カードの中身の横幅は内側の余白を除いた値なのだ～🌱`() {
        assertEquals(CARD_WIDTH - CARD_MARGIN * 2, buildRoot().cardContentView.actualSize.x)
    }

    @Test
    fun `カードは画面の縦からボタン列と余白を除いた分だけ伸びるのだ～🌱`() {
        // 画面の300から、上下の余白の14と、ボタン列までの10と、ボタンの20を引くのだ～🌱
        assertEquals(256, buildRoot().scrollView.actualSize.y)
    }

    @Test
    fun `折り返した3個の文字の高さが、カードの中身の高さへ伝わるのだ～🌱`() {
        // 幅228なら各1行で、1行の高さが10なのだ～🌱 上の余白1と、間の余白11が2個付くのだ～🌱
        assertEquals(10 + CARD_MARGIN + 1 + 10 + CARD_MARGIN + 10, buildRoot().cardContentView.actualSize.y)
    }

    @Test
    fun `カードの高さは中身に内側の余白を足した分なのだ～🌱`() {
        val layout = buildRoot()
        assertEquals(layout.cardContentView.actualSize.y + CARD_MARGIN * 2, layout.scrollView.childView.actualSize.y)
    }

    companion object {
        private val SCREEN_SIZE = IntPoint(400, 300)
        private const val SCROLLBAR_THICKNESS = 10
        private const val CARD_WIDTH = 18 * 15 - SCROLLBAR_THICKNESS * 2
        private const val CARD_MARGIN = 11
        private const val LINE_HEIGHT = 11
        private const val LINE_BOTTOM_MARGIN = 1
        private const val ROOT_MARGIN_TOP = 10
        private const val ROOT_MARGIN_BOTTOM = 4
        private const val CARD_TO_BUTTON_SPACE = 10
        private const val BUTTON_SPACE = 20
        private val BUTTON_SIZE = IntPoint(80, 20)

        /** 折り返す前の文字全体の幅なのだ～🌱 カードの中身の幅で1行に収まる長さなのだ～🌱 */
        private const val TOTAL_TEXT_WIDTH = 228

        private val DUMMY_TEXTURE = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("miragefairy2024", "textures/gui/dummy.png")
    }

}
