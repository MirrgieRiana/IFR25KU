package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy

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
    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        children.forEach {
            it.xCache = it.position.getOffset().x
            it.yCache = it.position.getOffset().y
        }
    }
}
