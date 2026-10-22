package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.util.Remover

class MarginView(private val xMin: Int, private val xMax: Int, private val yMin: Int, private val yMax: Int) : WrapperView() {

    override fun calculateContentSizeXImpl() = super.calculateContentSizeXImpl() + xMin + xMax
    override fun calculateContentSizeYImpl() = super.calculateContentSizeYImpl() + yMin + yMax

    override fun createChildRegionSizeXFunction(): (Int) -> Int = { actualSizeX - xMin - xMax }
    override fun createChildRegionSizeYFunction(): (Int) -> Int = { actualSizeY - yMin - yMax }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return childView.attachTo(offset.offset(xMin, yMin), viewPlacer)
    }

}

fun <P> Child<P, *>.margin(xMin: Int, xMax: Int, yMin: Int, yMax: Int) = this.wrap(MarginView(xMin, xMax, yMin, yMax))
fun <P> Child<P, *>.margin(x: Int, y: Int) = this.margin(x, x, y, y)
fun <P> Child<P, *>.margin(a: Int) = this.margin(a, a)
