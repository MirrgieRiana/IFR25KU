package miragefairy2024.mod.fairyquest

import miragefairy2024.ClientProxy
import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.createView
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

context(_: ModContext)
fun initFairyQuestMessageScreen() {
    onOpenFairyQuestMessageScreen.register { recipe ->
        recipe.openMessageScreen()
        true
    }
}

fun FairyQuestRecipe.openMessageScreen() {
    fun <S : Any> openScreen(proxy: ClientProxy<S>) {
        val parent = proxy.getCurrentScreen()
        proxy.setCurrentScreen(proxy.createMessageViewScreen(parent, MessageViewScreenDefinition(this.title, { FairyQuestMessageScreenContent.State() }) { _, state ->
            FairyQuestMessageScreenContent(this.title, this.message, this.client, state).also { view ->
                view.onBackToGame.register {
                    proxy.setCurrentScreen(null)
                    true
                }
                view.onBack.register {
                    proxy.setCurrentScreen(parent)
                    true
                }
            }.view
        }))
    }
    openScreen(clientProxy!!)
}

class FairyQuestMessageScreenContent(fairyQuestTitle: Component, fairyQuestMessage: Component, fairyQuestClient: Component, state: State) {

    class State {
        var scrollOffset = 0.0
    }

    val onBackToGame = EventRegistry<() -> Boolean>()
    val onBack = EventRegistry<() -> Boolean>()

    val view = View {
        val textColor = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            // メッセージ部分
            view += XListView().configure {
                position.alignmentX = Alignment.CENTER
                position.weight = 1.0
                view.sizingY = Sizing.FILL
                val scrollView = YScrollContainerView().configure {
                    view.scrollOffsetY.value = state.scrollOffset
                    view.scrollOffsetY.register { _, offset ->
                        state.scrollOffset = offset
                    }
                    view.sizingX = Sizing.WRAP

                    view += StackView().configure {
                        view.sizingX = Sizing.FILL

                        view += NinePatchTextureCard.FAIRY_QUEST_CARD_MESSAGE.createView()
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

                                view += NinePatchTextureCard.FAIRY_QUEST_CARD_LINE.createView()
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
                }
                view += scrollView
                val scrollbarView = YFlatScrollbarView().configure {
                    view.scrollbarThickness = 10
                    view.scrollbarColor = 0xA0FFFFFF.toInt()
                    view.isAttachment = true
                }
                view += scrollbarView
                connectScrollbar(scrollView.view, scrollbarView.view)
            }.margin(10, 0, 0, 0) // アンチスクロールバーマージン

            view += YSpaceView(10)

            // ボタン部分
            view += XListView().configure {
                position.alignmentX = Alignment.CENTER

                view += TextButtonView(IntPoint(80, 20)).configure {
                    view.message = text { guiBackToGameTranslation() }
                    view.onClick.register {
                        onBackToGame.fire {
                            if (it()) return@register true
                        }
                        false
                    }
                }
                view += XSpaceView(20)
                view += TextButtonView(IntPoint(80, 20)).configure {
                    view.message = CommonComponents.GUI_BACK
                    view.onClick.register {
                        onBack.fire {
                            if (it()) return@register true
                        }
                        false
                    }
                }
            }

        }.margin(0, 0, 10, 4)
    }

}
