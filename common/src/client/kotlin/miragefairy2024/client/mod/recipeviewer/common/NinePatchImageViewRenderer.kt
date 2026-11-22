package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.util.renderNinePatchImage
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics

object NinePatchImageViewRenderer : ViewRenderer<NinePatchImageView, Unit> {
    override fun createState(view: NinePatchImageView) = Unit
    override fun render(view: NinePatchImageView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        graphics.renderNinePatchImage(
            bounds,
            view.textureId,
            view.textureWidth, view.xStartSize, view.xMiddleSize, view.xEndSize,
            view.textureHeight, view.yStartSize, view.yMiddleSize, view.yEndSize,
            view.repeatX, view.repeatY,
        )
    }
}
