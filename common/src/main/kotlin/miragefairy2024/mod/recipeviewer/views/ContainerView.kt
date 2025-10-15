package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.plus

abstract class ContainerView<P> : ParentView<P>() {

    val children = mutableListOf<Child<P, *>>()

    fun add(child: Child<P, *>) {
        children += child
    }

    override fun calculateChildrenActualSize() {
        children.forEach {
            it.view.calculateActualSize(renderingProxy)
        }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) {
        children.forEach {
            it.view.attachTo(offset + it.offsetCache, viewPlacer)
        }
    }

}

operator fun <P> ContainerView<P>.plusAssign(view: View) = this.add(Child(this.createDefaultPosition(), view))
operator fun <P> ContainerView<P>.plusAssign(child: Child<P, *>) = this.add(child)
