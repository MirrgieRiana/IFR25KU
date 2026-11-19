package miragefairy2024.mod

import miragefairy2024.ClientProxy
import miragefairy2024.MessageViewScreenDefinition
import miragefairy2024.MirageFairy2024
import miragefairy2024.clientProxy
import miragefairy2024.mod.common.KeyMappingType
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
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
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.register
import miragefairy2024.util.text
import miragefairy2024.util.times
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

fun openTooltipViewerScreen() {
    fun <S : Any> openScreen(proxy: ClientProxy<S>) {
        val filter = ObservableValue("")
        val scrollOffsetY = ObservableValue(0.0)
        val definition = MessageViewScreenDefinition(text { TOOLTIP_VIEWER_KEY_TRANSLATION() }) {
            val player = proxy.getClientPlayer()
            val itemStacks = if (player != null) (0 until 9).map { player.inventory[it] } else emptyList()
            TooltipViewerViewWrapper(itemStacks, filter, scrollOffsetY, proxy::getItemTooltip).view
        }.also {
            it.additionalCloseKey = KeyMappingType.OPEN_TOOLTIP_VIEWER
            it.closeOnOutsideClick = false
        }
        proxy.setCurrentScreen(proxy.createMessageViewScreen(null, definition))
    }
    openScreen(clientProxy!!)
}

class TooltipViewerViewWrapper(itemStacks: List<ItemStack>, filter: ObservableValue<String>, scrollOffsetY: ObservableValue<Double>, tooltipProvider: (ItemStack) -> List<Component>) {

    val view = View {
        val panelTexture = "textures/gui/" * MirageFairy2024.identifier("panel") * ".png"
        val panelMargin = 7

        fun createContentView(): View {
            val allowIndices = filter.value.split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { null }
            fun isAllowedIndex(index: Int) = allowIndices == null || index in allowIndices

            return View {
                view += YListView().configure {
                    view.sizingX = Sizing.FILL

                    itemStacks.asSequence().filter { it.isNotEmpty }.forEachIndexed { index, itemStack ->
                        if (index > 0) view += YSpaceView(2)

                        // アイテムレコードなのだ～🌱
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            // アイテムスロットなのだ～🌱
                            view += StackView().configure {
                                view += ImageView(SlotType.NORMAL.viewTexture)
                                view += ItemView(itemStack).configure().margin(SlotType.NORMAL.margin.x, SlotType.NORMAL.margin.y)
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

                                    tooltipProvider(itemStack).forEachIndexed { lineIndex, text ->
                                        if (!isAllowedIndex(lineIndex)) return@forEachIndexed

                                        if (lineIndex > 0 && isAllowedIndex(lineIndex - 1)) {
                                            view += YSpaceView(if (lineIndex == 1) 3 else 1)
                                        }
                                        view += MultiLineTextView().configure {
                                            view.text.value = text
                                        }
                                    }
                                }.margin(4)
                            }
                        }
                    }
                }.margin(0, 2, 0, 0)
            }
        }

        val contentView = DynamicView(createContentView())

        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            view += StackView().configure { // 外枠なのだ～🌱
                position.alignmentX = Alignment.CENTER
                position.weight = 1.0
                view.sizingY = Sizing.FILL

                view += NinePatchImageView(panelTexture, 16, 16, 5, 5, 5, 5, 5, 5, false, false)
                view += YListView().configure { // 内枠なのだ～🌱
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 検索欄なのだ～🌱
                    view += TextFieldView().configure {
                        view.hint = text { TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION("0,1,2") }
                        view.text.value = filter.value
                        view.text.register { _, newValue ->
                            filter.value = newValue
                            contentView.content.value = createContentView()
                        }
                    }

                    view += YSpaceView(2)

                    // スクロールコンテナなのだ～🌱
                    view += XListView().configure {
                        position.weight = 1.0
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        val scrollView = YScrollContainerView().configure {
                            position.weight = 1.0
                            view.scrollOffsetY.value = scrollOffsetY.value
                            view.scrollOffsetY.register { _, newValue ->
                                scrollOffsetY.value = newValue
                            }
                            view += contentView
                        }
                        view += scrollView
                        val scrollbarView = YScrollbarView().configure()
                        view += scrollbarView
                        connectScrollbar(scrollView.view, scrollbarView.view)
                    }

                }.minContentSizeX(240).margin(panelMargin)
            }
        }.margin(4)
    }

}
