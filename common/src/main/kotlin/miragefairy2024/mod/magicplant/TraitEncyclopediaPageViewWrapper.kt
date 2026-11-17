package miragefairy2024.mod.magicplant

import miragefairy2024.ClientProxy
import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.clientProxy
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
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
import miragefairy2024.util.style
import miragefairy2024.util.text
import net.minecraft.network.chat.CommonComponents

fun Trait.openTraitEncyclopediaPageScreen() {
    fun <S : Any> openScreen(proxy: ClientProxy<S>) {
        val parent = proxy.getCurrentScreen()
        val title = this.getName()
        proxy.setCurrentScreen(proxy.createMessageViewScreen(parent, MessageViewScreenDefinition(title) {
            TraitEncyclopediaPageViewWrapper(this, { proxy.setCurrentScreen(null) }, { proxy.setCurrentScreen(parent) }).view
        }))
    }
    openScreen(clientProxy!!)
}

private class TraitEncyclopediaPageViewWrapper(trait: Trait, onBackToGame: () -> Unit, onBack: () -> Unit) {

    val view = View {
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

                    view += NinePatchImageView(NinePatchTextureCard.TRAIT_BACKGROUND.texture, 66, 66, 22, 22, 22, 22, 22, 22, true, true)
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 特性名なのだ～🌱
                        view += MultiLineTextView().configure {
                            view.text.value = text { trait.getName().style(trait.style) }
                            view.alignmentX = Alignment.CENTER
                            view.shadow = false
                        }

                        view += YSpaceView(5)

                        // 特性アイコンなのだ～🌱
                        view += ImageView(ViewTexture(trait.texture, IntPoint(32, 32))).configure {
                            position.alignmentX = Alignment.CENTER
                        }

                        view += YSpaceView(5)

                        // 本文なのだ～🌱
                        view += YScrollView().configure {
                            position.weight = 1.0
                            view.scrollbarColor = 0xA0FFFFFF.toInt()

                            view += MultiLineTextView().configure {
                                view.text.value = text { trait.text }
                                view.shadow = false
                            }
                        }
                    }.margin(5)
                }.minContentSizeX(160).maxContentSizeX(160)
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
