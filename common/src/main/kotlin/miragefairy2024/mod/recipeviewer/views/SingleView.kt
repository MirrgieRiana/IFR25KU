package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View

class SingleView : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateMinWidth() = children.single().view.contentSize.x
    override fun calculateMinHeight() = children.single().view.contentSize.y
    override fun calculateWidth() = children.single().view.actualSize.x
    override fun calculateHeight() = children.single().view.actualSize.y
    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        children.single().xCache = 0
        children.single().yCache = 0
    }

    val childView get() = children.single().view
}

fun View(block: Child<Unit, SingleView>.() -> Unit): View = Child(Unit, SingleView()).apply { block() }.view.childView
