package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.RenderOverflow
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.util.drawNinePatchImage
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.Slot
import mirrg.kotlin.helium.atLeast
import net.minecraft.client.gui.GuiGraphics

object NinePatchImageViewRenderer : ViewRenderer<NinePatchImageView, Unit> {
    override fun createState(view: NinePatchImageView) = Unit

    override fun getRenderOverflow(view: NinePatchImageView, bounds: IntRectangle): RenderOverflow {
        return RenderOverflow(
            view.xEndSize - bounds.sizeX atLeast 0,
            view.xStartSize - bounds.sizeX atLeast 0,
            view.yEndSize - bounds.sizeY atLeast 0,
            view.yStartSize - bounds.sizeY atLeast 0,
        )
    }

    override fun render(view: NinePatchImageView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        graphics.drawNinePatchImage(
            bounds,
            view.textureId,
            view.textureWidth, view.xStartSize, view.xMiddleSize, view.xEndSize,
            view.textureHeight, view.yStartSize, view.yMiddleSize, view.yEndSize,
            view.repeatX, view.repeatY,
        )
    }
}
