package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View

class SingleView : ContainerView<Unit>(), DefaultedContainerView {
    override fun add(view: View) = add(Unit, view)
    override fun calculateMinWidth() = children.single().view.getMinWidth()
    override fun calculateMinHeight() = children.single().view.getMinHeight()
    override fun calculateWidth() = children.single().view.getWidth()
    override fun calculateHeight() = children.single().view.getHeight()
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        children.single().xCache = 0
        children.single().yCache = 0
    }

    val childView get() = children.single().view
}
