package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import net.minecraft.client.gui.GuiGraphics

object FilledRectangleViewRenderer : ViewRenderer<FilledRectangleView> {
    override fun render(view: FilledRectangleView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        graphics.fill(bounds.x, bounds.y, bounds.x + bounds.sizeX, bounds.y + bounds.sizeY, view.color.value)
    }
}
