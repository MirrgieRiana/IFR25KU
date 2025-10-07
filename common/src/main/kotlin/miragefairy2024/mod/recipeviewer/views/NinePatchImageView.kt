package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
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
) : View, PlaceableView {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint.Companion.ZERO
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = maxSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this@NinePatchImageView, offset.sized(size))
                }
            }
        }
    }
}
