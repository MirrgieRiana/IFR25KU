package miragefairy2024.client.mod.recipeviewer.common

import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil

object TooltipBackgroundViewRenderer : ViewRenderer<TooltipBackgroundView> {
    override fun render(view: TooltipBackgroundView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        @Suppress("DEPRECATION")
        graphics.drawManaged {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            TooltipRenderUtil.renderTooltipBackground(graphics, bounds.x + 4, bounds.y + 4, bounds.sizeX - 8, bounds.sizeY - 8, 0)
        }
    }
}
