package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.size
import miragefairy2024.util.Remover
import miragefairy2024.util.flatten

class AbsoluteView(private val size: IntPoint) : ContainerView<AbsoluteView.Position>() {
    sealed class Position {
        abstract fun getMaxSize(childMinSize: IntPoint, regionSize: IntPoint): IntPoint
        abstract fun getOffset(): IntPoint
    }

    data object Fill : Position() {
        override fun getMaxSize(childMinSize: IntPoint, regionSize: IntPoint) = regionSize
        override fun getOffset() = IntPoint.Companion.ZERO
    }

    data class Offset(@JvmField val offset: IntPoint) : Position() {
        override fun getMaxSize(childMinSize: IntPoint, regionSize: IntPoint) = childMinSize
        override fun getOffset() = offset
    }

    data class Bounds(val bounds: IntRectangle) : Position() {
        override fun getMaxSize(childMinSize: IntPoint, regionSize: IntPoint) = bounds.size
        override fun getOffset() = bounds.offset
    }


    override fun createDefaultPosition() = Fill


    private lateinit var childrenWithMinSize: List<ParentView<Position>.ChildWithMinSize>

    override fun calculateMinSizeImpl(): IntPoint {
        childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return size
    }


    private lateinit var childrenWithSize: List<ParentView<Position>.ChildWithSize>

    override fun calculateSizeImpl(regionSize: IntPoint): IntPoint {
        childrenWithSize = childrenWithMinSize.map { it.withSize(it.child.position.getMaxSize(it.minSize, regionSize)) }
        return size
    }


    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return childrenWithSize.map {
            it.attachTo(offset + it.child.position.getOffset(), viewPlacer)
        }.flatten()
    }

}
