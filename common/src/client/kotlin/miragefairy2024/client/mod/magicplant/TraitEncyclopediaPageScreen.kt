package miragefairy2024.client.mod.magicplant

import miragefairy2024.client.mod.recipeviewer.screen.ViewScreen
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.style
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.ButtonView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.WrappedTextView
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
import miragefairy2024.util.style
import miragefairy2024.util.text
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents

/** 画面の上下の余白は、カードが縦いっぱいに伸びる分を残すのだ～🌱 */
private const val ROOT_MARGIN_TOP = 20
private const val ROOT_MARGIN_BOTTOM = 4

private const val CARD_MARGIN = 5

private const val CARD_WIDTH = 160

private const val SCROLLBAR_THICKNESS = 5

private const val SCROLLBAR_ARGB = 0xA0FFFFFF.toInt()

private const val TRAIT_ICON_SIZE = 32

/** ボタンの寸法は、バニラのボタンと同じ高さなのだ～🌱 */
private val BUTTON_SIZE = IntPoint(80, 20)

private const val BUTTON_SPACE = 20

class TraitEncyclopediaPageScreen(private val parent: Screen?, private val trait: Trait) : ViewScreen(trait.getName()) {

    override fun createRootView(): View {
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // カード外枠なのだ～🌱
                view += StackView().configure {
                    position.alignmentX = Alignment.CENTER
                    position.weight = 1.0
                    view.sizingY = Sizing.FILL

                    view += NinePatchImageView(NinePatchTextureCard.TRAIT_BACKGROUND.texture, 22, 22, 22, 22, 22, 22)

                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 特性名なのだ～🌱
                        view += TextView(text { trait.getName().style(trait.style) }).configure {
                            view.sizingX = Sizing.FILL
                            view.alignmentX = Alignment.CENTER
                            view.shadow = false
                        }

                        view += YSpaceView(CARD_MARGIN)

                        // 特性アイコンなのだ～🌱
                        view += YListView().configure {
                            view.sizingX = Sizing.FILL

                            view += ImageView(ViewTexture(trait.texture, IntPoint(TRAIT_ICON_SIZE, TRAIT_ICON_SIZE), IntRectangle(0, 0, TRAIT_ICON_SIZE, TRAIT_ICON_SIZE))).configure {
                                position.alignmentX = Alignment.CENTER
                            }
                        }

                        view += YSpaceView(CARD_MARGIN)

                        // 特性ポエムなのだ～🌱 スクロールバーは中身へ重ねるのだ～🌱
                        view += ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SCROLLBAR_ARGB)).configure {
                            position.weight = 1.0

                            view += WrappedTextView(text { trait.text }).also {
                                it.shadow = false
                            }
                        }
                    }.minContentSizeX(CARD_WIDTH).margin(CARD_MARGIN)
                }.margin(0, 0, ROOT_MARGIN_TOP, ROOT_MARGIN_BOTTOM)

                view += YSpaceView(10)

                // ボタン列なのだ～🌱
                view += XListView().configure {
                    position.alignmentX = Alignment.CENTER

                    view += ButtonView(BUTTON_SIZE, text { guiBackToGameTranslation() }).also {
                        it.onPress.register {
                            minecraft!!.setScreen(null)
                        }
                    }

                    view += XSpaceView(BUTTON_SPACE)

                    view += ButtonView(BUTTON_SIZE, CommonComponents.GUI_BACK).also {
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
