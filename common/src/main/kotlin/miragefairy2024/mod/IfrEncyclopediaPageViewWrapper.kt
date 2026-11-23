package miragefairy2024.mod

import miragefairy2024.ClientProxy
import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
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
import miragefairy2024.mod.recipeviewer.views.fixedContentSizeX
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.network.chat.CommonComponents

context(_: ModContext)
fun initIfrEncyclopediaPageScreen() {
    onOpenIfrEncyclopediaPageScreen.register { card ->
        card.openIfrEncyclopediaPageScreen()
        true
    }
}

private fun IfrEncyclopediaEntryCard.openIfrEncyclopediaPageScreen() {
    fun <S : Any> openScreen(proxy: ClientProxy<S>) {
        val parent = proxy.getCurrentScreen()
        val title = text { this@openIfrEncyclopediaPageScreen.titleTranslation() }
        proxy.setCurrentScreen(proxy.createMessageViewScreen(parent, MessageViewScreenDefinition(title) {
            IfrEncyclopediaPageViewWrapper(this).also { view ->
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

private class IfrEncyclopediaPageViewWrapper(card: IfrEncyclopediaEntryCard) {

    val onBackToGame = EventRegistry<() -> Boolean>()
    val onBack = EventRegistry<() -> Boolean>()

    val view = View {
        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            // カード部分
            view += XListView().configure {
                position.weight = 1.0
                position.alignmentX = Alignment.CENTER
                view.sizingY = Sizing.FILL

                // カード外枠
                view += StackView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += NinePatchTextureCard.IFR_ENCYCLOPEDIA_BACKGROUND.createView()
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // タイトル
                        view += MultiLineTextView().configure {
                            view.text.value = text { card.titleTranslation() }
                            view.alignmentX = Alignment.CENTER
                            view.color = IFR_ENCYCLOPEDIA_TEXT_COLOR
                            view.shadow = false
                        }

                        view += YSpaceView(5)

                        // 掲げられたアイテム
                        view += StackView().configure {
                            position.alignmentX = Alignment.CENTER

                            val itemStack = card.itemStacksGetter().firstOrNull()
                            if (itemStack != null) {
                                view += ImageView(SlotType.NORMAL.viewTexture)
                                view += ItemView(itemStack).configure().margin(SlotType.NORMAL.margin.x, SlotType.NORMAL.margin.y)
                            }
                        }.margin(0, 5)

                        view += YSpaceView(5)

                        // 本文
                        view += StackView().configure {
                            position.weight = 1.0
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            val scrollView = YScrollContainerView().configure {
                                view += MultiLineTextView().configure {
                                    view.text.value = text { card.textTranslation() }
                                    view.color = IFR_ENCYCLOPEDIA_TEXT_COLOR
                                    view.shadow = false
                                }
                            }
                            view += scrollView
                            val scrollbarView = YFlatScrollbarView().configure {
                                view.keyboardFocusable = false
                            }
                            view += AlignView().configure {
                                view.alignmentX = Alignment.END
                                view += scrollbarView
                            }
                            connectScrollbar(scrollView.view, scrollbarView.view)
                        }

                    }.margin(5, 5, 10, 5)
                }.fixedContentSizeX(200)
            }

            view += YSpaceView(10)

            // 下部ボタン
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

        }.margin(0, 0, 20, 4)
    }

}
