package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

class EmptyView : FixedView(IntPoint.ZERO) {
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = ViewController.NONE
}
