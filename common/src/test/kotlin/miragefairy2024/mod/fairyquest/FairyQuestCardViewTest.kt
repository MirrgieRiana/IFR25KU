package miragefairy2024.mod.fairyquest

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.mod.recipeviewer.views.GhostRecipeItemView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.util.text
import miragefairy2024.util.invoke
import miragefairy2024.util.toIngredientStack
import net.minecraft.SharedConstants
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.server.Bootstrap
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.SimpleContainer
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FairyQuestCardViewTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `本文だけをスクロール領域に入れて取引欄のゴーストと実スロットを重ねるのだ`() {
        val inventory = SimpleContainer(44)
        val slots = List(44) { Slot(inventory, it, 0, 0) }
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
        val card = FairyQuestCardView(recipe, slots, cardTitle, text { "inventory"() })
        val proxy: RenderingProxy = object : RenderingProxy {
            override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
            override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
            override fun getTextHeight() = 9
            override fun wrapText(text: FormattedText, maxWidth: Int) = text.string.chunked((maxWidth / 6).coerceAtLeast(1)).map { FormattedCharSequence.forward(it, Style.EMPTY) }
        }
        card.view.initSizeCalculation(proxy, { 400 }, { 240 })
        Assertions.assertEquals(IntPoint(176, 218), card.view.evaluateActualSize())

        val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
        card.view.attachTo(IntPoint.ZERO) { view, bounds ->
            placements += Pair(view, bounds)
            ViewController.NONE
        }
        Assertions.assertEquals(IntRectangle(18, 30, 145, 60), placements.single { it.first is YScrollView }.second)
        val scroll = placements.single { it.first is YScrollView }.first as YScrollView
        Assertions.assertTrue(scroll.childView.evaluateActualSizeY() > 60)
        Assertions.assertEquals(IntRectangle(7, 19, 162, 82), placements.single { it.first is ClickableView }.second)
        Assertions.assertEquals(IntRectangle(7, 19, 162, 82), placements.single { it.first is TooltipView }.second)

        val placedSlots = placements.mapNotNull { (view, bounds) -> if (view is MenuSlotView) Pair(view.slot, bounds) else null }.toMap()
        val ghosts = placements.mapNotNull { (view, bounds) -> if (view is GhostRecipeItemView) Pair(view, bounds) else null }
        Assertions.assertEquals(slots.toSet(), placedSlots.keys)
        Assertions.assertEquals(8, ghosts.size)
        ghosts.forEachIndexed { i, (_, bounds) ->
            val slotBounds = placedSlots.getValue(slots[36 + i])
            Assertions.assertEquals(IntRectangle(slotBounds.x + 1, slotBounds.y + 1, 16, 16), bounds)
        }

        val input = ghosts[0].first
        val output = ghosts[4].first
        val progress = placements.single { it.first is ProgressView }.first as ProgressView
        card.update(0)
        Assertions.assertTrue(input.showItemStack && input.showOverlay)
        Assertions.assertTrue(output.showItemStack && output.showOverlay)
        Assertions.assertEquals(8, input.itemStacks.single().count)

        inventory.setItem(36, ItemStack(Items.DIRT))
        inventory.setItem(40, ItemStack(Items.WHITE_BED))
        card.update(5)
        Assertions.assertFalse(input.showItemStack)
        Assertions.assertTrue(input.showOverlay)
        Assertions.assertFalse(output.showItemStack || output.showOverlay)
        Assertions.assertEquals(0.5, progress.progress)

        inventory.setItem(36, ItemStack(Items.DIRT, 8))
        card.update(5)
        Assertions.assertFalse(input.showItemStack || input.showOverlay)

        inventory.clearContent()
        card.update(0)
        Assertions.assertTrue(input.showItemStack && input.showOverlay)
        Assertions.assertTrue(output.showItemStack && output.showOverlay)
    }

}
