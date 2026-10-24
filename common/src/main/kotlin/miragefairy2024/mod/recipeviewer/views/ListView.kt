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

// TODO mirrg

private class Cache<T : Any>(private val initializer: () -> T) {

    private var cache: T? = null

    fun evaluate(): T {
        val old = cache
        return if (old != null) {
            old
        } else {
            val new = initializer()
            cache = new
            new
        }
    }

    fun reset() {
        cache = null
    }

}

abstract class ListView : ContainerView<ListView.Position>() {

    class Position {

        var alignmentX = Alignment.START
        var alignmentY = Alignment.START

        var weight = 0.0
            set(value) {
                if (!value.isFinite() || value < 0.0) throw IllegalArgumentException("weight must be finite and non-negative")
                field = value
            }

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

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
        totalWeight.reset()
        childRegionSizesX.reset()
    }

    override fun evaluateOwnContentSizeX() = children.sumOf { it.evaluateReservedSizeX() }
    override fun evaluateOwnContentSizeY() = children.maxOfOrNull { it.evaluateReservedSizeY() } ?: 0

    private var totalWeight = Cache { children.sumOf { it.position.weight } }
    private var childRegionSizesX = Cache {
        val totalWeight = totalWeight.evaluate()
        if (totalWeight > 0.0) {
            val remaining = (evaluateActualSizeX() - evaluateContentSizeX()).toDouble()
            var consumed = 0.0
            children.map {
                val oldConsumed = consumed
                val newConsumed = oldConsumed + remaining * it.position.weight / totalWeight
                consumed = newConsumed
                it.evaluateReservedSizeX() + (newConsumed.roundToInt() - oldConsumed.roundToInt())
            }
        } else {
            children.map {
                it.evaluateReservedSizeX()
            }
        }
    }

    override fun evaluateChildRegionSizeX(index: Int): Int {
        if (sizingX == Sizing.WRAP) return children[index].evaluateReservedSizeX() // WRAP の場合は領域を分配しないからレイアウト計算が不要なのだ～🌱
        if (children[index].position.weight == 0.0) return children[index].evaluateReservedSizeX() // 子が分配を要求しないからレイアウト計算が不要なのだ～🌱
        return childRegionSizesX.evaluate()[index]
    }

    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY()

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val totalWeight = totalWeight.evaluate()
        if (totalWeight > 0.0) {
            val remaining = (evaluateActualSizeX() - evaluateContentSizeX()).toDouble()
            var consumed = 0.0
            var x = 0
            return children.attachTo(viewPlacer) {
                val oldConsumed = consumed
                val newConsumed = oldConsumed + remaining * it.position.weight / totalWeight
                consumed = newConsumed
                val size = it.evaluateReservedSizeX() + (newConsumed.roundToInt() - oldConsumed.roundToInt())

                val childX = x + when (it.position.alignmentX) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (size - it.view.evaluateActualSizeX()) / 2
                    Alignment.END -> size - it.view.evaluateActualSizeX()
                }
                val childY = when (it.position.alignmentY) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (evaluateActualSizeY() - it.view.evaluateActualSizeY()) / 2
                    Alignment.END -> evaluateActualSizeY() - it.view.evaluateActualSizeY()
                }
                x += size
                offset.offset(childX, childY)
            }
        } else {
            var x = 0
            return children.attachTo(viewPlacer) {
                val size = it.evaluateReservedSizeX()
                val childX = x + when (it.position.alignmentX) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (size - it.view.evaluateActualSizeX()) / 2
                    Alignment.END -> size - it.view.evaluateActualSizeX()
                }
                val childY = when (it.position.alignmentY) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (evaluateActualSizeY() - it.view.evaluateActualSizeY()) / 2
                    Alignment.END -> evaluateActualSizeY() - it.view.evaluateActualSizeY()
                }
                x += size
                offset.offset(childX, childY)
            }
        }
    }

}

class YListView : ListView() {

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        super.initSizeCalculation(renderingProxy, regionSizeXProvider, regionSizeYProvider)
        totalWeight.reset()
        childRegionSizesY.reset()
    }

    override fun evaluateOwnContentSizeX() = children.maxOfOrNull { it.evaluateReservedSizeX() } ?: 0
    override fun evaluateOwnContentSizeY() = children.sumOf { it.evaluateReservedSizeY() }

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX()

    private var totalWeight = Cache { children.sumOf { it.position.weight } }
    private var childRegionSizesY = Cache {
        val totalWeight = totalWeight.evaluate()
        if (totalWeight > 0.0) {
            val remaining = (evaluateActualSizeY() - evaluateContentSizeY()).toDouble()
            var consumed = 0.0
            children.map {
                val oldConsumed = consumed
                val newConsumed = oldConsumed + remaining * it.position.weight / totalWeight
                consumed = newConsumed
                it.evaluateReservedSizeY() + (newConsumed.roundToInt() - oldConsumed.roundToInt())
            }
        } else {
            children.map {
                it.evaluateReservedSizeY()
            }
        }
    }

    override fun evaluateChildRegionSizeY(index: Int): Int {
        if (sizingY == Sizing.WRAP) return children[index].evaluateReservedSizeY() // WRAP の場合は領域を分配しないからレイアウト計算が不要なのだ～🌱
        if (children[index].position.weight == 0.0) return children[index].evaluateReservedSizeY() // 子が分配を要求しないからレイアウト計算が不要なのだ～🌱
        return childRegionSizesY.evaluate()[index]
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        val totalWeight = totalWeight.evaluate()
        if (totalWeight > 0.0) {
            val remaining = (evaluateActualSizeY() - evaluateContentSizeY()).toDouble()
            var consumed = 0.0
            var y = 0
            return children.attachTo(viewPlacer) {
                val oldConsumed = consumed
                val newConsumed = oldConsumed + remaining * it.position.weight / totalWeight
                consumed = newConsumed
                val size = it.evaluateReservedSizeY() + (newConsumed.roundToInt() - oldConsumed.roundToInt())

                val childY = y + when (it.position.alignmentY) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (size - it.view.evaluateActualSizeY()) / 2
                    Alignment.END -> size - it.view.evaluateActualSizeY()
                }
                val childX = when (it.position.alignmentX) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (evaluateActualSizeX() - it.view.evaluateActualSizeX()) / 2
                    Alignment.END -> evaluateActualSizeX() - it.view.evaluateActualSizeX()
                }
                y += size
                offset.offset(childX, childY)
            }
        } else {
            var y = 0
            return children.attachTo(viewPlacer) {
                val size = it.evaluateReservedSizeY()
                val childY = y + when (it.position.alignmentY) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (size - it.view.evaluateActualSizeY()) / 2
                    Alignment.END -> size - it.view.evaluateActualSizeY()
                }
                val childX = when (it.position.alignmentX) {
                    Alignment.START -> 0
                    Alignment.CENTER -> (evaluateActualSizeX() - it.view.evaluateActualSizeX()) / 2
                    Alignment.END -> evaluateActualSizeX() - it.view.evaluateActualSizeX()
                }
                y += size
                offset.offset(childX, childY)
            }
        }
    }

}
