package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class SpaceView(width: Int, height: Int) : FixedWidgetView(width, height) {
    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>) = Unit
}

class XSpaceView(width: Int) : SpaceView(width, 0)
class YSpaceView(height: Int) : SpaceView(0, height)
