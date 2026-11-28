package miragefairy2024.mod.magicplant

import miragefairy2024.ClientProxy
import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.ImageView
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
import miragefairy2024.util.style
import miragefairy2024.util.text
import net.minecraft.network.chat.CommonComponents

context(_: ModContext)
fun initTraitEncyclopediaPageScreen() {
    onOpenTraitEncyclopediaPageScreen.register { trait ->
        trait.openTraitEncyclopediaPageScreen()
        true
    }
}

private fun Trait.openTraitEncyclopediaPageScreen() {
    fun <S : Any> openScreen(proxy: ClientProxy<S>) {
        val parent = proxy.getCurrentScreen()
        proxy.setCurrentScreen(proxy.createMessageViewScreen(parent, MessageViewScreenDefinition(this.getName(), { TraitEncyclopediaPageScreenContent.State() }) { _, state ->
            TraitEncyclopediaPageScreenContent(this, state).also { view ->
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

class TraitEncyclopediaPageScreenContent(trait: Trait, state: State) {

    class State {
        var scrollOffset = 0.0
    }

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

                    view += NinePatchTextureCard.TRAIT_BACKGROUND.createView()
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 特性名
                        view += MultiLineTextView().configure {
                            view.text.value = trait.getName().style(trait.style)
                            view.alignmentX = Alignment.CENTER
                            view.shadow = false
                        }

                        view += YSpaceView(5)

                        // 特性アイコン
                        view += ImageView(ViewTexture(trait.texture, IntPoint(32, 32))).configure {
                            position.alignmentX = Alignment.CENTER
                        }

                        view += YSpaceView(5)

                        // 特性ポエム
                        view += StackView().configure {
                            position.weight = 1.0
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL
                            val scrollView = YScrollContainerView().configure {
                                view.scrollOffsetY.value = state.scrollOffset
                                view.scrollOffsetY.register { _, offset ->
                                    state.scrollOffset = offset
                                }
                                view += MultiLineTextView().configure {
                                    view.text.value = trait.text
                                    view.shadow = false
                                }
                            }
                            view += scrollView
                            val scrollbarView = YFlatScrollbarView().configure {
                                view.scrollbarColor = 0xA0FFFFFF.toInt()
                                view.isAttachment = true
                            }
                            view += AlignView().configure {
                                view.alignmentX = Alignment.END
                                view += scrollbarView
                            }
                            connectScrollbar(scrollView.view, scrollbarView.view)
                        }
                    }.margin(5)
                }.fixedContentSizeX(160)
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
