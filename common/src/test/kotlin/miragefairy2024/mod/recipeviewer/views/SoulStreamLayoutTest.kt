package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `SoulStreamScreen.kt`の画面の骨組みが、既にあるViewで組めるかを確認するのだ～🌱
 * スロットとボタンは、同じ寸法を占めるSpaceViewで代えているのだ～🌱
 */
class SoulStreamLayoutTest {

    /** 文字の高さを9として、横幅は寸法へ混ぜないのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = 0
        override fun getTextHeight() = TEXT_HEIGHT
        override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
    }

    private val text: Component = Component.literal("")

    private class Layout(
        val leftPaneView: YListView,
        val mainPaneView: YListView,
        val rightPaneView: YListView,
        val fairySlotRowView: XListView,
        val soulStreamScrollView: ScrollView,
        val filterScrollView: ScrollView,
    )

    /** スロットを横に並べた1行なのだ～🌱 */
    private fun slotRowView(count: Int) = XListView().also { listView ->
        repeat(count) {
            listView += SpaceView(IntPoint(SLOT_SIZE, SLOT_SIZE))
        }
    }

    /** ツールチップの背景を敷いて、中へスクロールを置くのだ～🌱 */
    private fun tooltipScrollAreaView(scrollContentSizeY: Int): Pair<StackView, ScrollView> {
        val scrollContentView = YListView().also { it.sizingX = Sizing.FILL }
        scrollContentView += SpaceView(IntPoint(0, scrollContentSizeY))

        val scrollView = ScrollView(SIDE_SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SIDE_SCROLLBAR_ARGB))
        scrollView += Child(Unit, scrollContentView).margin(0, SIDE_SCROLLBAR_THICKNESS, 0, 0)

        val areaView = StackView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        areaView += TooltipBackgroundView()
        areaView += Child(Unit, scrollView).margin(TOOLTIP_MARGIN)
        return Pair(areaView, scrollView)
    }

    /** 実際の画面と同じ入れ子を組むのだ～🌱 */
    private fun buildRoot(soulStreamRowCount: Int): Layout {
        // 左の欄なのだ～🌱
        val filterButtonRowView = XListView().also { it.sizingX = Sizing.FILL }
        filterButtonRowView += SpaceView(IntPoint(BUTTON_SIZE, BUTTON_SIZE))
        filterButtonRowView += XSpaceView(SIDE_PANE_GAP)
        filterButtonRowView += SpaceView(IntPoint(BUTTON_SIZE, BUTTON_SIZE))

        val (filterAreaView, filterScrollView) = tooltipScrollAreaView(1000)

        val leftPaneView = YListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        leftPaneView += filterButtonRowView
        leftPaneView += YSpaceView(SIDE_PANE_GAP)
        leftPaneView += Child(leftPaneView.createDefaultPosition(), filterAreaView).also { it.position.weight = 1.0 }

        // 真ん中の欄なのだ～🌱
        val fairySlotRowView = slotRowView(PLAYER_INVENTORY_WIDTH)

        val soulStreamContentView = YListView()
        repeat(soulStreamRowCount) {
            soulStreamContentView += slotRowView(PLAYER_INVENTORY_WIDTH)
        }
        val soulStreamScrollView = ScrollView(MAIN_SCROLLBAR_THICKNESS, ScrollbarStyle.Vanilla)
        soulStreamScrollView += Child(Unit, soulStreamContentView).margin(0, MAIN_SCROLLBAR_THICKNESS, 0, 0)

        val mainPaneView = YListView().also { it.sizingY = Sizing.FILL }
        mainPaneView += fairySlotRowView
        mainPaneView += YSpaceView(INVENTORY_SPACE)
        mainPaneView += Child(mainPaneView.createDefaultPosition(), soulStreamScrollView).also { it.position.weight = 1.0 }
        mainPaneView += YSpaceView(INVENTORY_SPACE)
        repeat(PLAYER_INVENTORY_HEIGHT) {
            mainPaneView += slotRowView(PLAYER_INVENTORY_WIDTH)
        }
        mainPaneView += YSpaceView(INVENTORY_SPACE)
        mainPaneView += slotRowView(PLAYER_INVENTORY_WIDTH)
        mainPaneView += YSpaceView(TITLE_SPACE)
        mainPaneView += Child(mainPaneView.createDefaultPosition(), TextView(text).also { it.sizingX = Sizing.FILL })
            .also { it.position.ignoreLayoutX = true }
            .margin(INVENTORY_NAME_MARGIN_X, 0)
        mainPaneView += YSpaceView(TITLE_SPACE)

        // 右の欄なのだ～🌱
        val effectButtonRowView = XListView().also { it.sizingX = Sizing.FILL }
        effectButtonRowView += SpaceView(IntPoint(BUTTON_SIZE, BUTTON_SIZE))

        val (effectAreaView, _) = tooltipScrollAreaView(1000)

        val rightPaneView = YListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        rightPaneView += effectButtonRowView
        rightPaneView += YSpaceView(SIDE_PANE_GAP)
        rightPaneView += Child(rightPaneView.createDefaultPosition(), effectAreaView).also { it.position.weight = 1.0 }

        // 3個の欄を横に並べるのだ～🌱
        val paneRowView = XListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        paneRowView += Child(paneRowView.createDefaultPosition(), leftPaneView).margin(0, 0, 0, RECIPE_VIEWER_MARGIN).also {
            it.position.ignoreLayoutX = true
            it.position.weight = 1.0
        }
        paneRowView += XSpaceView(PANE_GAP)
        paneRowView += Child(paneRowView.createDefaultPosition(), mainPaneView).minContentSizeX(MAIN_WIDTH).maxContentSizeX(MAIN_WIDTH)
        paneRowView += XSpaceView(PANE_GAP)
        paneRowView += Child(paneRowView.createDefaultPosition(), rightPaneView).margin(0, 0, 0, RECIPE_VIEWER_MARGIN).also {
            it.position.ignoreLayoutX = true
            it.position.weight = 1.0
        }

        val rootView = StackView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        rootView += FilledRectangleView()
        rootView += Child(Unit, paneRowView).margin(SCREEN_MARGIN)

        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(SCREEN_SIZE)
        return Layout(leftPaneView, mainPaneView, rightPaneView, fairySlotRowView, soulStreamScrollView, filterScrollView)
    }

    @Test
    fun `真ん中の欄は持ち物の1行とスクロールバーを合わせた横幅になるのだ～🌱`() {
        val layout = buildRoot(10)
        // スロット9個の162に、スクロールバーの18を足すのだ～🌱
        assertEquals(180, layout.mainPaneView.actualSize.x)
    }

    @Test
    fun `左右の欄は真ん中の欄を除いた余りを半分ずつ分けるのだ～🌱`() {
        val layout = buildRoot(10)
        // 画面の400から、根の余白8と欄の間隔4ずつと真ん中の180を引いて、半分ずつ分けるのだ～🌱
        assertEquals(102, layout.leftPaneView.actualSize.x)
        assertEquals(102, layout.rightPaneView.actualSize.x)
    }

    @Test
    fun `妖精の枠の1行はスロット9個の横幅になるのだ～🌱`() {
        val layout = buildRoot(10)
        assertEquals(162, layout.fairySlotRowView.actualSize.x)
        assertEquals(SLOT_SIZE, layout.fairySlotRowView.actualSize.y)
    }

    @Test
    fun `左右の欄の縦は画面からレシピビューワー向けの余白を引いた高さになるのだ～🌱`() {
        val layout = buildRoot(10)
        // 画面の300から、根の余白8と、下に残す20を引くのだ～🌱
        assertEquals(272, layout.leftPaneView.actualSize.y)
        assertEquals(272, layout.rightPaneView.actualSize.y)
    }

    @Test
    fun `妖精を預ける欄は真ん中の欄の余りを受け取るのだ～🌱`() {
        val layout = buildRoot(10)
        // 画面の300から根の余白8を引いた292から、
        // 妖精の枠の18と、間隔4が3個と、持ち物の54と、ホットバーの18と、題名の空白5が2個と、文字の9を引くのだ～🌱
        assertEquals(171, layout.soulStreamScrollView.actualSize.y)
    }

    @Test
    fun `妖精を預ける欄の中身はスクロールバーの分だけ狭まるのだ～🌱`() {
        val layout = buildRoot(10)
        // 真ん中の欄の180から、スクロールバーの18を引くのだ～🌱
        assertEquals(162, (layout.soulStreamScrollView.childView as MarginView).childView.actualSize.x)
    }

    @Test
    fun `妖精を預ける欄は行の分だけスクロールできるのだ～🌱`() {
        val layout = buildRoot(10)
        // 行が10個で縦が180になるため、領域の171を超えた分だけ動くのだ～🌱
        assertEquals(9, layout.soulStreamScrollView.maxScrollAmount)
    }

    @Test
    fun `妖精を預ける欄に行が収まるならスクロールできないのだ～🌱`() {
        val layout = buildRoot(2)
        assertEquals(0, layout.soulStreamScrollView.maxScrollAmount)
    }

    @Test
    fun `探す条件の欄はボタンと空白を引いた残りを受け取るのだ～🌱`() {
        val layout = buildRoot(10)
        // 左の欄の272から、ボタンの14と、空白の2と、ツールチップの余白4ずつを引くのだ～🌱
        assertEquals(248, layout.filterScrollView.actualSize.y)
    }

    companion object {
        private val SCREEN_SIZE = IntPoint(400, 300)
        private const val TEXT_HEIGHT = 9
        private const val SLOT_SIZE = 18
        private const val SCREEN_MARGIN = 4
        private const val PANE_GAP = 4
        private const val SIDE_PANE_GAP = 2
        private const val RECIPE_VIEWER_MARGIN = 20
        private const val MAIN_WIDTH = SLOT_SIZE * 9 + SLOT_SIZE
        private const val MAIN_SCROLLBAR_THICKNESS = SLOT_SIZE
        private const val SIDE_SCROLLBAR_THICKNESS = 5
        private const val SIDE_SCROLLBAR_ARGB = 0xA0FFFFFF.toInt()
        private const val BUTTON_SIZE = 14
        private const val TOOLTIP_MARGIN = 4
        private const val INVENTORY_NAME_MARGIN_X = 1
        private const val INVENTORY_SPACE = 4
        private const val TITLE_SPACE = 5
        private const val PLAYER_INVENTORY_WIDTH = 9
        private const val PLAYER_INVENTORY_HEIGHT = 3
    }

}
