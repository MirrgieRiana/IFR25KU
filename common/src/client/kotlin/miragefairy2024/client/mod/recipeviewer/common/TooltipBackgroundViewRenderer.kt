package miragefairy2024.client.mod.recipeviewer.common

import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil

object TooltipBackgroundViewRenderer : ViewRenderer<TooltipBackgroundView, Unit> {
    override fun createState(view: TooltipBackgroundView) = Unit
    override fun render(view: TooltipBackgroundView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        // 左右・上下の枠線が重ならずに収まる最小寸法は 4 × 4 GUI 単位なのだ～🌱
        if (bounds.sizeX < 4 || bounds.sizeY < 4) return
        @Suppress("DEPRECATION")
        graphics.drawManaged {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            if (bounds.sizeX >= 8 && bounds.sizeY >= 8) {
                TooltipRenderUtil.renderTooltipBackground(
                    graphics,
                    bounds.x + 4,
                    bounds.y + 4,
                    bounds.sizeX - 8,
                    bounds.sizeY - 8,
                    0,
                )
            } else {
                renderCompactBackground(bounds, graphics)
            }
        }
    }

    private fun renderCompactBackground(bounds: IntRectangle, graphics: GuiGraphics) {
        val left = bounds.x
        val top = bounds.y
        val right = left + bounds.sizeX
        val bottom = top + bounds.sizeY
        val backgroundColor = 0xF0100010.toInt()
        val borderColorTop = 0x505000FF
        val borderColorBottom = 0x5028007F

        graphics.fill(left + 1, top, right - 1, bottom, backgroundColor)
        graphics.fill(left, top + 1, left + 1, bottom - 1, backgroundColor)
        graphics.fill(right - 1, top + 1, right, bottom - 1, backgroundColor)
        graphics.fillGradient(left + 1, top + 2, left + 2, bottom - 2, borderColorTop, borderColorBottom)
        graphics.fillGradient(right - 2, top + 2, right - 1, bottom - 2, borderColorTop, borderColorBottom)
        graphics.fill(left + 1, top + 1, right - 1, top + 2, borderColorTop)
        graphics.fill(left + 1, bottom - 2, right - 1, bottom - 1, borderColorBottom)
    }
}
