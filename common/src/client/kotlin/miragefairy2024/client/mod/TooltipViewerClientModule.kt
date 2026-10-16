package miragefairy2024.client.mod

import com.mojang.blaze3d.platform.InputConstants
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.screen.ViewScreen
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
import miragefairy2024.mod.recipeviewer.views.TextBoxView
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

private const val ROOT_MARGIN = 4

private const val PANEL_MARGIN = 7

/** 内枠の横幅は、ツールチップが読める幅へ固定するのだ～🌱 */
private const val INNER_WIDTH = 240

/** 検索欄の高さは、バニラのテキスト入力と同じなのだ～🌱 */
private const val TEXT_BOX_HEIGHT = 20

private const val SCROLLBAR_THICKNESS = 5

private const val SCROLLBAR_ARGB = 0xA0000000.toInt()

/** スクロールの中身の右の余白は、スクロールバーの幅より2px広いのだ～🌱 */
private const val SCROLL_CONTENT_MARGIN_RIGHT = 7

/** ツールチップの背景の内側の余白は、バニラのツールチップと同じなのだ～🌱 */
private const val TOOLTIP_MARGIN = 4

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

                    view += NinePatchImageView(NinePatchTextureCard.PANEL.texture, 5, 6, 5, 5, 6, 5)

                    // 内枠なのだ～🌱
                    view += YListView().configure {
                        view.sizingX = Sizing.FILL
                        view.sizingY = Sizing.FILL

                        // 検索欄なのだ～🌱
                        view += TextBoxView(TEXT_BOX_HEIGHT).also {
                            it.hint = text { TOOLTIP_VIEWER_EXAMPLE_KEY_TRANSLATION("0,1,2") }
                            it.text = filter
                            it.onChanged.register { text ->
                                filter = text
                                filterChanged = true
                            }
                        }

                        view += YSpaceView(2)

                        // スクロールなのだ～🌱
                        view += ScrollView(SCROLLBAR_THICKNESS, ScrollbarStyle.Flat(SCROLLBAR_ARGB)).configure {
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
                                            }.margin(TOOLTIP_MARGIN)
                                        }
                                    }
                                }
                            }.margin(0, SCROLL_CONTENT_MARGIN_RIGHT, 0, 0)
                        }
                    }.minContentSizeX(INNER_WIDTH).margin(PANEL_MARGIN)
                }.margin(ROOT_MARGIN)
            }
        }
    }

    override fun render(guiGraphics: GuiGraphics, i: Int, j: Int, f: Float) {
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
        super.render(guiGraphics, i, j, f)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (minecraft!!.options.keyInventory.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        if (tooltipViewerKeyMappingCard.keyMapping.matches(keyCode, scanCode)) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

}
