package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ImageView
import net.minecraft.client.gui.GuiGraphics

object ImageViewRenderer : ViewRenderer<ImageView> {
    override fun render(view: ImageView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        graphics.blit(
            view.texture.id,
            bounds.x,
            bounds.y,
            view.texture.bounds.x.toFloat(),
            view.texture.bounds.y.toFloat(),
            view.texture.bounds.sizeX,
            view.texture.bounds.sizeY,
            view.texture.size.x,
            view.texture.size.y,
        )
    }
}
