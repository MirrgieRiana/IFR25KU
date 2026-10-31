package miragefairy2024.client.mod.magicplant

import miragefairy2024.client.mod.recipeviewer.vanilla.ViewScreen
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
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
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
import miragefairy2024.util.style
import miragefairy2024.util.text
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents

class TraitEncyclopediaPageScreen(private val parent: Screen?, private val trait: Trait) : ViewScreen(trait.getName()) {

    override fun createRootView(): View {
        val buttonSize = IntPoint(80, 20)
        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // カード外枠なのだ～🌱
                view += StackView().configure {
                    position.alignmentX = Alignment.CENTER
                    position.weight = 1.0
                    view.sizingY = Sizing.FILL

                    view += NinePatchImageView(NinePatchTextureCard.TRAIT_BACKGROUND.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)

                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 特性名なのだ～🌱
                        view += TextView(text { trait.getName().style(trait.style) }).configure {
                            view.sizingX = Sizing.FILL
                            view.alignmentX = Alignment.CENTER
                            view.shadow = false
                        }

                        view += YSpaceView(5)

                        // 特性アイコンなのだ～🌱
                        view += YListView().configure {
                            view.sizingX = Sizing.FILL

                            view += ImageView(ViewTexture(trait.texture, IntPoint(32, 32))).configure {
                                position.alignmentX = Alignment.CENTER
                            }
                        }

                        view += YSpaceView(5)

                        // 特性ポエムなのだ～🌱 スクロールバーは中身へ重ねるのだ～🌱
                        view += ScrollView(5, ScrollbarStyle.Flat(0xA0FFFFFF.toInt())).configure {
                            position.weight = 1.0

                            view += MultiLineTextView().configure {
                                view.text.value = text { trait.text }
                                view.shadow = false
                            }
                        }
                    }.minContentSizeX(160).margin(5)
                }.margin(0, 0, 20, 4)

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
