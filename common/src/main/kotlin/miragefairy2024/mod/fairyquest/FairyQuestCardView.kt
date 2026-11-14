package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.GhostRecipeItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.menuSlotView
import miragefairy2024.mod.recipeviewer.views.minContentSizeY
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.text
import miragefairy2024.util.toItemStacks
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

internal class FairyQuestCardView(private val recipe: FairyQuestRecipe, private val slots: List<Slot>, title: Component, playerInventoryTitle: Component) {

    private val inputs = List(4) { recipe.inputs.getOrNull(it)?.invoke() }
    private val inputViews = inputs.map { input ->
        GhostRecipeItemView(input?.toItemStacks().orEmpty()).also { it.overlayArgb = 0x20FF0000 }
    }
    private val outputViews = List(4) { i ->
        val output: ItemStack? = recipe.outputs.getOrNull(i)?.invoke()
        GhostRecipeItemView(listOfNotNull(output)).also { it.overlayArgb = 0x2000FF00 }
    }
    private val progressView = ProgressView(
        ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_background.png"), IntPoint(16, 16)),
        ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_overlay.png"), IntPoint(16, 16)),
    )

    val view = View {
        view += StackView().configure { // 外枠なのだ～🌱
            view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/panel.png"), 16, 16, 5, 5, 5, 5, 5, 5, false, false)
            view += YListView().configure {
                view += inventoryNameView(title).configure {
                    view.alignmentX = Alignment.CENTER
                }
                view += YSpaceView(3)

                // 依頼本文なのだ～🌱
                view += StackView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL
                    view += NinePatchImageView(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.texture, 66, 66, 22, 22, 22, 22, 22, 22, true, true)
                    view += YScrollView().configure {
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11, 0, 11, 0, 0, 11, 0, true, true)
                            view += MultiLineTextView().configure {
                                view.text.value = recipe.message
                                view.color = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
                                view.shadow = false
                            }.margin(0, 1, 0, 1)
                        }
                    }.margin(11, 6, 11, 11)
                }.minContentSizeY(11 * 7 + 5).onClick {
                    recipe.openMessageScreen()
                    true
                }.tooltip(text { guiFullScreenTranslation() })
                view += YSpaceView(3)

                // 取引欄なのだ～🌱
                view += XListView().configure {
                    inputViews.forEachIndexed { i, ghost ->
                        view += tradeSlotView(slots[36 + i], ghost)
                    }
                    view += progressView.configure().margin(1)
                    outputViews.forEachIndexed { i, ghost ->
                        view += tradeSlotView(slots[40 + i], ghost)
                    }
                }
                view += YSpaceView(3)

                // プレイヤーのインベントリなのだ～🌱
                view += inventoryNameView(playerInventoryTitle)
                view += YSpaceView(1)
                repeat(3) { r ->
                    view += slotRowView(9 * r)
                }
                view += YSpaceView(4)
                view += slotRowView(27)
            }.margin(7)
        }
    }

    fun update(progress: Int) {
        inputViews.forEachIndexed { i, ghost ->
            val stack: ItemStack = slots[36 + i].item
            val input = inputs[i]
            ghost.showItemStack = stack.isEmpty
            ghost.showOverlay = input != null && (!input.ingredient.test(stack) || stack.count < input.count)
        }
        outputViews.forEachIndexed { i, ghost ->
            val stack: ItemStack = slots[40 + i].item
            ghost.showItemStack = stack.isEmpty
            ghost.showOverlay = stack.isEmpty && ghost.itemStacks.any { it.isNotEmpty }
        }
        progressView.progress = progress / recipe.duration.toDouble()
    }

    private fun inventoryNameView(name: Component) = MultiLineTextView().also {
        it.text.value = name
        it.color = ColorPair.DARK_GRAY
        it.shadow = false
    }

    private fun tradeSlotView(slot: Slot, ghost: GhostRecipeItemView) = View {
        view += StackView().configure {
            view += menuSlotView(slot, SlotType.NORMAL)
            view += ghost.configure().margin(SlotType.NORMAL.margin.x, SlotType.NORMAL.margin.y)
        }
    }

    private fun slotRowView(offset: Int) = View {
        view += XListView().configure {
            repeat(9) { c ->
                view += menuSlotView(slots[offset + c], SlotType.NORMAL)
            }
        }
    }

}
