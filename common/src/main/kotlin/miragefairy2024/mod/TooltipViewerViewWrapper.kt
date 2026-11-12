package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
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
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.registerAndInitialize
import miragefairy2024.util.text
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

class TooltipViewerViewWrapper(itemStacks: List<ItemStack>, filter: ObservableValue<String>, tooltipProvider: (ItemStack) -> List<Component>) {

    val view = View {
        var currentScrollView: YScrollView? = null
        fun scrollView() = View {
            view += YScrollView().configure {
                view.scrollOffset.value = currentScrollView?.scrollOffset?.value ?: 0.0
                currentScrollView = view
                view += YListView().configure {
                    view.sizingX = Sizing.FILL

                    val allowIndices = filter.value.split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { null }
                    fun isAllowedIndex(index: Int) = allowIndices == null || index in allowIndices

                    var firstItemStack = true
                    itemStacks.forEach { itemStack ->
                        if (!itemStack.isNotEmpty) return@forEach

                        if (firstItemStack) {
                            firstItemStack = false
                        } else {
                            view += YSpaceView(2)
                        }

                        // アイテムレコードなのだ～🌱
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            // アイテムスロットなのだ～🌱
                            view += StackView().configure {
                                view += ImageView(SlotType.NORMAL.viewTexture)
                                view += ItemView(itemStack).configure().margin(1)
                            }

                            view += XSpaceView(2)

                            // ツールチップなのだ～🌱
                            view += StackView().configure {
                                position.weight = 1.0
                                position.ignoreLayoutX = true
                                view.sizingX = Sizing.FILL

                                view += TooltipBackgroundView()
                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    tooltipProvider(itemStack).forEachIndexed { index, text ->
                                        if (!isAllowedIndex(index)) return@forEachIndexed

                                        if (index >= 1 && isAllowedIndex(index - 1)) {
                                            view += YSpaceView(if (index == 1) 3 else 1)
                                        }
                                        view += MultiLineTextView().configure {
                                            view.text.value = text
                                            view.shadow = true
                                        }
                                    }
                                }.margin(4)
                            }
                        }
                    }
                }.margin(0, 2, 0, 0)
            }
        }

        val contentView = DynamicView()

        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            view += StackView().configure { // 外枠なのだ～🌱
                position.alignmentX = Alignment.CENTER
                position.weight = 1.0
                view.sizingY = Sizing.FILL

                view += NinePatchImageView(MirageFairy2024.identifier("textures/gui/panel.png"), 16, 16, 5, 5, 5, 5, 5, 5, false, false)
                view += YListView().configure { // 内枠なのだ～🌱
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 検索欄なのだ～🌱
                    view += TextFieldView().configure {
                        view.hint = text { TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION("0,1,2") }
                        view.text.value = filter.value
                        view.text.registerAndInitialize { _, newValue ->
                            filter.value = newValue
                            contentView.content.value = scrollView()
                        }
                    }

                    view += YSpaceView(2)

                    // スクロールなのだ～🌱
                    view += contentView.configure {
                        position.weight = 1.0
                    }
                }.minContentSizeX(240).margin(7)
            }
        }.margin(4)
    }

}
