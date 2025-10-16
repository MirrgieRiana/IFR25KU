package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.plus

class AbsoluteView(private val size: IntPoint) : ContainerView<AbsoluteView.Position>() {

    sealed class Position {
        abstract fun getOffset(): IntPoint
    }

    data class Offset(@JvmField val offset: IntPoint) : Position() {
        override fun getOffset() = offset
    }

    override fun createDefaultPosition() = Offset(IntPoint.ZERO)

    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP

    override fun calculateContentSize() = size

    override fun calculateChildrenActualSize() {
        children.calculateActualSize { actualSize }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        children.attachTo(viewPlacer) { offset + it.position.getOffset() }
    }

}
