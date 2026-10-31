package miragefairy2024.client.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewScreen
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

private val TEXT_COLOR = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())

class FairyQuestMessageScreen(private val parent: Screen?, private val fairyQuestTitle: Component, private val fairyQuestMessage: Component, private val fairyQuestClient: Component, title: Component) : ViewScreen(title) {

    override fun createRootView(): View {
        val buttonSize = IntPoint(80, 20)
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += ScrollView(10, ScrollbarStyle.Flat(0xA0FFFFFF.toInt())).configure {
                    position.alignmentX = Alignment.CENTER
                    position.weight = 1.0

                    // カードの外枠なのだ～🌱 スクロールバーと重ならないように、右に同じ幅の余白を置くのだ～🌱
                    view += StackView().configure {
                        view += NinePatchImageView(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)

                        view += YListView().configure {
                            view.sizingX = Sizing.FILL

                            // 依頼の題名なのだ～🌱
                            view += MultiLineTextView().configure {
                                view.text.value = fairyQuestTitle
                                view.alignmentX = Alignment.CENTER
                                view.color = TEXT_COLOR
                                view.shadow = false
                            }

                            view += YSpaceView(11)

                            // 依頼の本文なのだ～🌱 罫線を背景に重ねるのだ～🌱
                            view += StackView().configure {
                                view.sizingX = Sizing.FILL

                                // 四辺を 0 にして、罫線の繰り返しだけを描かせるのだ～🌱
                                view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11, 0, 11, 0, 0, 11, 0, true)

                                view += MultiLineTextView().configure {
                                    view.text.value = fairyQuestMessage
                                    view.color = TEXT_COLOR
                                    view.shadow = false
                                }.margin(0, 0, 0, 1)
                            }

                            view += YSpaceView(11)

                            // 依頼人なのだ～🌱
                            view += MultiLineTextView().configure {
                                view.text.value = fairyQuestClient
                                view.alignmentX = Alignment.END
                                view.color = TEXT_COLOR
                                view.shadow = false
                            }
                        }.minContentSizeX(((18 * 15) - 10 * 2) - 11 * 2).margin(11)
                    }.margin(0, 10, 0, 0)
                }.margin(10, 0, 10, 4)

                view += YSpaceView(10)

                // ボタン列なのだ～🌱
                view += XListView().configure {
                    position.alignmentX = Alignment.CENTER

                    view += TextButtonView(buttonSize, text { guiBackToGameTranslation() }).configure {
                        view.onPress.register {
                            minecraft!!.setScreen(null)
                        }
                    }

                    view += XSpaceView(20)

                    view += TextButtonView(buttonSize, CommonComponents.GUI_BACK).configure {
                        view.onPress.register {
                            onClose()
                        }
                    }
                }
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true
        if (minecraft!!.options.keyInventory.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.mouseClicked(mouseX, mouseY, button)) return true
        onClose()
        return true
    }

    override fun onClose() {
        minecraft!!.setScreen(parent)
    }

}
