package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.util.Remover
import kotlin.math.roundToInt

abstract class ListView : ContainerView<ListView.Position>() {

    class Position {
        var alignmentX = Alignment.START
        var alignmentY = Alignment.START
        var weight = 0.0
        var ignoreLayoutX = false
        var ignoreLayoutY = false
    }

    override fun createDefaultPosition() = Position()


    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.WRAP

    protected val Child<Position, *>.reservedSizeX get() = if (position.ignoreLayoutX) 0 else view.contentSize.x
    protected val Child<Position, *>.reservedSizeY get() = if (position.ignoreLayoutY) 0 else view.contentSize.y

}

class XListView : ListView() {

    override fun calculateContentSize(): IntPoint {
        return IntPoint(
            children.sumOf { it.reservedSizeX },
            children.maxOfOrNull { it.reservedSizeY } ?: 0,
        )
    }

    override fun calculateChildrenActualSize() {
        val remaining = (actualSize.x - contentSize.x).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        children.calculateActualSize {
            val old = x
            val new = old + it.reservedSizeX.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            val size = new.roundToInt() - old.roundToInt()

            IntPoint(size, actualSize.y)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (actualSize.x - contentSize.x).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        return children.attachTo(viewPlacer) {
            val old = x
            val new = old + it.reservedSizeX.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            val size = new.roundToInt() - old.roundToInt()

            val childX = old.roundToInt() + when (it.position.alignmentX) {
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

    override fun calculateContentSize(): IntPoint {
        return IntPoint(
            children.maxOfOrNull { it.reservedSizeX } ?: 0,
            children.sumOf { it.reservedSizeY },
        )
    }

    override fun calculateChildrenActualSize() {
        val remaining = (actualSize.y - contentSize.y).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        children.calculateActualSize {
            val old = y
            val new = old + it.reservedSizeY.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            val size = new.roundToInt() - old.roundToInt()

            IntPoint(actualSize.x, size)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (actualSize.y - contentSize.y).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        return children.attachTo(viewPlacer) {
            val old = y
            val new = old + it.reservedSizeY.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            val size = new.roundToInt() - old.roundToInt()

            val childY = old.roundToInt() + when (it.position.alignmentY) {
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
