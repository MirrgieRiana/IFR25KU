package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
import miragefairy2024.mod.recipeviewer.view.min

abstract class SpaceView(private val minSize: IntPoint, private val maxSize: IntPoint) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = this@SpaceView.minSize
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = maxSize min this@SpaceView.maxSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.Companion.NONE
                }
            }
        }
    }
}

class XSpaceView(minWidth: Int) : SpaceView(IntPoint(minWidth, 0), IntPoint(Int.MAX_VALUE, 0))
class YSpaceView(minHeight: Int) : SpaceView(IntPoint(0, minHeight), IntPoint(0, Int.MAX_VALUE))
