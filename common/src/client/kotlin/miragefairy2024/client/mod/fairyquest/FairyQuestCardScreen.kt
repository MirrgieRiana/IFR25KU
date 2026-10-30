package miragefairy2024.client.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.recipeviewer.vanilla.AbstractViewContainerScreen
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.SlotType
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
import miragefairy2024.mod.recipeviewer.views.menuSlotView
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
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

private val TEXT_COLOR = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())

private val PROGRESS_BACKGROUND = MirageFairy2024.identifier("textures/gui/fairy_quest_progress_background.png")
private val PROGRESS_OVERLAY = MirageFairy2024.identifier("textures/gui/fairy_quest_progress_overlay.png")

class FairyQuestCardScreen(handler: FairyQuestCardScreenHandler, private val playerInventory: Inventory, title: Component) : AbstractViewContainerScreen<FairyQuestCardScreenHandler>(handler, playerInventory, title) {

    /** 毎ティック、メニューの状態を View へ反映する処理なのだ～🌱 */
    private val screenUpdateListenerList = mutableListOf<() -> Unit>()

    override fun createRootView(): View {
        screenUpdateListenerList.clear()

        /** 持ち物の名前なのだ～🌱 */
        fun inventoryNameView(name: Component, alignmentX: Alignment) = TextView(name).also {
            it.sizingX = Sizing.FILL
            it.alignmentX = alignmentX
            it.color = ColorPair.DARK_GRAY
            it.shadow = false
        }

        /** 取引欄の、ゴーストのアイテムを重ねたスロットなのだ～🌱 */
        fun tradeSlotView(index: Int, itemStacks: List<ItemStack>, overlayArgb: Int, overlayPredicate: () -> Boolean) = View {
            view += StackView().configure {
                view += menuSlotView(menu.slots[index], SlotType.NORMAL)

                view += GhostItemView(itemStacks).also { ghostItemView ->
                    ghostItemView.overlayArgb = overlayArgb
                    screenUpdateListenerList += {
                        ghostItemView.showItemStack = menu.getSlot(index).item.isEmpty
                        ghostItemView.showOverlay = overlayPredicate()
                    }
                }.configure { }.margin(1)
            }
        }

        /** スロットを横に並べて、中央へ寄せた 1 行なのだ～🌱 */
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

                    view += NinePatchImageView(NinePatchTextureCard.PANEL.texture, 16, 16, 5, 6, 5, 5, 6, 5, true)

                    // 内枠なのだ～🌱
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL

                        view += inventoryNameView(menu.recipe.title, Alignment.CENTER).configure { position.ignoreLayoutX = true }.margin(1, 0)

                        view += YSpaceView(3)

                        // 依頼文の欄なのだ～🌱 押すと全画面で開くのだ～🌱
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL
                            // 縦は、外側が固定した高さを受け取るのだ～🌱
                            view.sizingY = Sizing.FILL

                            view += NinePatchImageView(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)

                            // スクロールバーを罫線へ重ねると、中身の罫線の方が手前で描かれて、スクロールバーが途切れるのだ～🌱
                            view += ScrollView(5, ScrollbarStyle.Flat(0xA0000000.toInt())).configure {
                                view += StackView().configure {
                                    view.sizingX = Sizing.FILL

                                    // 四辺を 0 にして、罫線の繰り返しだけを描かせるのだ～🌱
                                    view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11, 0, 11, 0, 0, 11, 0, true)

                                    view += MultiLineTextView().configure {
                                        view.text.value = menu.recipe.message
                                        view.color = TEXT_COLOR
                                        view.shadow = false
                                    }.margin(0, 0, 0, 1)
                                    // 中身をスクロールバーへ重ねないのだ～🌱
                                }.margin(0, 5, 0, 0)
                            }.margin(11, 6, 11, 11)
                        }.minContentSizeY(11 * 7 + 5).maxContentSizeY(11 * 7 + 5).onClick {
                            minecraft!!.setScreen(FairyQuestMessageScreen(this@FairyQuestCardScreen, menu.recipe.title, menu.recipe.message, menu.recipe.client, menu.recipe.title))
                            true
                        }.tooltip(text { guiFullScreenTranslation() })

                        view += YSpaceView(3)

                        // 取引欄なのだ～🌱
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            repeat(4) { i ->
                                val index = 9 * (3 + 1) + i
                                val input = menu.recipe.inputs.getOrNull(i)
                                view += tradeSlotView(index, input?.invoke()?.toItemStacks() ?: listOf(), 0x20FF0000) {
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
                                ViewTexture(PROGRESS_BACKGROUND, IntPoint(16, 16)),
                                ViewTexture(PROGRESS_OVERLAY, IntPoint(16, 16)),
                            ).also { progressView ->
                                screenUpdateListenerList += {
                                    progressView.progress = menu.progress / menu.recipe.duration.toDouble()
                                }
                            }.configure { }.margin(1)

                            repeat(4) { i ->
                                val index = 9 * (3 + 1) + 4 + i
                                val output = menu.recipe.outputs.getOrNull(i)
                                view += tradeSlotView(index, output?.invoke()?.let { listOf(it) } ?: listOf(), 0x2000FF00) {
                                    output?.invoke().orEmpty.isNotEmpty && menu.getSlot(index).item.isEmpty
                                }
                            }
                        }

                        view += YSpaceView(3)

                        view += inventoryNameView(playerInventory.name, Alignment.START).configure { position.ignoreLayoutX = true }.margin(1, 0)

                        view += YSpaceView(1)

                        // プレイヤーの持ち物なのだ～🌱
                        repeat(3) { r ->
                            view += slotRowView(9) { c -> 9 * r + c }
                        }

                        view += YSpaceView(4)

                        view += slotRowView(9) { c -> 9 * 3 + c }
                    }.minContentSizeX(18 * 9).margin(7)
                }
            }
        }

        screenUpdateListenerList.forEach {
            it()
        }
        return rootView
    }

    override fun containerTick() {
        super.containerTick()
        screenUpdateListenerList.forEach {
            it()
        }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = renderTransparentBackground(guiGraphics)

}
