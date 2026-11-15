package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.BeforeRenderView
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
import miragefairy2024.util.times
import miragefairy2024.util.toItemStacks
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

private const val PLAYER_INVENTORY_WIDTH = 9

private const val PLAYER_INVENTORY_HEIGHT = 3

private const val TRADE_SLOT_COUNT = 4

private const val HOTBAR_SLOT_OFFSET = PLAYER_INVENTORY_WIDTH * PLAYER_INVENTORY_HEIGHT
private const val INPUT_SLOT_OFFSET = HOTBAR_SLOT_OFFSET + PLAYER_INVENTORY_WIDTH
private const val OUTPUT_SLOT_OFFSET = INPUT_SLOT_OFFSET + TRADE_SLOT_COUNT

private val PANEL_TEXTURE = "textures/gui/" * MirageFairy2024.identifier("panel") * ".png"

private const val PANEL_MARGIN = 7

private const val SECTION_SPACE = 3
private const val PLAYER_INVENTORY_NAME_SPACE = 1
private const val HOTBAR_SPACE = 4
private const val INVENTORY_NAME_MARGIN_X = 1

fun fairyQuestCardView(recipe: FairyQuestRecipe, slots: List<Slot>, title: Component, playerInventoryTitle: Component, getProgress: () -> Int): View {
    val inputs = List(TRADE_SLOT_COUNT) { recipe.inputs.getOrNull(it)?.invoke() }
    val inputViews = inputs.map { input ->
        GhostRecipeItemView(input?.toItemStacks().orEmpty()).also { it.overlayArgb = 0x20FF0000 }
    }
    val outputViews = List(TRADE_SLOT_COUNT) { i ->
        val output: ItemStack? = recipe.outputs.getOrNull(i)?.invoke()
        GhostRecipeItemView(listOfNotNull(output)).also { it.overlayArgb = 0x2000FF00 }
    }
    val progressView = ProgressView(
        ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_background.png"), IntPoint(16, 16)),
        ViewTexture(MirageFairy2024.identifier("textures/gui/fairy_quest_progress_overlay.png"), IntPoint(16, 16)),
    )

    fun update(progress: Int) {
        inputViews.forEachIndexed { i, ghost ->
            val stack: ItemStack = slots[INPUT_SLOT_OFFSET + i].item
            val input = inputs[i]
            ghost.showItemStack = stack.isEmpty
            ghost.showOverlay = input != null && (!input.ingredient.test(stack) || stack.count < input.count)
        }
        outputViews.forEachIndexed { i, ghost ->
            val stack: ItemStack = slots[OUTPUT_SLOT_OFFSET + i].item
            ghost.showItemStack = stack.isEmpty
            ghost.showOverlay = stack.isEmpty && ghost.itemStacks.any { it.isNotEmpty }
        }
        progressView.progress = progress / recipe.duration.toDouble()
    }

    fun inventoryNameView(name: Component) = MultiLineTextView().also {
        it.text.value = name
        it.color = ColorPair.DARK_GRAY
        it.shadow = false
    }

    fun tradeSlotView(slot: Slot, ghost: GhostRecipeItemView) = View {
        view += StackView().configure {
            view += menuSlotView(slot, SlotType.NORMAL)
            view += ghost.configure().margin(SlotType.NORMAL.margin.x, SlotType.NORMAL.margin.y)
        }
    }

    fun slotRowView(offset: Int) = View {
        view += XListView().configure {
            repeat(PLAYER_INVENTORY_WIDTH) { c ->
                view += menuSlotView(slots[offset + c], SlotType.NORMAL)
            }
        }
    }

    return View {
        view += StackView().configure { // 外枠なのだ～🌱
            view += NinePatchImageView(PANEL_TEXTURE, 16, 16, 5, 5, 5, 5, 5, 5, false, false)
            view += YListView().configure { // 内枠なのだ～🌱

                view += inventoryNameView(title).configure {
                    view.alignmentX = Alignment.CENTER
                }.margin(INVENTORY_NAME_MARGIN_X, 0)

                view += YSpaceView(SECTION_SPACE)

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
                            }.margin(0, 0, 0, 1)
                        }
                    }.margin(11, 6, 11, 11)
                }.minContentSizeY(11 * 7 + 5).onClick {
                    recipe.openMessageScreen()
                    true
                }.tooltip(text { guiFullScreenTranslation() })

                view += YSpaceView(SECTION_SPACE)

                // 取引欄なのだ～🌱
                view += XListView().configure {
                    inputViews.forEachIndexed { i, ghost ->
                        view += tradeSlotView(slots[INPUT_SLOT_OFFSET + i], ghost)
                    }
                    view += progressView.configure().margin(1)
                    outputViews.forEachIndexed { i, ghost ->
                        view += tradeSlotView(slots[OUTPUT_SLOT_OFFSET + i], ghost)
                    }
                }

                view += YSpaceView(SECTION_SPACE)

                view += inventoryNameView(playerInventoryTitle).configure().margin(INVENTORY_NAME_MARGIN_X, 0)

                view += YSpaceView(PLAYER_INVENTORY_NAME_SPACE)

                // プレイヤーのインベントリなのだ～🌱
                repeat(PLAYER_INVENTORY_HEIGHT) { r ->
                    view += slotRowView(PLAYER_INVENTORY_WIDTH * r)
                }

                view += YSpaceView(HOTBAR_SPACE)

                // ホットバーなのだ～🌱
                view += slotRowView(HOTBAR_SLOT_OFFSET)

            }.margin(PANEL_MARGIN)

            view += BeforeRenderView { update(getProgress()) }
        }
    }
}
