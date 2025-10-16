package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import kotlin.math.roundToInt

abstract class ListView : ContainerView<ListView.Position>() {

    class Position {
        var alignmentX = Alignment.START
        var alignmentY = Alignment.START
        var weight = 0.0
    }

    override fun createDefaultPosition() = Position()

    var fillX = false
    var fillY = false

}

class XListView : ListView() {

    override fun calculateContentSizeImpl(): IntPoint {
        return IntPoint(
            children.sumOf { it.view.contentSize.x },
            (children.maxOfOrNull { it.view.contentSize.y } ?: 0),
        )
    }

    override fun calculateActualSizeImpl(regionSize: IntPoint): IntPoint {
        return IntPoint(
            if (fillX) regionSize.x else contentSize.x,
            if (fillY) regionSize.y else contentSize.y,
        )
    }

    override fun calculateChildrenActualSize() {
        val remaining = (actualSize.x - contentSize.x).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        children.calculateActualSize {
            val old = x
            val new = old + it.view.contentSize.x.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            val size = new.roundToInt() - old.roundToInt()

            IntPoint(size, actualSize.y)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        val remaining = (actualSize.x - contentSize.x).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        children.attachTo(viewPlacer) {
            val old = x
            val new = old + it.view.contentSize.x.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            val size = new.roundToInt() - old.roundToInt()

            val childX = when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.actualSize.x) / 2
                Alignment.END -> size - it.view.actualSize.x
            }
            val childY = when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSize.y - it.view.actualSize.y) / 2
                Alignment.END -> actualSize.y - it.view.actualSize.y
            }
            offset.offset(childX, childY)
        }
    }

}

class YListView : ListView() {

    override fun calculateContentSizeImpl(): IntPoint {
        return IntPoint(
            (children.maxOfOrNull { it.view.contentSize.x } ?: 0),
            children.sumOf { it.view.contentSize.y },
        )
    }

    override fun calculateActualSizeImpl(regionSize: IntPoint): IntPoint {
        return IntPoint(
            if (fillX) regionSize.x else contentSize.x,
            if (fillY) regionSize.y else contentSize.y,
        )
    }

    override fun calculateChildrenActualSize() {
        val remaining = (actualSize.y - contentSize.y).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        children.calculateActualSize {
            val old = y
            val new = old + it.view.contentSize.y.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            val size = new.roundToInt() - old.roundToInt()

            IntPoint(actualSize.x, size)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        val remaining = (actualSize.y - contentSize.y).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        children.attachTo(viewPlacer) {
            val old = y
            val new = old + it.view.contentSize.y.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            val size = new.roundToInt() - old.roundToInt()

            val childY = when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.actualSize.y) / 2
                Alignment.END -> size - it.view.actualSize.y
            }
            val childX = when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSize.x - it.view.actualSize.x) / 2
                Alignment.END -> actualSize.x - it.view.actualSize.x
            }
            offset.offset(childX, childY)
        }
    }

}
