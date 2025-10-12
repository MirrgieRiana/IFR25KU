package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.mod.recipeviewer.view.size
import miragefairy2024.util.Remover

class AbsoluteView(private val size: IntPoint) : ContainerView<AbsoluteView.Position>() {

    sealed class Position {
        abstract fun getSize(contentSize: IntPoint, regionSize: IntPoint): IntPoint
        abstract fun getOffset(): IntPoint
    }

    data object Fill : Position() {
        override fun getSize(contentSize: IntPoint, regionSize: IntPoint) = regionSize
        override fun getOffset() = IntPoint.Companion.ZERO
    }

    data class Offset(@JvmField val offset: IntPoint) : Position() {
        override fun getSize(contentSize: IntPoint, regionSize: IntPoint) = contentSize
        override fun getOffset() = offset
    }

    data class Bounds(val bounds: IntRectangle) : Position() {
        override fun getSize(contentSize: IntPoint, regionSize: IntPoint) = bounds.size
        override fun getOffset() = bounds.offset
    }


    override fun createDefaultPosition() = Fill

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.WRAP

    override fun calculateContentSize() = size

    override fun calculateChildrenActualSize() {
        children.calculateActualSize { it.position.getSize(it.view.contentSize, actualSize) }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return children.attachTo(viewPlacer) { offset + it.position.getOffset() }
    }

}
