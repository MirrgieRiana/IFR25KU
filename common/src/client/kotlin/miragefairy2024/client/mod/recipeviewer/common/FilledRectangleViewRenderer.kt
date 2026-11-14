package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.x1
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y1
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.FilledRectangleView
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics

object FilledRectangleViewRenderer : ViewRenderer<FilledRectangleView, Unit> {
    override fun createState(view: FilledRectangleView) = Unit
    override fun render(view: FilledRectangleView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        graphics.fill(bounds.x1, bounds.y1, bounds.x2, bounds.y2, view.color.value)
    }
}
