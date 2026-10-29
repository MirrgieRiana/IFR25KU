package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.clientProxy
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderScreen
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
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

class FairyQuestMessageScreen(private val fairyQuestTitle: Component, private val fairyQuestMessage: Component, private val fairyQuestClient: Component, override val title: Component) : ViewProviderScreen {
    override val closeOnInventoryKey = true
    override val closeOnUnhandledClick = true

    override fun createView(): View {
        return View {
            view += XListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += SpaceView().configure { position.weight = 1.0 }
                view += YListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    view += YScrollView().configure {
                        position.weight = 1.0
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
                                    view.color = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
                                    view.shadow = false
                                }
                                view += YSpaceView(11)
                                view += StackView().configure {
                                    view.sizingX = Sizing.FILL

                                    view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/fairy_quest_card_line.png"), 11, 11, 0, 11, 0, 0, 11, 0, true)
                                    view += MultiLineTextView().configure {
                                        view.text.value = fairyQuestMessage
                                        view.color = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
                                        view.shadow = false
                                    }.margin(0, 0, 0, 1)
                                }
                                view += YSpaceView(11)
                                view += MultiLineTextView().configure {
                                    view.text.value = fairyQuestClient
                                    view.alignmentX = Alignment.END
                                    view.color = ColorPair(0xFF6B472E.toInt(), 0xFF6B472E.toInt())
                                    view.shadow = false
                                }
                            }.margin(11)
                        }
                    }.margin(10, 0, 0, 0)

                    view += YSpaceView(10)
                    view += XListView().configure {
                        position.alignmentX = Alignment.CENTER

                        view += TextButtonView(IntPoint(80, 20), text { guiBackToGameTranslation() }).configure {
                            view.onPress.register {
                                clientProxy!!.closeToGame()
                            }
                        }
                        view += XSpaceView(20)
                        view += TextButtonView(IntPoint(80, 20), CommonComponents.GUI_BACK).configure {
                            view.onPress.register {
                                clientProxy!!.closeViewScreen()
                            }
                        }
                    }
                }.minContentSizeX(270).maxContentSizeX(270)
                view += SpaceView().configure { position.weight = 1.0 }
            }.margin(0, 0, 10, 4)
        }
    }
}
