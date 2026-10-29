package miragefairy2024.mod

import miragefairy2024.clientProxy
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.common.guiBackToGameTranslation
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderScreen
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
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

class IfrEncyclopediaPageScreen(private val card: IfrEncyclopediaEntryCard) : ViewProviderScreen {
    override val title = text { card.titleTranslation() }
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

                    view += StackView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        position.weight = 1.0
                        position.ignoreLayoutY = true

                        view += NinePatchImageView(NinePatchTextureCard.IFR_ENCYCLOPEDIA_BACKGROUND.texture, 66, 66, 22, 22, 22, 22, 22, 22, true)
                        view += YListView().configure {
                            view.sizingX = Sizing.FILL
                            view.sizingY = Sizing.FILL

                            view += MultiLineTextView().configure {
                                view.text.value = title
                                view.alignmentX = Alignment.CENTER
                                view.color = ColorPair(0xFF493208.toInt(), 0xFF493208.toInt())
                                view.shadow = false
                            }
                            view += YSpaceView(5)
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
                            view += YScrollView().configure {
                                position.weight = 1.0
                                view.reserveScrollbarSpace = false

                                view += MultiLineTextView().configure {
                                    view.text.value = text { card.textTranslation() }
                                    view.color = ColorPair(0xFF493208.toInt(), 0xFF493208.toInt())
                                    view.shadow = false
                                }
                            }
                        }.margin(5, 5, 10, 5)
                    }

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
                }.minContentSizeX(200).maxContentSizeX(200)
                view += SpaceView().configure { position.weight = 1.0 }
            }.margin(0, 0, 20, 4)
        }
    }
}
