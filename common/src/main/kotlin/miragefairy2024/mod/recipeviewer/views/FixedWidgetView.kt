package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class FixedWidgetView(size: IntPoint) : View, PlaceableView {
    override fun calculateActualSize(renderingProxy: RenderingProxy) = Unit
    override val contentSize = size
    override val actualSize = size
    override fun attachTo(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, IntRectangle(x, y, actualSize.x, actualSize.y))
}
