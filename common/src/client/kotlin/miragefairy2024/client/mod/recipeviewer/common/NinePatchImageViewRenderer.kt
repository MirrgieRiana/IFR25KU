package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.util.drawNinePatchImage
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor

object NinePatchImageViewRenderer : ViewRenderer<NinePatchImageView, Unit> {
    override fun createState(view: NinePatchImageView) = Unit

    override fun render(view: NinePatchImageView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float, isFocused: Boolean) {
        graphics.drawNinePatchImage(bounds, view.ninePatchTextureCard)
    }
}
