package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.minus
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.plus

class MarginView(private val xMin: Int, private val xMax: Int, private val yMin: Int, private val yMax: Int) : SingleView() {
    override fun getMinSize(childWithMinSize: ChildWithMinSize) = childWithMinSize.minSize.plus(xMin + xMax, yMin + yMax)
    override fun getChildWithSize(childWithMinSize: ChildWithMinSize, maxSize: IntPoint) = childWithMinSize.withSize(maxSize.minus(xMin + xMax, yMin + yMax))
    override fun getSize(childWithSize: ChildWithSize) = childWithSize.size.plus(xMin + xMax, yMin + yMax)
    override fun assemble(childWithSize: ParentView<Unit>.ChildWithSize, offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = childWithSize.assemble(offset.offset(xMin, yMin), viewPlacer)
}

fun MarginView(x: Int, y: Int) = MarginView(x, x, y, y)
fun MarginView(margin: Int) = MarginView(margin, margin)
