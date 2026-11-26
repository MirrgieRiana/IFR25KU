package miragefairy2024.mod.fairyquest

import kotlinx.coroutines.runBlocking
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.views.BeforeRenderView
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.mod.recipeviewer.views.GhostRecipeItemView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import net.minecraft.SharedConstants
import net.minecraft.core.Holder
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.server.Bootstrap
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FairyQuestCardViewTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val menuType = runBlocking { fairyQuestCardScreenHandlerType.creator() }
        fairyQuestCardScreenHandlerType.complete(menuType, Holder.direct(menuType))
    }

    @Test
    fun `本文だけをスクロール領域に入れて取引欄のゴーストと実スロットを重ねるのだ`() {
        val cardTitle = text { "title"() }
        val recipe = object : FairyQuestRecipe {
            override val color = 0
            override val icon = { ItemStack(Items.WHITE_BED) }
            override val title = cardTitle
            override val message = text { "message ".repeat(100)() }
            override val client = cardTitle
            override val duration = 10
            override val inputs = listOf({ Items.DIRT.toIngredientStack(8) })
            override val outputs = listOf(icon)
        }
        val inventory = Inventory(Mockito.mock(Player::class.java))
        val menu = FairyQuestCardScreenHandler(0, inventory, recipe, ContainerLevelAccess.NULL)
        val slots = menu.slots
        val rootView = menu.createRootView(cardTitle, text { "inventory"() }, ScreenEventSource())
        val proxy: RenderingProxy = object : RenderingProxy {
            override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
            override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
            override fun getTextHeight() = 9
            override fun wrapText(text: FormattedText, maxWidth: Int) = text.string.chunked((maxWidth / 6).coerceAtLeast(1)).map { FormattedCharSequence.forward(it, Style.EMPTY) }
        }
        rootView.initSizeCalculation(proxy, { 400 }, { 240 })
        Assertions.assertEquals(IntPoint(176, 218), rootView.evaluateActualSize())

        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        rootView.attachTo(IntPoint.ZERO) { view, bounds ->
            placements += Pair(view, bounds)
            ViewController.NONE
        }
        val (scrollView, scrollBounds) = placements.single { it.first is YScrollContainerView }
        val (scrollbarView, scrollbarBounds) = placements.single { it.first is YFlatScrollbarView }
        val scroll = scrollView as YScrollContainerView
        val scrollbar = scrollbarView as YFlatScrollbarView
        Assertions.assertEquals(IntRectangle(18, 30, 140, 60), scrollBounds)
        Assertions.assertEquals(IntRectangle(158, 30, 5, 60), scrollbarBounds)
        Assertions.assertTrue(scroll.childView.evaluateActualSizeY() > 60)
        Assertions.assertEquals(scrollBounds.sizeY, scrollbar.viewportLength)
        Assertions.assertEquals(scroll.childView.evaluateActualSizeY(), scrollbar.contentLength)
        scroll.scrollOffsetY.value = 15.0
        Assertions.assertEquals(15.0, scrollbar.scrollOffset.value)
        scrollbar.scrollOffset.value = 30.0
        Assertions.assertEquals(30.0, scroll.scrollOffsetY.value)
        val messageTextBounds = mutableListOf<IntRectangle>()
        scroll.childView.attachTo(IntPoint.ZERO) { view, bounds ->
            if (view is MultiLineTextView) messageTextBounds += bounds
            ViewController.NONE
        }
        Assertions.assertEquals(140, messageTextBounds.single().sizeX)
        Assertions.assertEquals(IntRectangle(7, 19, 162, 82), placements.single { it.first is ClickableView }.second)
        Assertions.assertEquals(IntRectangle(7, 19, 162, 82), placements.single { it.first is TooltipView }.second)
        Assertions.assertEquals(listOf(IntRectangle(8, 7, 160, 9), IntRectangle(8, 125, 160, 9)), placements.filter { it.first is MultiLineTextView }.map { it.second })

        val placedSlots = placements.mapNotNull { (view, bounds) -> if (view is MenuSlotView) Pair(view.slot, bounds) else null }.toMap()
        val ghosts = placements.mapNotNull { (view, bounds) -> if (view is GhostRecipeItemView) Pair(view, bounds) else null }
        Assertions.assertEquals(slots.toSet(), placedSlots.keys)
        Assertions.assertEquals(IntRectangle(7, 135, 18, 18), placedSlots.getValue(slots[0]))
        Assertions.assertEquals(IntRectangle(7, 193, 18, 18), placedSlots.getValue(slots[27]))
        Assertions.assertEquals(8, ghosts.size)
        ghosts.forEachIndexed { i, (_, bounds) ->
            val slotBounds = placedSlots.getValue(slots[36 + i])
            Assertions.assertEquals(IntRectangle(slotBounds.x + 1, slotBounds.y + 1, 16, 16), bounds)
        }

        val input = ghosts[0].first
        val output = ghosts[4].first
        val progress = placements.single { it.first is ProgressView }.first as ProgressView
        val beforeRenderView = placements.single { it.first is BeforeRenderView }.first as BeforeRenderView

        fun beforeRender() = beforeRenderView.onBeforeRender.fire()

        beforeRender()
        Assertions.assertTrue(input.showItemStack && input.showOverlay)
        Assertions.assertTrue(output.showItemStack && output.showOverlay)
        Assertions.assertEquals(8, input.itemStacks.single().count)

        slots[36].set(ItemStack(Items.DIRT))
        slots[40].set(ItemStack(Items.WHITE_BED))
        menu.progress = 5
        beforeRender()
        Assertions.assertFalse(input.showItemStack)
        Assertions.assertTrue(input.showOverlay)
        Assertions.assertFalse(output.showItemStack || output.showOverlay)
        Assertions.assertEquals(0.5, progress.progress)

        slots[36].set(ItemStack(Items.DIRT, 8))
        beforeRender()
        Assertions.assertFalse(input.showItemStack || input.showOverlay)

        slots[36].set(ItemStack.EMPTY)
        slots[40].set(ItemStack.EMPTY)
        menu.progress = 0
        beforeRender()
        Assertions.assertTrue(input.showItemStack && input.showOverlay)
        Assertions.assertTrue(output.showItemStack && output.showOverlay)
        Assertions.assertEquals(0.0, progress.progress)
    }

}
