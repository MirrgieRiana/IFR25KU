package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
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
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                childrenWithMinSize.sumOf { it.minSize.x },
                (childrenWithMinSize.maxOfOrNull { it.minSize.y } ?: 0) atLeast minHeight,
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val remainingX = maxSize.x - minSize.x
                val totalWeight = childrenWithMinSize.sumOf { it.position.weight }
                val childrenWithSize = run {
                    var childX = 0.0
                    childrenWithMinSize.map {
                        val nextChildX = childX + it.minSize.x + if (totalWeight > 0.0) remainingX * (it.position.weight / totalWeight) else 0.0
                        val childWithSize = it.withSize(IntPoint(nextChildX.roundToInt() - childX.roundToInt(), maxSize.y))
                        childX = nextChildX
                        childWithSize
                    }
                }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        childrenWithSize.sumOf { it.size.x },
                        (childrenWithSize.maxOfOrNull { it.size.y } ?: 0) atLeast minHeight,
                    )

                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        var childX = 0
                        return childrenWithSize.map {
                            val childY = when (it.position.alignment) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.y - it.size.y) / 2
                                Alignment.END -> size.y - it.size.y
                            }
                            val remover = it.assemble(offset.offset(childX, childY), viewPlacer)
                            childX += it.size.x
                            remover
                        }.flatten()
                    }
                }
            }
        }
    }
}

class YListView : ListView() {
    var minWidth = 0
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                (childrenWithMinSize.maxOfOrNull { it.minSize.x } ?: 0) atLeast minWidth,
                childrenWithMinSize.sumOf { it.minSize.y },
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val remainingY = maxSize.y - minSize.y
                val totalWeight = childrenWithMinSize.sumOf { it.position.weight }
                val childrenWithSize = run {
                    var childY = 0.0
                    childrenWithMinSize.map {
                        val nextChildY = childY + it.minSize.y + if (totalWeight > 0.0) remainingY * (it.position.weight / totalWeight) else 0.0
                        val childWithSize = it.withSize(IntPoint(maxSize.x, nextChildY.roundToInt() - childY.roundToInt()))
                        childY = nextChildY
                        childWithSize
                    }
                }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        (childrenWithSize.maxOfOrNull { it.size.x } ?: 0) atLeast minWidth,
                        childrenWithSize.sumOf { it.size.y },
                    )

                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        var childY = 0
                        return childrenWithSize.map {
                            val childX = when (it.position.alignment) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.x - it.size.x) / 2
                                Alignment.END -> size.x - it.size.x
                            }
                            val remover = it.assemble(offset.offset(childX, childY), viewPlacer)
                            childY += it.size.y
                            remover
                        }.flatten()
                    }
                }
            }
        }
    }
}
