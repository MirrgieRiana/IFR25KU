package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View

abstract class ContainerView<P> : ParentView<P>() {

    protected val children = mutableListOf<Child<P, *>>()

    fun add(child: Child<P, *>) {
        children += child
    }

    final override fun calculateChildrenContentSize() {
        children.forEach {
            it.view.calculateContentSize(renderingProxy)
        }
    }

    final override fun calculateChildrenActualSize(regionSize: IntPoint) {
        children.forEach {
            it.view.calculateActualSize(regionSize)
        }
    }

}

operator fun <P> ContainerView<P>.plusAssign(view: View) = this.add(Child(this.createDefaultPosition(), view))
operator fun <P> ContainerView<P>.plusAssign(child: Child<P, *>) = this.add(child)
