package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy

class AbsoluteView(private val width: Int, private val height: Int) : ContainerView<IntPoint>() {
    override fun createDefaultPosition() = IntPoint(0, 0)
    override fun calculateMinWidth() = width
    override fun calculateMinHeight() = height
    override fun calculateWidth() = width
    override fun calculateHeight() = height
    override fun layout(renderingProxy: RenderingProxy) {
        super.layout(renderingProxy)
        children.forEach {
            it.xCache = it.position.x
            it.yCache = it.position.y
        }
    }
}
