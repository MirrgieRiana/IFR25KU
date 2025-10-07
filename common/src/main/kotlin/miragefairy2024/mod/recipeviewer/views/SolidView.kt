package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized

abstract class SolidView(private val size: IntPoint) : AbstractView(), PlaceableView {
    override fun calculateMinSizeImpl() = size
    override fun calculateSizeImpl(regionSize: IntPoint) = size
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(size))
}
