package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil

/** バニラの描画は枠の分を外側へ広げるため、渡す矩形をその分だけ内側へ詰めるのだ～🌱 */
private const val BORDER_SIZE = 4

object TooltipBackgroundViewRenderer : ViewRenderer<TooltipBackgroundView> {
    override fun render(view: TooltipBackgroundView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        TooltipRenderUtil.renderTooltipBackground(
            graphics,
            bounds.x + BORDER_SIZE,
            bounds.y + BORDER_SIZE,
            bounds.sizeX - BORDER_SIZE * 2,
            bounds.sizeY - BORDER_SIZE * 2,
            0,
        )
    }
}
