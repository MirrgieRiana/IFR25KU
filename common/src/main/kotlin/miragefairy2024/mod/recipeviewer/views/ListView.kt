package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.flatten
import miragefairy2024.mod.recipeviewer.view.offset
import mirrg.kotlin.helium.atLeast
import kotlin.math.roundToInt

abstract class ListView : ContainerView<ListView.Position>() {
    class Position(var alignment: Alignment, var weight: Double)

    override fun createDefaultPosition() = Position(Alignment.START, 0.0)
}

class XListView : ListView() {

    var minHeight = 0

    private lateinit var childrenWithMinSize: List<ParentView<Position>.ChildWithMinSize>

    override fun calculateMinSizeImpl(): IntPoint {
        childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return IntPoint(
            childrenWithMinSize.sumOf { it.minSize.x },
            (childrenWithMinSize.maxOfOrNull { it.minSize.y } ?: 0) atLeast minHeight,
        )
    }


    private lateinit var childrenWithSize: List<ParentView<Position>.ChildWithSize>

    override fun calculateSizeImpl(regionSize: IntPoint): IntPoint {
        val remainingX = regionSize.x - calculatedMinSize.x
        val totalWeight = childrenWithMinSize.sumOf { it.child.position.weight }
        childrenWithSize = run {
            var childX = 0.0
            childrenWithMinSize.map {
                val nextChildX = childX + it.minSize.x + if (totalWeight > 0.0) remainingX * (it.child.position.weight / totalWeight) else 0.0
                val childWithSize = it.withSize(IntPoint(nextChildX.roundToInt() - childX.roundToInt(), regionSize.y))
                childX = nextChildX
                childWithSize
            }
        }
        return IntPoint(
            childrenWithSize.sumOf { it.size.x },
            (childrenWithSize.maxOfOrNull { it.size.y } ?: 0) atLeast minHeight,
        )
    }


    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        var childX = 0
        return childrenWithSize.map {
            val childY = when (it.child.position.alignment) {
                Alignment.START -> 0
                Alignment.CENTER -> (calculatedSize.y - it.size.y) / 2
                Alignment.END -> calculatedSize.y - it.size.y
            }
            val remover = it.attachTo(offset.offset(childX, childY), viewPlacer)
            childX += it.size.x
            remover
        }.flatten()
    }

}

class YListView : ListView() {

    var minWidth = 0

    private lateinit var childrenWithMinSize: List<ParentView<Position>.ChildWithMinSize>

    override fun calculateMinSizeImpl(): IntPoint {
        childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return IntPoint(
            (childrenWithMinSize.maxOfOrNull { it.minSize.x } ?: 0) atLeast minWidth,
            childrenWithMinSize.sumOf { it.minSize.y },
        )
    }


    private lateinit var childrenWithSize: List<ParentView<Position>.ChildWithSize>

    override fun calculateSizeImpl(regionSize: IntPoint): IntPoint {
        val remainingY = regionSize.y - calculatedMinSize.y
        val totalWeight = childrenWithMinSize.sumOf { it.child.position.weight }
        childrenWithSize = run {
            var childY = 0.0
            childrenWithMinSize.map {
                val nextChildY = childY + it.minSize.y + if (totalWeight > 0.0) remainingY * (it.child.position.weight / totalWeight) else 0.0
                val childWithSize = it.withSize(IntPoint(regionSize.x, nextChildY.roundToInt() - childY.roundToInt()))
                childY = nextChildY
                childWithSize
            }
        }
        return IntPoint(
            (childrenWithSize.maxOfOrNull { it.size.x } ?: 0) atLeast minWidth,
            childrenWithSize.sumOf { it.size.y },
        )
    }


    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        var childY = 0
        return childrenWithSize.map {
            val childX = when (it.child.position.alignment) {
                Alignment.START -> 0
                Alignment.CENTER -> (calculatedSize.x - it.size.x) / 2
                Alignment.END -> calculatedSize.x - it.size.x
            }
            val remover = it.attachTo(offset.offset(childX, childY), viewPlacer)
            childY += it.size.y
            remover
        }.flatten()
    }

}
