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

    protected val Child<Position, *>.reservedSizeX get() = if (position.ignoreLayoutX) 0 else view.getContentSizeX()
    protected val Child<Position, *>.reservedSizeY get() = if (position.ignoreLayoutY) 0 else view.getContentSizeY()

}

class XListView : ListView() {

    override fun getOwnContentSizeX() = children.sumOf { it.reservedSizeX }
    override fun getOwnContentSizeY() = children.maxOfOrNull { it.reservedSizeY } ?: 0

    override fun createChildRegionSizeXFunction(): (Int) -> Int {
        val remaining = (getActualSizeX() - getContentSizeX()).toDouble()
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

    override fun createChildRegionSizeYFunction(): (Int) -> Int = { getActualSizeY() }

    override fun calculateChildrenActualSizeX() {
        children.calculateActualSizeX()
    }

    override fun calculateChildrenActualSizeY() {
        children.calculateActualSizeY()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (getActualSizeX() - getContentSizeX()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        return children.attachTo(viewPlacer) {
            val old = x
            val new = old + it.reservedSizeX.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            val size = new.roundToInt() - old.roundToInt()

            val childX = old.roundToInt() + when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.getActualSizeX()) / 2
                Alignment.END -> size - it.view.getActualSizeX()
            }
            val childY = when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (getActualSizeY() - it.view.getActualSizeY()) / 2
                Alignment.END -> getActualSizeY() - it.view.getActualSizeY()
            }
            offset.offset(childX, childY)
        }
    }

}

class YListView : ListView() {

    override fun getOwnContentSizeX() = children.maxOfOrNull { it.reservedSizeX } ?: 0
    override fun getOwnContentSizeY() = children.sumOf { it.reservedSizeY }

    override fun createChildRegionSizeXFunction(): (Int) -> Int = { getActualSizeX() }

    override fun calculateChildrenActualSizeX() {
        children.calculateActualSizeX()
    }

    override fun createChildRegionSizeYFunction(): (Int) -> Int {
        val remaining = (getActualSizeY() - getContentSizeY()).toDouble()
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

    override fun calculateChildrenActualSizeY() {
        children.calculateActualSizeY()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (getActualSizeY() - getContentSizeY()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        return children.attachTo(viewPlacer) {
            val old = y
            val new = old + it.reservedSizeY.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            val size = new.roundToInt() - old.roundToInt()

            val childY = old.roundToInt() + when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.getActualSizeY()) / 2
                Alignment.END -> size - it.view.getActualSizeY()
            }
            val childX = when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (getActualSizeX() - it.view.getActualSizeX()) / 2
                Alignment.END -> getActualSizeX() - it.view.getActualSizeX()
            }
            offset.offset(childX, childY)
        }
    }

}
