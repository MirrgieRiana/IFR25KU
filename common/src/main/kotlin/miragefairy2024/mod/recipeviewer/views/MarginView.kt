package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset

class MarginView(private val xMin: Int, private val xMax: Int, private val yMin: Int, private val yMax: Int) : WrapperView() {

    override fun evaluateContentSizeXImpl() = super.evaluateContentSizeXImpl() + xMin + xMax
    override fun evaluateContentSizeYImpl() = super.evaluateContentSizeYImpl() + yMin + yMax

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() - xMin - xMax
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() - yMin - yMax

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return childView.attachTo(offset.offset(xMin, yMin), viewPlacer)
    }

}

fun <P> Child<P, *>.margin(xMin: Int, xMax: Int, yMin: Int, yMax: Int) = this.wrap(MarginView(xMin, xMax, yMin, yMax))
fun <P> Child<P, *>.margin(x: Int, y: Int) = this.margin(x, x, y, y)
fun <P> Child<P, *>.margin(a: Int) = this.margin(a, a)
