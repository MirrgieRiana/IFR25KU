package miragefairy2024.mod.fairyquest

import miragefairy2024.ClientProxy
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
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
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

context(ModContext)
fun initFairyQuestMessageScreen() {
    onOpenFairyQuestMessageScreen.register { recipe ->
        recipe.openMessageScreen()
        true
    }
}

fun FairyQuestRecipe.openMessageScreen() {
    fun <S : Any> openScreen(proxy: ClientProxy<S>) {
        val parent = proxy.getCurrentScreen()
        proxy.setCurrentScreen(proxy.createMessageViewScreen(parent, this.title) {
            FairyQuestMessageViewWrapper(this.title, this.message, this.client).also { view ->
                view.onBackToGame.register {
                    proxy.setCurrentScreen(null)
                }
                view.onBack.register {
                    proxy.setCurrentScreen(parent)
                }
            }.view
        })
    }
    openScreen(clientProxy!!)
}

class FairyQuestMessageViewWrapper(fairyQuestTitle: Component, fairyQuestMessage: Component, fairyQuestClient: Component) {

    val onBackToGame = EventRegistry<() -> Unit>()
    val onBack = EventRegistry<() -> Unit>()

    val view = View {
        val textColor = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            // メッセージ部分
            view += YScrollView().configure {
                position.alignmentX = Alignment.CENTER
                position.weight = 1.0
                view.sizingX = Sizing.WRAP
                view.scrollbarThickness = 10
                view.scrollbarColor = 0xA0FFFFFF.toInt()

                view += StackView().configure {
                    view.sizingX = Sizing.FILL

                    view += NinePatchImageView(NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL

                        view += MultiLineTextView().configure {
                            view.text.value = fairyQuestTitle
                            view.alignmentX = Alignment.CENTER
                            view.color = textColor
                            view.shadow = false
                        }
                        view += YSpaceView(11)
                        view += StackView().configure {
                            view.sizingX = Sizing.FILL

                            view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11, 0, 11, 0, 0, 11, 0, true)
                            view += MultiLineTextView().configure {
                                view.text.value = fairyQuestMessage
                                view.color = textColor
                                view.shadow = false
                            }.margin(0, 0, 0, 1)
                        }
                        view += YSpaceView(11)
                        view += MultiLineTextView().configure {
                            view.text.value = fairyQuestClient
                            view.alignmentX = Alignment.END
                            view.color = textColor
                            view.shadow = false
                        }
                    }.minContentSizeX(18 * 15 - 10 * 2 - 11 * 2).margin(11)
                }
            }.margin(10, 0, 0, 0) // アンチスクロールバーマージン

            view += YSpaceView(10)

            // ボタン部分
            view += XListView().configure {
                position.alignmentX = Alignment.CENTER

                view += TextButtonView(IntPoint(80, 20)).configure {
                    view.message = text { guiBackToGameTranslation() }
                    view.onClick.register {
                        onBackToGame.fire()
                        true
                    }
                }
                view += XSpaceView(20)
                view += TextButtonView(IntPoint(80, 20)).configure {
                    view.message = CommonComponents.GUI_BACK
                    view.onClick.register {
                        onBack.fire()
                        true
                    }
                }
            }

        }.margin(0, 0, 10, 4)
    }

}
