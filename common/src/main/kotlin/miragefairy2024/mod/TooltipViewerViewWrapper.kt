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
import miragefairy2024.mod.recipeviewer.views.AlignView
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ItemView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
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
        val definition = MessageViewScreenDefinition(text { TOOLTIP_VIEWER_KEY_TRANSLATION() }) {
            val player = proxy.getClientPlayer()
            val itemStacks = if (player != null) (0 until 9).map { player.inventory[it] } else emptyList()
            TooltipViewerViewWrapper(itemStacks, proxy::getItemTooltip).view
        }.also {
            it.additionalCloseKey = KeyMappingType.OPEN_TOOLTIP_VIEWER
            it.closeOnOutsideClick = false
        }
        proxy.setCurrentScreen(proxy.createMessageViewScreen(null, definition))
    }
    openScreen(clientProxy!!)
}

class TooltipViewerViewWrapper(itemStacks: List<ItemStack>, tooltipProvider: (ItemStack) -> List<Component>) {

    val view = View {

        fun createContentView(filter: String): View {
            return View {
                view += YListView().configure {
                    view.sizingX = Sizing.FILL

                    val allowIndices = filter.split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { null }
                    fun isAllowedIndex(index: Int) = allowIndices == null || index in allowIndices

                    itemStacks.filter { it.isNotEmpty }.forEachIndexed { index, itemStack ->

                        if (index > 0) view += YSpaceView(2)

                        // アイテムレコード
                        view += XListView().configure {
                            view.sizingX = Sizing.FILL

                            // アイテムスロット
                            view += StackView().configure {
                                view += ImageView(SlotType.NORMAL.viewTexture)
                                view += ItemView(itemStack).configure().margin(SlotType.NORMAL.margin.x, SlotType.NORMAL.margin.y)
                            }

                            view += XSpaceView(2)

                            // ツールチップ
                            view += StackView().configure {
                                position.weight = 1.0
                                view.sizingX = Sizing.FILL

                                view += TooltipBackgroundView()
                                view += YListView().configure {
                                    view.sizingX = Sizing.FILL

                                    tooltipProvider(itemStack).forEachIndexed { lineIndex, text ->
                                        if (!isAllowedIndex(lineIndex)) return@forEachIndexed

                                        if (lineIndex >= 1 && isAllowedIndex(lineIndex - 1)) {
                                            view += YSpaceView(if (lineIndex == 1) 3 else 1)
                                        }
                                        if (text.string.isEmpty()) {
                                            // MultiLineTextView に空文字列を渡すと 0 行の表示に潰れるのだ～🌱
                                            view += TextView()
                                        } else {
                                            view += MultiLineTextView().configure {
                                                view.text.value = text
                                            }
                                        }
                                    }
                                }.margin(4)
                            }

                        }

                    }
                }.margin(0, 2, 0, 0) // スクロールバーとの間隔なのだ～🌱
            }
        }

        // YListView は配置時に各行の位置を確定するため、行の高さの変化を後続行の位置に反映するには一覧全体の再配置が必要なのだ～🌱
        val contentView = DynamicView(createContentView(""))

        view += AlignView().configure {
            view.alignmentX = Alignment.CENTER

            // 外枠
            view += StackView().configure {
                view.sizingY = Sizing.FILL

                view += NinePatchImageView("textures/gui/" * MirageFairy2024.identifier("panel") * ".png", 16, 16, 5, 5, 5, 5, 5, 5, false, false)
                // 内枠
                view += YListView().configure {
                    view.sizingX = Sizing.FILL
                    view.sizingY = Sizing.FILL

                    // 検索欄
                    val textField = TextFieldView().configure {
                        view.hint = text { TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION("0,1,2") }
                    }
                    view += textField

                    view += YSpaceView(2)

                    // スクロールコンテナ
                    view += XListView().configure {
                        position.weight = 1.0
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL
                        val scrollView = YScrollContainerView().configure {
                            position.weight = 1.0
                            view += contentView
                        }
                        view += scrollView
                        val scrollbarView = YFlatScrollbarView()
                        view += scrollbarView
                        connectScrollbar(scrollView.view, scrollbarView)

                        textField.view.text.register { _, newValue ->
                            contentView.content.value = createContentView(newValue)
                            scrollView.view.updateScrollBounds()
                            scrollView.view.clampScrollOffset()
                        }
                    }

                }.minContentSizeX(240).margin(7)
            }
        }.margin(4)
    }

}
