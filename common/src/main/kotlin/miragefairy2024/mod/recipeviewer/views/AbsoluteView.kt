package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
import miragefairy2024.mod.recipeviewer.view.flatten
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.size

class AbsoluteView(private val size: IntPoint) : ContainerView<AbsoluteView.Position>() {
    sealed class Position {
        abstract fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint): IntPoint
        abstract fun getOffset(): IntPoint
    }

    data object Fill : Position() {
        override fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint) = maxSize
        override fun getOffset() = IntPoint.Companion.ZERO
    }

    data class Offset(@JvmField val offset: IntPoint) : Position() {
        override fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint) = childMinSize
        override fun getOffset() = offset
    }

    data class Bounds(val bounds: IntRectangle) : Position() {
        override fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint) = bounds.size
        override fun getOffset() = bounds.offset
    }

    override fun createDefaultPosition() = Fill
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = childrenWithMinSize.map { it.withSize(it.position.getMaxSize(it.minSize, maxSize)) }
                return object : ViewWithSize {
                    override val size = this@AbsoluteView.size
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        return childrenWithSize.map {
                            it.assemble(offset + it.position.getOffset(), viewPlacer)
                        }.flatten()
                    }
                }
            }
        }
    }
}
