package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.clientProxy
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderScreen
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
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
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.register
import miragefairy2024.util.text

class TooltipViewerScreen : ViewProviderScreen {
    override val title = text { TOOLTIP_VIEWER_KEY_TRANSLATION() }
    override val closeOnInventoryKey = true
    override val closeKeyMappings = listOf(OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION.keyGetter())

    private val filter = ObservableValue("")

    override fun createView(): View {
        val player = clientProxy!!.getClientPlayer() ?: return SpaceView()

        fun tooltipListView() = View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL

                val allowIndices = filter.value.split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { null }
                fun isAllowedIndex(index: Int) = allowIndices == null || index in allowIndices

                var firstItemStack = true
                repeat(9) { i ->
                    val itemStack = player.inventory[i]
                    if (itemStack.isNotEmpty) {
                        if (firstItemStack) {
                            firstItemStack = false
                        } else {
                            view += YSpaceView(2)
                        }
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            view += StackView().configure {
                                view += ImageView(SlotType.NORMAL.viewTexture)
                                view += ItemView(itemStack).configure {
                                    view.showDecorations = true
                                }.margin(1)
                            }
                            view += XSpaceView(2)
                            view += StackView().configure {
                                view.sizingX = Sizing.FILL
                                position.weight = 1.0
                                position.ignoreLayoutX = true

                                view += TooltipBackgroundView()
                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    clientProxy!!.getItemTooltip(itemStack).forEachIndexed { index, text ->
                                        if (isAllowedIndex(index)) {
                                            if (index >= 1 && isAllowedIndex(index - 1)) view += YSpaceView(if (index == 1) 3 else 1)
                                            view += MultiLineTextView().configure {
                                                view.text.value = text
                                                view.shadow = true
                                            }
                                        }
                                    }
                                }.margin(4)
                            }
                        }
                    }
                }
            }
        }

        val content = ObservableValue(tooltipListView())

        return View {
            view += XListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                view += SpaceView().configure { position.weight = 1.0 }
                view += StackView().configure {
                    view.sizingY = Sizing.FILL

                    view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/panel.png"), 16, 16, 5, 5, 5, 5, 5, 5, false)
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        view += TextFieldView().configure {
                            view.hint = text { TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION("0,1,2") }
                            view.text.value = filter.value
                            view.text.register { _, newValue ->
                                filter.value = newValue
                                content.value = tooltipListView()
                            }
                        }
                        view += YSpaceView(2)
                        view += YScrollView().configure {
                            position.weight = 1.0

                            view += DynamicView(content).configure {}.margin(0, 2, 0, 0)
                        }
                    }.minContentSizeX(240).maxContentSizeX(240).margin(7)
                }
                view += SpaceView().configure { position.weight = 1.0 }
            }.margin(4)
        }
    }
}
