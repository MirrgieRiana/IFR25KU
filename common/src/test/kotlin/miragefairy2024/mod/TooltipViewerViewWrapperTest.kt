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
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.util.empty
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
    override fun calculateTextWidth(text: FormattedCharSequence): Int {
        var width = 0
        text.accept { _, _, _ ->
            width += 6
            true
        }
        return width
    }
    override fun getTextHeight() = 9

    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> {
        return text.string.chunked(maxWidth / 6 atLeast 1).map { FormattedCharSequence.forward(it, Style.EMPTY) }
    }
}

private fun TooltipViewerViewWrapper.place(placements: MutableList<Pair<PlaceableView, IntRectangle>>): ViewController {
    fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
        val placement = Pair(view, bounds)
        placements += placement
        val controller = if (view is YScrollContainerView) view.childView.attachTo(bounds.offset, ::place) else ViewController.NONE
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
        val view = TooltipViewerViewWrapper(listOf(ItemStack.EMPTY, first, ItemStack.EMPTY, second)) {
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
    fun `フィルタを変更すると入力欄を維持しながら行と間隔を更新するのだ`() {
        val view = TooltipViewerViewWrapper(listOf(ItemStack(Items.STONE))) {
            listOf("title", "first", "second", "third").map { text { it() } }
        }
        view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { 400 }, { 240 })
        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        val controller = view.place(placements)
        try {
            val field = placements.mapNotNull { it.first as? TextFieldView }.single()
            val scrollView = placements.mapNotNull { it.first as? YScrollContainerView }.single()
            val scrollbarView = placements.mapNotNull { it.first as? YFlatScrollbarView }.single()
            listOf(
                "0,1,3" to listOf("title", "first", "third"),
                "1,3" to listOf("first", "third"),
                "-1,99" to emptyList(),
                "invalid" to listOf("title", "first", "second", "third"),
                "" to listOf("title", "first", "second", "third"),
            ).forEach { (value, expected) ->
                scrollView.scrollOffsetY.value += 10.5
                val scrollOffset = scrollView.scrollOffsetY.value
                Assertions.assertEquals(scrollOffset, scrollbarView.scrollOffset.value)
                field.text.value = value
                Assertions.assertSame(field, placements.mapNotNull { it.first as? TextFieldView }.single())
                Assertions.assertSame(scrollView, placements.mapNotNull { it.first as? YScrollContainerView }.single())
                Assertions.assertEquals(0.0, scrollView.scrollOffsetY.value)
                scrollbarView.scrollOffset.value += 0.25
                Assertions.assertEquals(scrollbarView.scrollOffset.value, scrollView.scrollOffsetY.value)
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
    fun `絞り込み後に範囲内の位置を保ち短くなった一覧を表示領域に戻すのだ`() {
        val view = TooltipViewerViewWrapper(listOf(ItemStack(Items.STONE))) {
            listOf(text { "title"() }, text { "long".repeat(500)() }, text { "mid".repeat(300)() })
        }
        view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { 400 }, { 240 })
        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        val controller = view.place(placements)
        try {
            val field = placements.mapNotNull { it.first as? TextFieldView }.single()
            val scroll = placements.mapNotNull { it.first as? YScrollContainerView }.single()
            val scrollbar = placements.mapNotNull { it.first as? YFlatScrollbarView }.single()
            Assertions.assertTrue(scrollbar.contentLength > scrollbar.viewportLength)
            scroll.scrollOffsetY.value = 300.0
            field.text.value = "1"
            Assertions.assertEquals(300.0, scroll.scrollOffsetY.value)
            Assertions.assertEquals(300.0, scrollbar.scrollOffset.value)
            field.text.value = "2"
            Assertions.assertEquals(107.0, scroll.scrollOffsetY.value)
            Assertions.assertEquals(107.0, scrollbar.scrollOffset.value)
            field.text.value = "0"
            Assertions.assertTrue(scrollbar.contentLength <= scrollbar.viewportLength)
            Assertions.assertEquals(0.0, scroll.scrollOffsetY.value)
            Assertions.assertEquals(0.0, scrollbar.scrollOffset.value)
        } finally {
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `ゼロ文字の行にも通常の文字行と同じ高さを割り当てるのだ`() {
        val view = TooltipViewerViewWrapper(listOf(ItemStack(Items.STONE))) {
            listOf(text { "title"() }, text { empty() }, text { "tail"() })
        }
        view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { 400 }, { 240 })
        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        val controller = view.place(placements)
        try {
            val lines = placements.filter { it.first is MultiLineTextView }
            val emptyLine = placements.single { it.first is TextView }.second
            Assertions.assertEquals(lines[0].second.sizeY, emptyLine.sizeY)
            Assertions.assertEquals(12, emptyLine.y - lines[0].second.y)
            Assertions.assertEquals(10, lines[1].second.y - emptyLine.y)
        } finally {
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `長文を背景の内側で折り返し画面の高さを超えた内容をスクロールするのだ`() {
        val view = TooltipViewerViewWrapper(listOf(ItemStack(Items.STONE))) {
            listOf(text { "title"() }, text { "long".repeat(200)() })
        }
        listOf(IntPoint(400, 240), IntPoint(320, 180)).forEach { size ->
            view.view.initSizeCalculation(TOOLTIP_VIEWER_RENDERING_PROXY, { size.x }, { size.y })
            val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
            val controller = view.place(placements)
            try {
                val panel = placements.single { it.first is NinePatchImageView }
                Assertions.assertEquals(IntRectangle((size.x - 254) / 2, 4, 254, size.y - 8), panel.second)
                val field = placements.mapNotNull { it.first as? TextFieldView }.single()
                field.text.value = "1"
                val scroll = placements.single { it.first is YScrollContainerView }
                val scrollView = scroll.first as YScrollContainerView
                Assertions.assertEquals(IntRectangle((size.x - 240) / 2, 33, 235, size.y - 44), scroll.second)
                val scrollbar = placements.single { it.first is YFlatScrollbarView }
                Assertions.assertEquals(IntRectangle((size.x - 240) / 2 + 235, 33, 5, size.y - 44), scrollbar.second)
                Assertions.assertEquals(5, (scrollbar.first as YFlatScrollbarView).scrollbarThickness)
                Assertions.assertTrue(scrollView.childView.evaluateActualSizeY() > scroll.second.sizeY)
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
        val view = TooltipViewerViewWrapper(emptyList()) { throw AssertionError("No item tooltip should be requested") }
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
