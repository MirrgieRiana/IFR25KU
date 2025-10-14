package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy

class AbsoluteView(private val size: IntPoint) : ContainerView<IntPoint>() {
    override fun createDefaultPosition() = IntPoint(0, 0)
    override fun calculateContentSize() = size
    override fun calculateActualSize() = size
    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        children.forEach {
            it.xCache = it.position.x
            it.yCache = it.position.y
        }
    }
}
