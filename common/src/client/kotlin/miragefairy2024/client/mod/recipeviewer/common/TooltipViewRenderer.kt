package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.TooltipView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

object TooltipViewRenderer : ViewRenderer<TooltipView> {
    override fun render(view: TooltipView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        if (isHovered) {
            val tooltip = view.tooltipProvider(mouseX, mouseY)
            if (tooltip.isNotEmpty()) {
                Minecraft.getInstance().screen?.setTooltipForNextRenderPass(tooltip.map { it.visualOrderText })
            }
        }
    }
}
