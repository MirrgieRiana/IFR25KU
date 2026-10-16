package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.Remover
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `BagClientModule.kt`の画面の骨組みが、既にあるViewで組めるかを確認するのだ～🌱
 * スロットと持ち物の名前は、同じ寸法を占める別のViewで代えているのだ～🌱
 */
class BagLayoutTest {

    /** 寸法の計算だけを試すため、文字の計測は呼ばれないのだ～🌱 */
    private val renderingProxy = object : RenderingProxy {
        override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
        override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
        override fun getTextHeight() = throw UnsupportedOperationException()
        override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
    }

    private val screenSize = IntPoint(400, 300)

    private val slotSize = 18

    private val playerInventoryWidth = 9

    private val playerInventoryHeight = 3

    private val panelMargin = 7

    private val textHeight = 9

    /** 枠の横幅を超える長さの名前で、名前が枠を広げないことを試すのだ～🌱 */
    private val inventoryNameSizeX = 500

    private class Layout(val rootView: View, val panelView: StackView, val innerView: YListView)

    /** 実際の画面と同じ入れ子を組むのだ～🌱 */
    private fun buildRoot(bagWidth: Int, bagHeight: Int): Layout {
        /** スロットを横に並べて、中央へ寄せた1行なのだ～🌱 */
        fun slotRowView(count: Int): XListView {
            val rowView = XListView().also { it.sizingX = Sizing.FILL }
            rowView += Child(rowView.createDefaultPosition(), SpaceView()).also { it.position.weight = 1.0 }
            repeat(count) {
                rowView += Child(rowView.createDefaultPosition(), FilledRectangleView()).minContentSizeX(slotSize).minContentSizeY(slotSize)
            }
            rowView += Child(rowView.createDefaultPosition(), SpaceView()).also { it.position.weight = 1.0 }
            return rowView
        }

        // 内枠なのだ～🌱
        val innerView = YListView().also { it.sizingX = Sizing.FILL }
        innerView += Child(innerView.createDefaultPosition(), SpaceView(IntPoint(inventoryNameSizeX, textHeight))).also { it.position.ignoreLayoutX = true }.margin(1, 0)
        innerView += Child(innerView.createDefaultPosition(), YSpaceView(3))
        repeat(bagHeight) {
            innerView += Child(innerView.createDefaultPosition(), slotRowView(bagWidth))
        }
        innerView += Child(innerView.createDefaultPosition(), YSpaceView(3))
        innerView += Child(innerView.createDefaultPosition(), SpaceView(IntPoint(inventoryNameSizeX, textHeight))).also { it.position.ignoreLayoutX = true }.margin(1, 0)
        innerView += Child(innerView.createDefaultPosition(), YSpaceView(1))
        repeat(playerInventoryHeight) {
            innerView += Child(innerView.createDefaultPosition(), slotRowView(playerInventoryWidth))
        }
        innerView += Child(innerView.createDefaultPosition(), YSpaceView(4))
        innerView += Child(innerView.createDefaultPosition(), slotRowView(playerInventoryWidth))

        // 外枠なのだ～🌱 背景の九分割はFILLで広がるため、寸法には寄与しないのだ～🌱
        val panelView = StackView()
        panelView += Child(panelView.createDefaultPosition(), innerView).minContentSizeX(slotSize * (if (bagWidth > playerInventoryWidth) bagWidth else playerInventoryWidth)).margin(panelMargin)

        val rootView = YListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
        }
        rootView += Child(rootView.createDefaultPosition(), panelView).also {
            it.position.alignmentX = Alignment.CENTER
            it.position.alignmentY = Alignment.CENTER
            it.position.weight = 1.0
        }

        rootView.calculateContentSize(renderingProxy)
        rootView.calculateActualSize(screenSize)

        return Layout(rootView, panelView, innerView)
    }

    /** カバンが持ち物より狭い場合、枠の横幅は持ち物の側で決まるのだ～🌱 */
    @Test
    fun narrowBagPanelSizeX() {
        val layout = buildRoot(5, 2)
        assertEquals(slotSize * playerInventoryWidth, layout.innerView.actualSize.x)
        assertEquals(slotSize * playerInventoryWidth + panelMargin * 2, layout.panelView.actualSize.x)
    }

    /** カバンが持ち物より広い場合、枠の横幅はカバンの側で決まるのだ～🌱 */
    @Test
    fun wideBagPanelSizeX() {
        val layout = buildRoot(13, 2)
        assertEquals(slotSize * 13, layout.innerView.actualSize.x)
        assertEquals(slotSize * 13 + panelMargin * 2, layout.panelView.actualSize.x)
    }

    /** 枠の縦幅は、名前と空きとスロットの段を積み上げた値なのだ～🌱 */
    @Test
    fun panelSizeY() {
        val layout = buildRoot(5, 2)
        val expectedInnerSizeY = textHeight + 3 + slotSize * 2 + 3 + textHeight + 1 + slotSize * playerInventoryHeight + 4 + slotSize
        assertEquals(expectedInnerSizeY, layout.innerView.actualSize.y)
        assertEquals(expectedInnerSizeY + panelMargin * 2, layout.panelView.actualSize.y)
    }

    /** 画面の中で、枠は横も縦も中央へ寄るのだ～🌱 */
    @Test
    fun panelIsCentered() {
        val layout = buildRoot(5, 2)
        val slots = collectPlacedSlots(layout.rootView)
        val panelSize = layout.panelView.actualSize
        assertEquals((screenSize.x - panelSize.x) / 2 + panelMargin, slots.minOf { it.x })
        assertEquals((screenSize.y - panelSize.y) / 2 + panelMargin + textHeight + 3, slots.minOf { it.y })
    }

    /** カバンが持ち物より狭い場合、カバンのスロットは枠の中央へ寄るのだ～🌱 */
    @Test
    fun narrowBagRowIsCentered() {
        val layout = buildRoot(5, 2)
        val slots = collectPlacedSlots(layout.rootView)
        val bagRowY = slots.minOf { it.y }
        val bagRowSlots = slots.filter { it.y == bagRowY }.sortedBy { it.x }
        assertEquals(5, bagRowSlots.size)
        // 行の横幅が162で、スロットの合計が90であるため、左の空きは36なのだ～🌱
        val leftSpace = (slotSize * playerInventoryWidth - slotSize * 5) / 2
        val innerLeft = (screenSize.x - layout.panelView.actualSize.x) / 2 + panelMargin
        assertEquals(innerLeft + leftSpace, bagRowSlots[0].x)
    }

    /** 置かれたスロットの矩形を集めるのだ～🌱 */
    private fun collectPlacedSlots(view: View): List<IntRectangle> {
        val rectangles = mutableListOf<IntRectangle>()
        view.attachTo(IntPoint.ZERO) { _, bounds ->
            rectangles += bounds
            Remover.NONE
        }
        return rectangles
    }

}
