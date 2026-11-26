package miragefairy2024.mod.fairy

import kotlinx.coroutines.runBlocking
import miragefairy2024.ClientProxy
import miragefairy2024.clientProxy
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.util.Chance
import miragefairy2024.util.CondensedItem
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import net.minecraft.SharedConstants
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
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
import org.mockito.Mockito

private val MOTIF_TABLE_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = 9

    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> {
        return text.string.chunked(maxWidth / 6 atLeast 1).map { FormattedCharSequence.forward(it, Style.EMPTY) }
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MotifTableViewTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val menuType = runBlocking { motifTableScreenHandlerType.creator() }
        motifTableScreenHandlerType.complete(menuType, Holder.direct(menuType))
    }

    @Test
    fun `アイテムと提供割合と凝縮率を同じ順序で配置し行全体にツールチップを付けるのだ`() {
        val first: ItemStack = ItemStack(Items.STONE, 64)
        val second: ItemStack = ItemStack(Items.DIRT)
        val motif: Motif = Mockito.mock(Motif::class.java)
        val menu = MotifTableScreenHandler(0, listOf(
            CondensedMotifChance(first, Chance(0.125, CondensedItem(2.5, motif))),
            CondensedMotifChance(second, Chance(0.875, CondensedItem(1.0, motif))),
        ))
        val size = IntPoint(400, 240)
        val view = menu.createRootView(text { "title"() }, text { "inventory"() }, ScreenEventSource())
        view.initSizeCalculation(MOTIF_TABLE_RENDERING_PROXY, { size.x }, { size.y })
        val uiSize = view.evaluateActualSize()
        Assertions.assertEquals(IntPoint(334, size.y), uiSize)
        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
            placements += Pair(view, bounds)
            if (view is YScrollContainerView) return view.childView.attachTo(bounds.offset, ::place)
            return ViewController.NONE
        }
        val controller = view.attachTo(IntPoint((size.x - uiSize.x) / 2, 0), ::place)
        try {
            Assertions.assertEquals(IntRectangle(33, 4, 334, 232), placements.single { it.first is NinePatchImageView }.second)
            val scroll = placements.single { it.first is YScrollContainerView }
            Assertions.assertEquals(IntRectangle(40, 11, 302, 218), scroll.second)
            val scrollbar = placements.single { it.first is YInventoryScrollbarView }
            Assertions.assertEquals(IntRectangle(342, 11, 18, 218), scrollbar.second)
            Assertions.assertEquals(18, (scrollbar.first as YInventoryScrollbarView).scrollbarThickness)
            val items = placements.mapNotNull { it.first as? ItemView }
            Assertions.assertEquals(listOf(first, second), items.map { it.itemStack })
            Assertions.assertTrue(items.all { !it.showDecorations })
            val texts = placements.filter { it.first is MultiLineTextView }
            Assertions.assertEquals(listOf(first.hoverName.string, "12.5000%", "x2.50", second.hoverName.string, "87.5000%", "x1.00"), texts.map { (it.first as MultiLineTextView).text.value.string })
            Assertions.assertEquals(listOf(150, 50, 80, 150, 50, 80), texts.map { it.second.sizeX })
            Assertions.assertEquals(listOf(Alignment.START, Alignment.END, Alignment.END, Alignment.START, Alignment.END, Alignment.END), texts.map { (it.first as MultiLineTextView).alignmentX })
            Assertions.assertTrue(texts.all { (it.first as MultiLineTextView).color == ColorPair.DARK_GRAY && !(it.first as MultiLineTextView).shadow })
            val tooltips = placements.filter { it.first is TooltipView }
            Assertions.assertEquals(listOf(IntRectangle(40, 11, 299, 16), IntRectangle(40, 27, 299, 16)), tooltips.map { it.second })
            val oldProxy = clientProxy
            val proxy: ClientProxy<*> = Mockito.mock(ClientProxy::class.java)
            clientProxy = proxy
            try {
                Mockito.`when`(proxy.getItemTooltip(first)).thenReturn(listOf(text { "first tooltip"() }))
                Mockito.`when`(proxy.getItemTooltip(second)).thenReturn(listOf(text { "second tooltip"() }))
                Assertions.assertEquals(listOf(text { "first tooltip"() }), (tooltips[0].first as TooltipView).tooltipProvider(40, 11))
                Assertions.assertEquals(listOf(text { "second tooltip"() }), (tooltips[1].first as TooltipView).tooltipProvider(40, 27))
            } finally {
                clientProxy = oldProxy
            }
        } finally {
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `画面寸法ごとに一覧を配置し名前の折り返し幅とバーの同期を確認するのだ`() {
        val itemStack: ItemStack = ItemStack(Items.STONE).also {
            it.set(DataComponents.CUSTOM_NAME, text { "long".repeat(20)() })
        }
        val motif: Motif = Mockito.mock(Motif::class.java)
        val menu = MotifTableScreenHandler(0, List(20) { CondensedMotifChance(itemStack, Chance(0.05, CondensedItem(1.0, motif))) })
        listOf(IntPoint(400, 240), IntPoint(640, 360)).forEach { size ->
            val view = menu.createRootView(text { "title"() }, text { "inventory"() }, ScreenEventSource())
            view.initSizeCalculation(MOTIF_TABLE_RENDERING_PROXY, { size.x }, { size.y })
            val uiSize = view.evaluateActualSize()
            Assertions.assertEquals(IntPoint(334, size.y), uiSize)
            val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
            fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
                placements += Pair(view, bounds)
                if (view is YScrollContainerView) return view.childView.attachTo(bounds.offset, ::place)
                return ViewController.NONE
            }
            val controller = view.attachTo(IntPoint((size.x - uiSize.x) / 2, 0), ::place)
            try {
                val scroll = placements.single { it.first is YScrollContainerView }.first as YScrollContainerView
                val scrollbar = placements.single { it.first is YInventoryScrollbarView }.first as YInventoryScrollbarView
                Assertions.assertFalse(scrollbar.keyboardFocusable)
                Assertions.assertEquals(840, scroll.childView.evaluateActualSizeY())
                Assertions.assertEquals(840, scrollbar.contentLength)
                Assertions.assertEquals(size.y - 22, scrollbar.viewportLength)
                scrollbar.scrollOffset.value = 125.5
                Assertions.assertEquals(125.5, scroll.scrollOffsetY.value)
                scroll.scrollOffsetY.value = 250.25
                Assertions.assertEquals(250.25, scrollbar.scrollOffset.value)
                val names = placements.filter { (it.first as? MultiLineTextView)?.text?.value == itemStack.hoverName }
                Assertions.assertEquals(20, names.size)
                Assertions.assertTrue(names.all { it.second.sizeX == 150 && it.second.sizeY == 42 })
                val tooltips = placements.filter { it.first is TooltipView }
                Assertions.assertTrue(tooltips.all { it.second.sizeY == 42 })
                val items = placements.filter { it.first is ItemView }
                Assertions.assertEquals(13, items.first().second.y - names.first().second.y)
                Assertions.assertEquals(IntRectangle((size.x - 334) / 2, 4, 334, size.y - 8), placements.single { it.first is NinePatchImageView }.second)
            } finally {
                controller.dispose()
                controller.detach()
            }
        }
    }

}
