package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
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
    override fun calculateMinSizeImpl() = IntPoint.Companion.ZERO
    override fun calculateSizeImpl(regionSize: IntPoint) = regionSize
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(calculatedSize))
}
