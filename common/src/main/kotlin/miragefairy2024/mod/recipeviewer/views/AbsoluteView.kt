package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
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

    override fun calculateContentSize() = size
    override fun calculateActualSize() = size

    override fun calculateChildrenActualSize() {
        children.forEach {
            it.view.calculateActualSize(renderingProxy)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        children.forEach {
            it.view.attachTo(offset + it.position.getOffset(), viewPlacer)
        }
    }

}
