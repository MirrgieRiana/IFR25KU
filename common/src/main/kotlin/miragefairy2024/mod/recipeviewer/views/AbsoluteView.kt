package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View

class AbsoluteView(private val width: Int, private val height: Int) : ContainerView<IntPoint>(), DefaultedContainerView {
    override fun add(view: View) = add(IntPoint(0, 0), view)
    override fun calculateMinWidth() = width
    override fun calculateMinHeight() = height
    override fun calculateWidth() = width
    override fun calculateHeight() = height
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        children.forEach {
            it.xCache = it.position.x
            it.yCache = it.position.y
        }
    }
}
