package miragefairy2024.client.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.recipeviewer.screen.ViewScreen
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

private const val ROOT_MARGIN_TOP = 10
private const val ROOT_MARGIN_BOTTOM = 4

/** カードとその左右のスクロールバーの分を合わせた横幅なのだ～🌱 */
private const val TOTAL_WIDTH = 18 * 15

private const val SCROLLBAR_THICKNESS = 10

/** カードの横幅なのだ～🌱 左右のスクロールバーの分を除くのだ～🌱 */
private const val CARD_WIDTH = TOTAL_WIDTH - SCROLLBAR_THICKNESS * 2

/** カードの内側の余白なのだ～🌱 */
private const val CARD_MARGIN = 11

private const val SCROLLBAR_ARGB = 0xA0FFFFFF.toInt()

/** 罫線の繰り返しの周期なのだ～🌱 */
private const val LINE_HEIGHT = 11

/** 罫線の下に残す余白なのだ～🌱 */
private const val LINE_BOTTOM_MARGIN = 1

private val TEXT_COLOR = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())

/** ボタンの寸法は、バニラのボタンと同じ高さなのだ～🌱 */
private val BUTTON_SIZE = IntPoint(80, 20)

private const val BUTTON_SPACE = 20

private const val CARD_TO_BUTTON_SPACE = 10

class FairyQuestMessageScreen(private val parent: Screen?, private val fairyQuestTitle: Component, private val fairyQuestMessage: Component, private val fairyQuestClient: Component, title: Component) : ViewScreen(title) {

    override fun createRootView(): View {
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SCROLLBAR_ARGB)).configure {
                    position.alignmentX = Alignment.CENTER
                    position.weight = 1.0

                    // カードの外枠なのだ～🌱 スクロールバーと重ならないように、右に同じ幅の余白を置くのだ～🌱
                    view += StackView().configure {
                        view += NinePatchImageView(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.texture, 22, 22, 22, 22, 22, 22)

                        view += YListView().configure {
                            view.sizingX = Sizing.FILL

                            // 依頼の題名なのだ～🌱
                            view += MultiLineTextView(fairyQuestTitle).also {
                                it.alignmentX = Alignment.CENTER
                                it.color = TEXT_COLOR
                                it.shadow = false
                            }

                            view += YSpaceView(CARD_MARGIN)

                            // 依頼の本文なのだ～🌱 罫線を背景に重ねるのだ～🌱
                            view += StackView().configure {
                                view.sizingX = Sizing.FILL

                                // 四辺を0にして、罫線の繰り返しだけを描かせるのだ～🌱
                                view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 0, LINE_HEIGHT, 0, 0, LINE_HEIGHT, 0)

                                view += MultiLineTextView(fairyQuestMessage).also {
                                    it.color = TEXT_COLOR
                                    it.shadow = false
                                }.configure().margin(0, 0, 0, LINE_BOTTOM_MARGIN)
                            }

                            view += YSpaceView(CARD_MARGIN)

                            // 依頼人なのだ～🌱
                            view += MultiLineTextView(fairyQuestClient).also {
                                it.alignmentX = Alignment.END
                                it.color = TEXT_COLOR
                                it.shadow = false
                            }
                        }.minContentSizeX(CARD_WIDTH - CARD_MARGIN * 2).margin(CARD_MARGIN)
                    }.margin(0, SCROLLBAR_THICKNESS, 0, 0)
                }.margin(SCROLLBAR_THICKNESS, 0, ROOT_MARGIN_TOP, ROOT_MARGIN_BOTTOM)

                view += YSpaceView(CARD_TO_BUTTON_SPACE)

                // ボタン列なのだ～🌱
                view += XListView().configure {
                    position.alignmentX = Alignment.CENTER

                    view += TextButtonView(BUTTON_SIZE, text { guiBackToGameTranslation() }).also {
                        it.onPress.register {
                            minecraft!!.setScreen(null)
                        }
                    }

                    view += XSpaceView(BUTTON_SPACE)

                    view += TextButtonView(BUTTON_SIZE, CommonComponents.GUI_BACK).also {
                        it.onPress.register {
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
