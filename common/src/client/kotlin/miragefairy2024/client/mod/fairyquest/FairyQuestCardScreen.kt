package miragefairy2024.client.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.recipeviewer.screen.ViewContainerScreen
import miragefairy2024.client.mod.recipeviewer.screen.menuSlotView
import miragefairy2024.client.util.SlotType
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.fairyquest.FairyQuestCardScreenHandler
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.GhostItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeY
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeY
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.orEmpty
import miragefairy2024.util.text
import miragefairy2024.util.toItemStacks
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

private const val PLAYER_INVENTORY_WIDTH = 9

private const val PLAYER_INVENTORY_HEIGHT = 3

private const val PANEL_MARGIN = 7

/** 枠の横幅は、持ち物の 1 行の分なのだ～🌱 */
private const val MAIN_WIDTH = 18 * 9

private const val INVENTORY_NAME_SPACE = 3

private const val PLAYER_INVENTORY_NAME_SPACE = 1

private const val HOTBAR_SPACE = 4

/** 名前の左右の余白なのだ～🌱 */
private const val INVENTORY_NAME_MARGIN_X = 1

/** 依頼文の欄の縦なのだ～🌱 */
private const val MESSAGE_HEIGHT = 11 * 7 + 5

/** カードの内側の余白なのだ～🌱 右だけは、スクロールバーの分を残して狭いのだ～🌱 */
private const val CARD_MARGIN = 11
private const val CARD_MARGIN_RIGHT = 6

private const val SCROLLBAR_THICKNESS = 5

/** 罫線の繰り返しの周期なのだ～🌱 */
private const val LINE_HEIGHT = 11

/** 罫線の下に残す余白なのだ～🌱 */
private const val LINE_BOTTOM_MARGIN = 1

private val TEXT_COLOR = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())

/** スロットの枠の内側の余白なのだ～🌱 */
private const val SLOT_CONTENT_MARGIN = 1

/** 取引欄のスロットの個数なのだ～🌱 入力と出力で同じなのだ～🌱 */
private const val TRADE_SLOT_COUNT = 4

/** 入れるべきアイテムが足りないときに重ねる色なのだ～🌱 */
private const val INPUT_OVERLAY_ARGB = 0x20FF0000

/** 受け取れる成果があるときに重ねる色なのだ～🌱 */
private const val OUTPUT_OVERLAY_ARGB = 0x2000FF00

private const val PROGRESS_SIZE = 16

private val PROGRESS_BACKGROUND = MirageFairy2024.identifier("textures/gui/fairy_quest_progress_background.png")
private val PROGRESS_OVERLAY = MirageFairy2024.identifier("textures/gui/fairy_quest_progress_overlay.png")

class FairyQuestCardScreen(handler: FairyQuestCardScreenHandler, private val playerInventory: Inventory, title: Component) : ViewContainerScreen<FairyQuestCardScreenHandler>(handler, playerInventory, title) {

    /** 毎ティック、メニューの状態をViewへ反映する処理なのだ～🌱 */
    private val onScreenUpdate = mutableListOf<() -> Unit>()

    override fun createRootView(): View {
        onScreenUpdate.clear()

        /** 持ち物の名前なのだ～🌱 */
        fun inventoryNameView(name: Component, alignmentX: Alignment) = TextView(name).also {
            it.sizingX = Sizing.FILL
            it.alignmentX = alignmentX
            it.color = ColorPair.DARK_GRAY
            it.shadow = false
        }

        /** 取引欄の、ゴーストのアイテムを重ねたスロットなのだ～🌱 */
        fun tradeSlotView(index: Int, itemStacks: List<net.minecraft.world.item.ItemStack>, overlayArgb: Int, overlayPredicate: () -> Boolean) = View {
            view += StackView().configure {
                view += menuSlotView(menu.slots[index], SlotType.NORMAL)

                view += GhostItemView(itemStacks).also { ghostItemView ->
                    ghostItemView.overlayArgb = overlayArgb
                    onScreenUpdate += {
                        ghostItemView.showItemStack = menu.getSlot(index).item.isEmpty
                        ghostItemView.showOverlay = overlayPredicate()
                    }
                }.configure { }.margin(SLOT_CONTENT_MARGIN)
            }
        }

        /** スロットを横に並べて、中央へ寄せた1行なのだ～🌱 */
        fun slotRowView(count: Int, slotIndexFunction: (Int) -> Int) = View {
            view += XListView().configure {
                view.sizingX = Sizing.FILL

                view += SpaceView().configure { position.weight = 1.0 }
                repeat(count) { c ->
                    view += menuSlotView(menu.slots[slotIndexFunction(c)], SlotType.NORMAL)
                }
                view += SpaceView().configure { position.weight = 1.0 }
            }
        }

        val rootView = View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // 外枠なのだ～🌱 余った空間を全部取ってから、その中で中央へ寄せるのだ～🌱
                view += StackView().configure {
                    position.alignmentX = Alignment.CENTER
                    position.alignmentY = Alignment.CENTER
                    position.weight = 1.0

                    view += NinePatchImageView(NinePatchTextureCard.PANEL.texture, 5, 6, 5, 5, 6, 5)

                    // 内枠なのだ～🌱
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL

                        view += inventoryNameView(menu.recipe.title, Alignment.CENTER).configure { position.ignoreLayoutX = true }.margin(INVENTORY_NAME_MARGIN_X, 0)

                        view += YSpaceView(INVENTORY_NAME_SPACE)

                        // 依頼文の欄なのだ～🌱 押すと全画面で開くのだ～🌱
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            // 縦は、外側が固定した高さを受け取るのだ～🌱
                            view.sizingY = Sizing.FILL

                            view += NinePatchImageView(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.texture, 22, 22, 22, 22, 22, 22)

                            // スクロールバーを罫線へ重ねると、中身の罫線の方が手前で描かれて、スクロールバーが途切れるのだ～🌱
                            view += ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(0xA0000000.toInt())).configure {
                                view += StackView().configure {
                                    view.sizingX = Sizing.FILL

                                    // 四辺を0にして、罫線の繰り返しだけを描かせるのだ～🌱
                                    view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 0, LINE_HEIGHT, 0, 0, LINE_HEIGHT, 0)

                                    view += MultiLineTextView(menu.recipe.message).also {
                                        it.color = TEXT_COLOR
                                        it.shadow = false
                                    }.configure().margin(0, 0, 0, LINE_BOTTOM_MARGIN)
                                    // 中身をスクロールバーへ重ねないのだ～🌱
                                }.margin(0, SCROLLBAR_THICKNESS, 0, 0)
                            }.margin(CARD_MARGIN, CARD_MARGIN_RIGHT, CARD_MARGIN, CARD_MARGIN)
                        }.minContentSizeY(MESSAGE_HEIGHT).maxContentSizeY(MESSAGE_HEIGHT).onClick {
                            minecraft!!.setScreen(FairyQuestMessageScreen(this@FairyQuestCardScreen, menu.recipe.title, menu.recipe.message, menu.recipe.client, menu.recipe.title))
                            true
                        }.tooltip(text { guiFullScreenTranslation() })

                        view += YSpaceView(INVENTORY_NAME_SPACE)

                        // 取引欄なのだ～🌱
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            repeat(TRADE_SLOT_COUNT) { i ->
                                val index = PLAYER_INVENTORY_WIDTH * (PLAYER_INVENTORY_HEIGHT + 1) + i
                                val input = menu.recipe.inputs.getOrNull(i)
                                view += tradeSlotView(index, input?.invoke()?.toItemStacks() ?: listOf(), INPUT_OVERLAY_ARGB) {
                                    when {
                                        input == null -> false
                                        !input().ingredient.test(menu.getSlot(index).item) -> true
                                        menu.getSlot(index).item.count < input().count -> true
                                        else -> false
                                    }
                                }
                            }

                            // 進捗なのだ～🌱
                            view += ProgressView(
                                ViewTexture(PROGRESS_BACKGROUND, IntPoint(PROGRESS_SIZE, PROGRESS_SIZE), IntRectangle(0, 0, PROGRESS_SIZE, PROGRESS_SIZE)),
                                ViewTexture(PROGRESS_OVERLAY, IntPoint(PROGRESS_SIZE, PROGRESS_SIZE), IntRectangle(0, 0, PROGRESS_SIZE, PROGRESS_SIZE)),
                            ).also { progressView ->
                                onScreenUpdate += {
                                    progressView.progress = menu.progress / menu.recipe.duration.toDouble()
                                }
                            }.configure { }.margin(SLOT_CONTENT_MARGIN)

                            repeat(TRADE_SLOT_COUNT) { i ->
                                val index = PLAYER_INVENTORY_WIDTH * (PLAYER_INVENTORY_HEIGHT + 1) + TRADE_SLOT_COUNT + i
                                val output = menu.recipe.outputs.getOrNull(i)
                                view += tradeSlotView(index, output?.invoke()?.let { listOf(it) } ?: listOf(), OUTPUT_OVERLAY_ARGB) {
                                    output?.invoke().orEmpty.isNotEmpty && menu.getSlot(index).item.isEmpty
                                }
                            }
                        }

                        view += YSpaceView(INVENTORY_NAME_SPACE)

                        view += inventoryNameView(playerInventory.name, Alignment.START).configure { position.ignoreLayoutX = true }.margin(INVENTORY_NAME_MARGIN_X, 0)

                        view += YSpaceView(PLAYER_INVENTORY_NAME_SPACE)

                        // プレイヤーの持ち物なのだ～🌱
                        repeat(PLAYER_INVENTORY_HEIGHT) { r ->
                            view += slotRowView(PLAYER_INVENTORY_WIDTH) { c -> PLAYER_INVENTORY_WIDTH * r + c }
                        }

                        view += YSpaceView(HOTBAR_SPACE)

                        view += slotRowView(PLAYER_INVENTORY_WIDTH) { c -> PLAYER_INVENTORY_WIDTH * PLAYER_INVENTORY_HEIGHT + c }
                    }.minContentSizeX(MAIN_WIDTH).margin(PANEL_MARGIN)
                }
            }
        }

        onScreenUpdate.forEach {
            it()
        }
        return rootView
    }

    override fun containerTick() {
        super.containerTick()
        onScreenUpdate.forEach {
            it()
        }
    }

}
