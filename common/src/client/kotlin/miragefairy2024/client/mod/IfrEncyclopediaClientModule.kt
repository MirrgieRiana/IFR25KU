package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.screen.ViewScreen
import miragefairy2024.mod.IfrEncyclopediaEntryCard
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.onOpenIfrEncyclopediaPageScreen
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.TextView
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
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents

context(ModContext)
fun initIfrEncyclopediaClientModule() {
    onOpenIfrEncyclopediaPageScreen.register {
        Minecraft.getInstance().setScreen(IfrEncyclopediaPageScreen(Minecraft.getInstance().screen, it))
        true
    }
}

/** 画面の上下の余白は、カードが縦いっぱいに伸びる分を残すのだ～🌱 */
private const val ROOT_MARGIN_TOP = 20
private const val ROOT_MARGIN_BOTTOM = 4

private const val CARD_MARGIN_TOP = 10
private const val CARD_MARGIN = 5

private const val CARD_WIDTH = 200

private const val SCROLLBAR_THICKNESS = 5

private const val SCROLLBAR_ARGB = 0xA0000000.toInt()

/** 背景のテクスチャはダークモードでも替わらないから、暗い側も同じ色にするのだ～🌱 */
private val TEXT_COLOR = ColorPair(0xFF493208.toInt(), 0xFF493208.toInt())

/** ボタンの寸法は、バニラのボタンと同じ高さなのだ～🌱 */
private val BUTTON_SIZE = IntPoint(80, 20)

private const val BUTTON_SPACE = 20

class IfrEncyclopediaPageScreen(private val parent: Screen?, private val card: IfrEncyclopediaEntryCard) : ViewScreen(text { card.titleTranslation() }) {

    override fun createRootView(): View {
        val itemStack = card.itemStacksGetter().firstOrNull()

        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // カード外枠なのだ～🌱
                view += StackView().configure {
                    position.alignmentX = Alignment.CENTER
                    position.weight = 1.0
                    view.sizingY = Sizing.FILL

                    view += NinePatchImageView(NinePatchTextureCard.IFR_ENCYCLOPEDIA_BACKGROUND.texture, 22, 22, 22, 22, 22, 22)

                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // タイトルなのだ～🌱
                        view += TextView(text { card.titleTranslation() }).configure {
                            view.sizingX = Sizing.FILL
                            view.alignmentX = Alignment.CENTER
                            view.color = TEXT_COLOR
                            view.shadow = false
                        }

                        view += YSpaceView(CARD_MARGIN)

                        // 掲げられたアイテムなのだ～🌱 アイテムが無くても、上下の余白は占めるのだ～🌱
                        view += YListView().configure {
                            view.sizingX = Sizing.FILL

                            if (itemStack != null) {
                                view += OutputSlotView(itemStack).configure {
                                    position.alignmentX = Alignment.CENTER
                                }
                            }
                        }.margin(0, 0, CARD_MARGIN, CARD_MARGIN)

                        view += YSpaceView(CARD_MARGIN)

                        // 本文なのだ～🌱 スクロールバーは中身へ重ねるのだ～🌱
                        view += ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SCROLLBAR_ARGB)).configure {
                            position.weight = 1.0

                            view += MultiLineTextView(text { card.textTranslation() }).also {
                                it.color = TEXT_COLOR
                                it.shadow = false
                            }
                        }
                    }.minContentSizeX(CARD_WIDTH).margin(CARD_MARGIN, CARD_MARGIN, CARD_MARGIN_TOP, CARD_MARGIN)
                }.margin(0, 0, ROOT_MARGIN_TOP, ROOT_MARGIN_BOTTOM)

                view += YSpaceView(10)

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
