package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ScrollbarRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import net.minecraft.client.gui.GuiGraphics

object FlatScrollbarRenderer : ScrollbarRenderer {
    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun render(graphics: GuiGraphics, bounds: IntRectangle, color: Int) {
        graphics.fill(bounds.x, bounds.y, bounds.x2, bounds.y2, color)
    }
}
