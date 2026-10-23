package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
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

    protected fun Child<Position, *>.evaluateReservedSizeX() = if (position.ignoreLayoutX) 0 else view.evaluateContentSizeX()
    protected fun Child<Position, *>.evaluateReservedSizeY() = if (position.ignoreLayoutY) 0 else view.evaluateContentSizeY()

}

class XListView : ListView() {

    private var childRegionSizeXFunction: ((Int) -> Int)? = null

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        childRegionSizeXFunction = null
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
    }

    override fun getOwnContentSizeX() = children.sumOf { it.evaluateReservedSizeX() }
    override fun getOwnContentSizeY() = children.maxOfOrNull { it.evaluateReservedSizeY() } ?: 0

    override fun evaluateChildRegionSizeX(index: Int): Int {
        val old = childRegionSizeXFunction
        if (old != null) {
            return old(index)
        } else {
            val new = createChildRegionSizeXFunction()
            childRegionSizeXFunction = new
            return new(index)
        }
    }

    private fun createChildRegionSizeXFunction(): (Int) -> Int {
        if (sizingX == Sizing.WRAP) return { children[it].evaluateReservedSizeX() }
        val totalWeight = children.sumOf { it.position.weight }
        if (!(totalWeight > 0.0)) return { children[it].evaluateReservedSizeX() }
        val regionSizes by lazy {
            val remaining = (evaluateActualSizeX() - evaluateContentSizeX()).toDouble()
            var x = 0.0
            children.map {
                val old = x
                val new = old + it.evaluateReservedSizeX().toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
                x = new
                new.roundToInt() - old.roundToInt()
            }
        }
        return { index ->
            if (children[index].position.weight == 0.0) {
                children[index].evaluateReservedSizeX()
            } else {
                regionSizes[index]
            }
        }
    }

    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY()

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (evaluateActualSizeX() - evaluateContentSizeX()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var x = 0.0
        return children.attachTo(viewPlacer) {
            val old = x
            val new = old + it.evaluateReservedSizeX().toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
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

    private var childRegionSizeYFunction: ((Int) -> Int)? = null

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        childRegionSizeYFunction = null
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
    }

    override fun getOwnContentSizeX() = children.maxOfOrNull { it.evaluateReservedSizeX() } ?: 0
    override fun getOwnContentSizeY() = children.sumOf { it.evaluateReservedSizeY() }

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX()

    override fun evaluateChildRegionSizeY(index: Int): Int {
        val old = childRegionSizeYFunction
        if (old != null) {
            return old(index)
        } else {
            val new = createChildRegionSizeYFunction()
            childRegionSizeYFunction = new
            return new(index)
        }
    }

    private fun createChildRegionSizeYFunction(): (Int) -> Int {
        if (sizingY == Sizing.WRAP) return { children[it].evaluateReservedSizeY() }
        val totalWeight = children.sumOf { it.position.weight }
        if (!(totalWeight > 0.0)) return { children[it].evaluateReservedSizeY() }
        val regionSizes by lazy {
            val remaining = (evaluateActualSizeY() - evaluateContentSizeY()).toDouble()
            var y = 0.0
            children.map {
                val old = y
                val new = old + it.evaluateReservedSizeY().toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
                y = new
                new.roundToInt() - old.roundToInt()
            }
        }
        return { index ->
            if (children[index].position.weight == 0.0) {
                children[index].evaluateReservedSizeY()
            } else {
                regionSizes[index]
            }
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val remaining = (evaluateActualSizeY() - evaluateContentSizeY()).toDouble()
        val totalWeight = children.sumOf { it.position.weight }
        var y = 0.0
        return children.attachTo(viewPlacer) {
            val old = y
            val new = old + it.evaluateReservedSizeY().toDouble() + if (totalWeight > 0.0) remaining * it.position.weight / totalWeight else 0.0
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
