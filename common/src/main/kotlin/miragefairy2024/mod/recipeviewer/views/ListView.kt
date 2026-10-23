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

    protected val Child<Position, *>.reservedSizeX get() = if (position.ignoreLayoutX) 0 else view.evaluateContentSizeX()
    protected val Child<Position, *>.reservedSizeY get() = if (position.ignoreLayoutY) 0 else view.evaluateContentSizeY()

}

class XListView : ListView() {

    override fun getOwnContentSizeX() = children.sumOf { it.reservedSizeX }
    override fun getOwnContentSizeY() = children.maxOfOrNull { it.reservedSizeY } ?: 0

    override fun createChildRegionSizeXFunction(): (Int) -> Int {
        val remaining = (evaluateActualSizeX() - evaluateContentSizeX()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        val regionSizes = children.map {
            val old = x
            val new = old + it.reservedSizeX.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            new.roundToInt() - old.roundToInt()
        }
        return { regionSizes[it] }
    }

    override fun createChildRegionSizeYFunction(): (Int) -> Int = { evaluateActualSizeY() }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (evaluateActualSizeX() - evaluateContentSizeX()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        return children.attachTo(viewPlacer) {
            val old = x
            val new = old + it.reservedSizeX.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            val size = new.roundToInt() - old.roundToInt()

            val childX = old.roundToInt() + when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.evaluateActualSizeX()) / 2
                Alignment.END -> size - it.view.evaluateActualSizeX()
            }
            val childY = when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (evaluateActualSizeY() - it.view.evaluateActualSizeY()) / 2
                Alignment.END -> evaluateActualSizeY() - it.view.evaluateActualSizeY()
            }
            offset.offset(childX, childY)
        }
    }

}

class YListView : ListView() {

    override fun getOwnContentSizeX() = children.maxOfOrNull { it.reservedSizeX } ?: 0
    override fun getOwnContentSizeY() = children.sumOf { it.reservedSizeY }

    override fun createChildRegionSizeXFunction(): (Int) -> Int = { evaluateActualSizeX() }

    override fun createChildRegionSizeYFunction(): (Int) -> Int {
        val remaining = (evaluateActualSizeY() - evaluateContentSizeY()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        val regionSizes = children.map {
            val old = y
            val new = old + it.reservedSizeY.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            new.roundToInt() - old.roundToInt()
        }
        return { regionSizes[it] }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (evaluateActualSizeY() - evaluateContentSizeY()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        return children.attachTo(viewPlacer) {
            val old = y
            val new = old + it.reservedSizeY.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            val size = new.roundToInt() - old.roundToInt()

            val childY = old.roundToInt() + when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.evaluateActualSizeY()) / 2
                Alignment.END -> size - it.view.evaluateActualSizeY()
            }
            val childX = when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (evaluateActualSizeX() - it.view.evaluateActualSizeX()) / 2
                Alignment.END -> evaluateActualSizeX() - it.view.evaluateActualSizeX()
            }
            offset.offset(childX, childY)
        }
    }

}
