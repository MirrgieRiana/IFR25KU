package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.mixins.api.SlotScissorAreaHolder
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.minus
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput

/**
 * スクロールの中へ置いたスロットの位置を、スクロール量に追従させるのだ～🌱
 * バニラの画面は、ウィジェットを全部描いた後にスロットを描くため、この書き込みは同じフレームの描画へ届くのだ～🌱
 */
class MenuSlotWidget(
    private val view: MenuSlotView,
    viewBounds: IntRectangle,
    private val slotOrigin: IntPoint,
    private val context: ScrollViewPlacerContext,
) : AbstractWidget(viewBounds.x, viewBounds.y, viewBounds.sizeX, viewBounds.sizeY, text { empty() }) {

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(x - slotOrigin.x + view.margin.x, y - slotOrigin.y + view.margin.y)
        val scissorArea = context.getScissorArea() - slotOrigin
        (view.slot as SlotScissorAreaHolder).`mirageFairy2024$setScissorArea`(intArrayOf(scissorArea.x, scissorArea.y, scissorArea.x2, scissorArea.y2))
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

}

interface ScrollViewPlacerContext : VanillaViewPlacerContext {
    fun getScissorArea(): IntRectangle
}
