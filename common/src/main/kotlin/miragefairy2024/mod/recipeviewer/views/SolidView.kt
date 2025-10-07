package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
import miragefairy2024.mod.recipeviewer.view.sized

abstract class SolidView(private val size: IntPoint) : View, PlaceableView {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = this@SolidView.size
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = this@SolidView.assemble(offset, viewPlacer)
                }
            }
        }
    }

    protected open fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(size))
}
