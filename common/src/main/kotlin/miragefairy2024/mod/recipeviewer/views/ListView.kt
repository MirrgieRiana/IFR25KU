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

    protected val Child<Position, *>.reservedSizeX get() = if (position.ignoreLayoutX) 0 else view.contentSizeX
    protected val Child<Position, *>.reservedSizeY get() = if (position.ignoreLayoutY) 0 else view.contentSizeY

}

class XListView : ListView() {

    override fun calculateContentSizeXImpl() = children.sumOf { it.reservedSizeX }
    override fun calculateContentSizeYImpl() = children.maxOfOrNull { it.reservedSizeY } ?: 0

    override fun calculateChildrenActualSizeX() {
        val remaining = (actualSizeX - contentSizeX).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        children.calculateActualSizeX {
            val old = x
            val new = old + it.reservedSizeX.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            new.roundToInt() - old.roundToInt()
        }
    }

    override fun calculateChildrenActualSizeY() {
        children.calculateActualSizeY { actualSizeY }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (actualSizeX - contentSizeX).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        return children.attachTo(viewPlacer) {
            val old = x
            val new = old + it.reservedSizeX.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            x = new
            val size = new.roundToInt() - old.roundToInt()

            val childX = old.roundToInt() + when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.actualSizeX) / 2
                Alignment.END -> size - it.view.actualSizeX
            }
            val childY = when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSizeY - it.view.actualSizeY) / 2
                Alignment.END -> actualSizeY - it.view.actualSizeY
            }
            offset.offset(childX, childY)
        }
    }

}

class YListView : ListView() {

    override fun calculateContentSizeXImpl() = children.maxOfOrNull { it.reservedSizeX } ?: 0
    override fun calculateContentSizeYImpl() = children.sumOf { it.reservedSizeY }

    override fun calculateChildrenActualSizeX() {
        children.calculateActualSizeX { actualSizeX }
    }

    override fun calculateChildrenActualSizeY() {
        val remaining = (actualSizeY - contentSizeY).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        children.calculateActualSizeY {
            val old = y
            val new = old + it.reservedSizeY.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            new.roundToInt() - old.roundToInt()
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (actualSizeY - contentSizeY).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        return children.attachTo(viewPlacer) {
            val old = y
            val new = old + it.reservedSizeY.toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
            y = new
            val size = new.roundToInt() - old.roundToInt()

            val childY = old.roundToInt() + when (it.position.alignmentY) {
                Alignment.START -> 0
                Alignment.CENTER -> (size - it.view.actualSizeY) / 2
                Alignment.END -> size - it.view.actualSizeY
            }
            val childX = when (it.position.alignmentX) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSizeX - it.view.actualSizeX) / 2
                Alignment.END -> actualSizeX - it.view.actualSizeX
            }
            offset.offset(childX, childY)
        }
    }

}
