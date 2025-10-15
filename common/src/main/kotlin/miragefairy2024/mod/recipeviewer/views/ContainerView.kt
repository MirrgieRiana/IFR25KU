package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.View

abstract class ContainerView<P> : ParentView<P>() {

    val children = mutableListOf<Child<P, *>>()

    fun add(child: Child<P, *>) {
        children += child
    }

}

operator fun <P> ContainerView<P>.plusAssign(view: View) = this.add(Child(this.createDefaultPosition(), view))
operator fun <P> ContainerView<P>.plusAssign(child: Child<P, *>) = this.add(child)
