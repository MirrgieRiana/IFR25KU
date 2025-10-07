package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.atMost

abstract class SpaceView(private val minSize: IntPoint, private val maxSize: IntPoint) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy) = minSize
    override fun calculateSize(regionSize: IntPoint) = regionSize atMost maxSize
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.Companion.NONE
}

class XSpaceView(minWidth: Int) : SpaceView(IntPoint(minWidth, 0), IntPoint(Int.MAX_VALUE, 0))
class YSpaceView(minHeight: Int) : SpaceView(IntPoint(0, minHeight), IntPoint(0, Int.MAX_VALUE))
