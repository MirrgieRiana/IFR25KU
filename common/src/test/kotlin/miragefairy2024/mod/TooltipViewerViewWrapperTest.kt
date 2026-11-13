package miragefairy2024.mod

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import net.minecraft.SharedConstants
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.server.Bootstrap
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private val TOOLTIP_VIEWER_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = 9
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> {
        return text.string.chunked(maxWidth / 6 atLeast 1).map { FormattedCharSequence.forward(it, Style.EMPTY) }
    }
}

private fun TooltipViewerViewWrapper.place(placements: MutableList<Pair<PlaceableView, IntRectangle>>): ViewController {
    fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
        val placement = Pair(view, bounds)
        placements += placement
        val controller = if (view is YScrollView) view.childView.attachTo(bounds.offset, ::place) else ViewController.NONE
        return object : ViewController by controller {
            override fun detach() {
                controller.detach()
                placements -= placement
            }
        }
    }
    return this.view.attachTo(IntPoint.ZERO, ::place)
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TooltipViewerViewWrapperTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `空のアイテムを除外しスロットとツールチップを同じ順序で配置するのだ`() {
        val first: ItemStack = ItemStack(Items.STONE, 64)
        val second: ItemStack = ItemStack(Items.DIRT, 12)
        val view = TooltipViewerViewWrapper(listOf(ItemStack.EMPTY, first, ItemStack.EMPTY, second), ObservableValue("")) {
            listOf(text { it.count.toString()() })
        }
        view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { 400 }, { 240 })
        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        val controller = view.place(placements)
        try {
            val items = placements.mapNotNull { it.first as? ItemView }
            Assertions.assertEquals(listOf(first, second), items.map { it.itemStack })
            Assertions.assertTrue(items.all { it.showDecorations })
            Assertions.assertEquals(listOf("64", "12"), placements.mapNotNull { (it.first as? MultiLineTextView)?.text?.value?.string })
            Assertions.assertEquals(2, placements.count { it.first is TooltipBackgroundView })
        } finally {
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `フィルタを変更すると入力欄とスクロール位置を保持したまま行と間隔を更新するのだ`() {
        val filter = ObservableValue("")
        val view = TooltipViewerViewWrapper(listOf(ItemStack(Items.STONE)), filter) {
            listOf("title", "first", "second", "third").map { text { it() } }
        }
        view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { 400 }, { 240 })
        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        val controller = view.place(placements)
        try {
            val field = placements.mapNotNull { it.first as? TextFieldView }.single()
            var scrollView = placements.mapNotNull { it.first as? YScrollView }.single()
            listOf(
                "0,1,3" to listOf("title", "first", "third"),
                "1,3" to listOf("first", "third"),
                "-1,99" to emptyList(),
                "invalid" to listOf("title", "first", "second", "third"),
                "" to listOf("title", "first", "second", "third"),
            ).forEach { (value, expected) ->
                scrollView.scrollOffsetY.value += 10.5
                val scrollOffset = scrollView.scrollOffsetY.value
                field.text.value = value
                Assertions.assertEquals(value, filter.value)
                Assertions.assertSame(field, placements.mapNotNull { it.first as? TextFieldView }.single())
                Assertions.assertEquals(1, placements.count { it.first is YScrollView })
                scrollView = placements.mapNotNull { it.first as? YScrollView }.single()
                Assertions.assertEquals(scrollOffset, scrollView.scrollOffsetY.value)
                val lines = placements.filter { it.first is MultiLineTextView }
                Assertions.assertEquals(expected, lines.map { (it.first as MultiLineTextView).text.value.string })
                if (value == "0,1,3") {
                    Assertions.assertEquals(12, lines[1].second.y - lines[0].second.y)
                    Assertions.assertEquals(9, lines[2].second.y - lines[1].second.y)
                }
                if (value.isEmpty()) {
                    Assertions.assertEquals(10, lines[2].second.y - lines[1].second.y)
                }
            }
        } finally {
            controller.dispose()
            controller.detach()
        }
        Assertions.assertTrue(placements.isEmpty())
    }

    @Test
    fun `長文を背景の内側で折り返し画面の高さを超えた内容をスクロールするのだ`() {
        val filter = ObservableValue("1")
        val view = TooltipViewerViewWrapper(listOf(ItemStack(Items.STONE)), filter) {
            listOf(text { "title"() }, text { "long".repeat(200)() })
        }
        listOf(IntPoint(400, 240), IntPoint(320, 180)).forEach { size ->
            view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { size.x }, { size.y })
            val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
            val controller = view.place(placements)
            try {
                val panel = placements.single { it.first is NinePatchImageView }
                Assertions.assertEquals(IntRectangle((size.x - 254) / 2, 4, 254, size.y - 8), panel.second)
                val field = placements.single { it.first is TextFieldView }
                Assertions.assertEquals("1", (field.first as TextFieldView).text.value)
                val scroll = placements.single { it.first is YScrollView }
                Assertions.assertEquals(IntRectangle((size.x - 240) / 2, 33, 240, size.y - 44), scroll.second)
                Assertions.assertEquals(5, (scroll.first as YScrollView).scrollbarThickness)
                Assertions.assertTrue((scroll.first as YScrollView).childView.evaluateActualSizeY() > scroll.second.sizeY)
                val line = placements.single { it.first is MultiLineTextView }
                Assertions.assertEquals(205, line.second.sizeX)
                Assertions.assertTrue(line.second.sizeY > scroll.second.sizeY)
            } finally {
                controller.dispose()
                controller.detach()
            }
        }
    }

    @Test
    fun `アイテムがなくても検索欄と固定幅のパネルを配置するのだ`() {
        val view = TooltipViewerViewWrapper(emptyList(), ObservableValue("")) { throw AssertionError("No item tooltip should be requested") }
        view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { 400 }, { 240 })
        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        val controller = view.place(placements)
        try {
            Assertions.assertEquals(254, placements.single { it.first is NinePatchImageView }.second.sizeX)
            Assertions.assertEquals(240, placements.single { it.first is TextFieldView }.second.sizeX)
            Assertions.assertEquals(0, placements.count { it.first is ItemView || it.first is TooltipBackgroundView })
        } finally {
            controller.dispose()
            controller.detach()
        }
    }

}
