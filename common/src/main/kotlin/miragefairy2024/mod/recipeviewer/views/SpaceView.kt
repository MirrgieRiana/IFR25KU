package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

class SpaceView(size: IntPoint) : FixedView(size) {
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Unit
}

fun SpaceView() = SpaceView(IntPoint.ZERO)

@Suppress("FunctionName")
fun XSpaceView(sizeX: Int) = SpaceView(IntPoint(sizeX, 0))

@Suppress("FunctionName")
fun YSpaceView(sizeY: Int) = SpaceView(IntPoint(0, sizeY))
