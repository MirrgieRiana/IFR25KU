package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import net.minecraft.resources.ResourceLocation

class NinePatchImageView(
    val textureId: ResourceLocation,
    val xStartSize: Int,
    val xMiddleSize: Int,
    val xEndSize: Int,
    val yStartSize: Int,
    val yMiddleSize: Int,
    val yEndSize: Int,
) : AbstractView(), PlaceableView {
    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL
    override fun calculateContentSize() = IntPoint.Companion.ZERO
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}
