package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class FixedWidgetView(size: IntPoint) : FixedView(size), PlaceableView {
    override fun attachTo(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, IntRectangle(x, y, actualSize.x, actualSize.y))
}
