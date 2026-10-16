package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

/**
 * スクロールの中へ置いたスロットの位置を、スクロール量に追従させるのだ～🌱
 * バニラの画面は、ウィジェットを全部描いた後にスロットを描くため、この書き込みは同じフレームの描画へ届くのだ～🌱
 */
class MenuSlotWidget(
    private val view: MenuSlotView,
    private val viewBounds: IntRectangle,
    private val scrollViewWidget: ScrollViewWidget,
) : AbstractWidget(viewBounds.x, viewBounds.y, viewBounds.sizeX, viewBounds.sizeY, Component.empty()) {

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        view.apply(IntPoint(viewBounds.x, viewBounds.y - scrollViewWidget.view.scrollAmount), scrollViewWidget.contentScissorArea)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

}
