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
 * `FairyQuestCardScreen.kt`の画面の骨組みが、既にあるViewで組めるかを確認するのだ～🌱
 * スロットは、同じ寸法を占めるSpaceViewで代えているのだ～🌱
 */
class FairyQuestCardLayoutTest {

    /** 1行の高さが10で、幅20ごとに1行へ折り返す文字を模すのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = 0
        override fun getTextHeight() = 10
        override fun wrapText(text: FormattedText, maxWidth: Int) = List(TOTAL_TEXT_WIDTH / maxWidth) { FormattedCharSequence.EMPTY }
    }

    private val text: Component = Component.literal("")

    private class Layout(val rootView: YListView, val messageAreaView: StackView, val scrollView: ScrollView, val messageTextAreaView: StackView, val tradeRowView: XListView)

    /** 実際の画面と同じ入れ子を組むのだ～🌱 */
    private fun buildRoot(): Layout {
        // 依頼文の欄なのだ～🌱
        val lineBackgroundView = StackView().also { it.sizingX = Sizing.FILL }
        lineBackgroundView += NinePatchImageView(DUMMY_TEXTURE, 0, LINE_HEIGHT, 0, 0, LINE_HEIGHT, 0)
        lineBackgroundView += Child(Unit, MultiLineTextView(text)).margin(0, 0, 0, LINE_BOTTOM_MARGIN)

        val scrollView = ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(0xA0000000.toInt()))
        scrollView += Child(Unit, lineBackgroundView).margin(0, SCROLLBAR_THICKNESS, 0, 0)

        val messageAreaView = StackView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        messageAreaView += NinePatchImageView(DUMMY_TEXTURE, 22, 22, 22, 22, 22, 22)
        messageAreaView += Child(Unit, scrollView).margin(CARD_MARGIN, CARD_MARGIN_RIGHT, CARD_MARGIN, CARD_MARGIN)

        // 取引欄なのだ～🌱 スロット8個と進捗で、横が埋まるのだ～🌱
        val tradeRowView = XListView().also { it.sizingX = Sizing.FILL }
        repeat(TRADE_SLOT_COUNT * 2) {
            tradeRowView += SpaceView(IntPoint(SLOT_SIZE, SLOT_SIZE))
        }
        val progressView = StackView()
        progressView += SpaceView(IntPoint(PROGRESS_SIZE, PROGRESS_SIZE))
        tradeRowView += Child(tradeRowView.createDefaultPosition(), progressView).margin(1)

        // 内枠なのだ～🌱
        val innerView = YListView().also { it.sizingX = Sizing.FILL }
        innerView += TextView(text).also { it.sizingX = Sizing.FILL }.let { Child(innerView.createDefaultPosition(), it).also { child -> child.position.ignoreLayoutX = true } }
        innerView += YSpaceView(INVENTORY_NAME_SPACE)
        innerView += Child(innerView.createDefaultPosition(), messageAreaView).minContentSizeY(MESSAGE_HEIGHT).maxContentSizeY(MESSAGE_HEIGHT)
        innerView += YSpaceView(INVENTORY_NAME_SPACE)
        innerView += tradeRowView

        // 外枠なのだ～🌱
        val cardView = StackView()
        cardView += NinePatchImageView(DUMMY_TEXTURE, 5, 6, 5, 5, 6, 5)
        cardView += Child(Unit, innerView).minContentSizeX(MAIN_WIDTH).margin(PANEL_MARGIN)

        val rootView = YListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        rootView += Child(rootView.createDefaultPosition(), cardView).also {
            it.position.alignmentX = Alignment.CENTER
            it.position.alignmentY = Alignment.CENTER
            it.position.weight = 1.0
        }

        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(SCREEN_SIZE)
        return Layout(rootView, messageAreaView, scrollView, lineBackgroundView, tradeRowView)
    }

    @Test
    fun `内枠の横幅は持ち物の1行の分なのだ～🌱`() {
        assertEquals(MAIN_WIDTH, buildRoot().tradeRowView.actualSize.x)
    }

    @Test
    fun `取引欄はスロット8個と進捗で横がぴったり埋まるのだ～🌱`() {
        // スロットが18ずつ8個と、進捗の18を足すと、内枠の162になるのだ～🌱
        assertEquals(MAIN_WIDTH, SLOT_SIZE * TRADE_SLOT_COUNT * 2 + PROGRESS_SIZE + 2)
        assertEquals(MAIN_WIDTH, buildRoot().tradeRowView.contentSize.x)
    }

    @Test
    fun `依頼文の欄の縦は移植元と同じ値に固定されるのだ～🌱`() {
        assertEquals(MESSAGE_HEIGHT, buildRoot().messageAreaView.actualSize.y)
    }

    @Test
    fun `スクロールの領域は依頼文の欄から内側の余白を除いた分なのだ～🌱`() {
        assertEquals(MESSAGE_HEIGHT - CARD_MARGIN * 2, buildRoot().scrollView.actualSize.y)
    }

    @Test
    fun `本文はスクロールバーへ重ならないのだ～🌱`() {
        val layout = buildRoot()
        // スクロールの幅は、内枠162から左の余白11と右の6を引いた145なのだ～🌱
        assertEquals(MAIN_WIDTH - CARD_MARGIN - CARD_MARGIN_RIGHT, layout.scrollView.actualSize.x)
        // 本文は、そこから更にスクロールバーの5を引いた140なのだ～🌱
        assertEquals(MAIN_WIDTH - CARD_MARGIN - CARD_MARGIN_RIGHT - SCROLLBAR_THICKNESS, layout.messageTextAreaView.actualSize.x)
    }

    companion object {
        private val SCREEN_SIZE = IntPoint(400, 300)
        private const val MAIN_WIDTH = 18 * 9
        private const val PANEL_MARGIN = 7
        private const val INVENTORY_NAME_SPACE = 3
        private const val MESSAGE_HEIGHT = 11 * 7 + 5
        private const val CARD_MARGIN = 11
        private const val CARD_MARGIN_RIGHT = 6
        private const val SCROLLBAR_THICKNESS = 5
        private const val LINE_HEIGHT = 11
        private const val LINE_BOTTOM_MARGIN = 1
        private const val TRADE_SLOT_COUNT = 4
        private const val SLOT_SIZE = 18
        private const val PROGRESS_SIZE = 16

        /** 折り返す前の文字全体の幅なのだ～🌱 本文の幅で1行に収まる長さなのだ～🌱 */
        private const val TOTAL_TEXT_WIDTH = 140

        private val DUMMY_TEXTURE = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("miragefairy2024", "textures/gui/dummy.png")
    }

}
