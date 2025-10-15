package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View

class WrapperView : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateContentSize() = children.single().view.contentSize
    override fun calculateActualSize() = children.single().view.actualSize
    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        children.single().offsetCache = IntPoint.ZERO
    }

    val childView get() = children.single().view
}

fun View(block: Child<Unit, WrapperView>.() -> Unit): View = Child(Unit, WrapperView()).apply { block() }.view.childView
