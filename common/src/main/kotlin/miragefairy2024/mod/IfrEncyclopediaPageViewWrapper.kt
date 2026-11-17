package miragefairy2024.mod

import miragefairy2024.ClientProxy
import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.clientProxy
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.maxContentSizeX
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

fun IfrEncyclopediaEntryCard.openIfrEncyclopediaPageScreen() {
    fun <S : Any> openScreen(proxy: ClientProxy<S>) {
        val parent = proxy.getCurrentScreen()
        val title = text { this@openIfrEncyclopediaPageScreen.titleTranslation() }
        proxy.setCurrentScreen(proxy.createMessageViewScreen(parent, MessageViewScreenDefinition(title) {
            IfrEncyclopediaPageViewWrapper(this, title, { proxy.setCurrentScreen(null) }, { proxy.setCurrentScreen(parent) }).view
        }))
    }
    openScreen(clientProxy!!)
}

private class IfrEncyclopediaPageViewWrapper(card: IfrEncyclopediaEntryCard, title: Component, onBackToGame: () -> Unit, onBack: () -> Unit) {

    val view = View {
        val textColor = ColorPair(0xFF493208.toInt(), 0xFF493208.toInt())

        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            view += XListView().configure {
                position.weight = 1.0
                position.ignoreLayoutY = true
                position.alignmentX = Alignment.CENTER
                view.sizingY = Sizing.FILL

                view += StackView().configure { // カードなのだ～🌱
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += NinePatchImageView(NinePatchTextureCard.IFR_ENCYCLOPEDIA_BACKGROUND.texture, 66, 66, 22, 22, 22, 22, 22, 22, true, true)
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // タイトルなのだ～🌱
                        view += MultiLineTextView().configure {
                            view.text.value = title
                            view.alignmentX = Alignment.CENTER
                            view.color = textColor
                            view.shadow = false
                        }

                        view += YSpaceView(5)

                        // 掲げられたアイテムなのだ～🌱
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            view += SpaceView().configure { position.weight = 1.0 }
                            val itemStack = card.itemStacksGetter().firstOrNull()
                            if (itemStack != null) {
                                view += StackView().configure {
                                    view += ImageView(SlotType.NORMAL.viewTexture)
                                    view += ItemView(itemStack).configure {
                                        view.showDecorations = true
                                    }.margin(1)
                                }
                            }
                            view += SpaceView().configure { position.weight = 1.0 }
                        }.margin(0, 5)

                        view += YSpaceView(5)

                        // 本文なのだ～🌱
                        view += YScrollView().configure {
                            position.weight = 1.0

                            view += MultiLineTextView().configure {
                                view.text.value = text { card.textTranslation() }
                                view.color = textColor
                                view.shadow = false
                            }
                        }
                    }.margin(5, 5, 10, 5)
                }.minContentSizeX(200).maxContentSizeX(200)
            }

            view += YSpaceView(10)

            // ボタン部分なのだ～🌱
            view += XListView().configure {
                position.alignmentX = Alignment.CENTER

                view += TextButtonView(IntPoint(80, 20)).configure {
                    view.message = text { guiBackToGameTranslation() }
                    view.onClick.register {
                        onBackToGame()
                        true
                    }
                }

                view += XSpaceView(20)

                view += TextButtonView(IntPoint(80, 20)).configure {
                    view.message = CommonComponents.GUI_BACK
                    view.onClick.register {
                        onBack()
                        true
                    }
                }
            }
        }.margin(0, 0, 20, 4)
    }

}
