package miragefairy2024.client.mod

import com.mojang.blaze3d.platform.InputConstants
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewScreen
import miragefairy2024.client.util.KeyMappingCard
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION
import miragefairy2024.mod.TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION
import miragefairy2024.mod.TOOLTIP_VIEWER_KEY_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox

val tooltipViewerKeyMappingCard = KeyMappingCard(
    OPEN_TOOLTIP_VIEWER_KEY_TRANSLATION.keyGetter(),
    InputConstants.UNKNOWN.value,
    KeyMapping.CATEGORY_INVENTORY,
) {
    Minecraft.getInstance().setScreen(TooltipViewerScreen())
}

context(ModContext)
fun initTooltipViewerClientModule() {
    tooltipViewerKeyMappingCard.init()
}

class TooltipViewerScreen : ViewScreen(text { TOOLTIP_VIEWER_KEY_TRANSLATION() }) {

    private var filter = ""

    /** 検索欄の入力の最中にウィジェットを捨てると落ちるため、作り直しは描画の直前まで遅らせるのだ～🌱 */
    private var filterChanged = false

    override fun createRootView(): View {
        val allowedIndices = filter.split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { null }
        fun isAllowedIndex(index: Int) = allowedIndices == null || index in allowedIndices

        return View {
            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // 外枠なのだ～🌱
                view += StackView().configure {
                    position.alignmentX = Alignment.CENTER
                    position.weight = 1.0
                    view.sizingY = Sizing.FILL

                    view += NinePatchImageView(NinePatchTextureCard.PANEL.texture, 16, 16, 5, 6, 5, 5, 6, 5, true)

                    // 内枠なのだ～🌱
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 検索欄なのだ～🌱
                        view += TextFieldView().configure {
                            view.hint = text { TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION("0,1,2") }
                            view.text.value = filter
                            view.text.register { _, text ->
                                filter = text
                                filterChanged = true
                            }
                        }

                        view += YSpaceView(2)

                        // スクロールなのだ～🌱
                        view += ScrollView(5, ScrollbarStyle.Flat(0xA0000000.toInt())).configure {
                            position.weight = 1.0

                            // スクロールの中身なのだ～🌱 スクロールバーと重ならないように、右へ余白を作るのだ～🌱
                            view += YListView().configure {
                                view.sizingX = Sizing.FILL

                                val player = Minecraft.getInstance().player ?: return@configure

                                var firstItemStack = true
                                (0 until 9).forEach { i ->
                                    val itemStack = player.inventory[i]
                                    if (!itemStack.isNotEmpty) return@forEach

                                    if (firstItemStack) {
                                        firstItemStack = false
                                    } else {
                                        view += YSpaceView(2)
                                    }

                                    // アイテムレコードなのだ～🌱
                                    view += XListView().configure {
                                        view.sizingX = Sizing.FILL

                                        view += OutputSlotView(itemStack)

                                        view += XSpaceView(2)

                                        // ツールチップなのだ～🌱
                                        view += StackView().configure {
                                            position.weight = 1.0
                                            view.sizingX = Sizing.FILL

                                            view += TooltipBackgroundView()

                                            view += YListView().configure {
                                                view.sizingX = Sizing.FILL

                                                val tooltip = getTooltipFromItem(minecraft!!, itemStack)
                                                tooltip.forEachIndexed { index, text ->
                                                    if (!isAllowedIndex(index)) return@forEachIndexed
                                                    if (index >= 1 && isAllowedIndex(index - 1)) {
                                                        view += YSpaceView(if (index == 1) 3 else 1)
                                                    }
                                                    view += TextView(text)
                                                }
                                            }.margin(4)
                                        }
                                    }
                                }
                            }.margin(0, 7, 0, 0)
                        }
                    }.minContentSizeX(240).margin(7)
                }.margin(4)
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (filterChanged) {
            filterChanged = false
            rebuildWidgets()
            // 作り直すと検索欄のウィジェットが入れ替わるため、入力を続けられるようにフォーカスを戻すのだ～🌱
            val editBox = children().filterIsInstance<EditBox>().firstOrNull()
            if (editBox != null) {
                focused = editBox
                editBox.moveCursorToEnd(false)
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true
        if (minecraft!!.options.keyInventory.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        if (tooltipViewerKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return false
    }

}
