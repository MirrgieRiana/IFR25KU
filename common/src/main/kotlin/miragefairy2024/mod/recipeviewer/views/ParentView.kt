package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class ParentView<P> : AbstractView() {

    protected inner class ChildWithMinSize(val child: Child<P, *>, val minSize: IntPoint)
    protected inner class ChildWithSize(val child: Child<P, *>, val size: IntPoint)

    protected fun Child<P, *>.withMinSize(rendererProxy: RendererProxy) = ChildWithMinSize(this, this.view.calculateMinSize(rendererProxy))
    protected fun ParentView<P>.ChildWithMinSize.withSize(regionSize: IntPoint) = ChildWithSize(this.child, this.child.view.calculateSize(regionSize))
    protected fun ParentView<P>.ChildWithSize.attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = this.child.view.attachTo(offset, viewPlacer)

    abstract fun createDefaultPosition(): P

}

class Child<P, V : View>(var position: P, val view: V)

context(Child<*, out ParentView<P>>)
fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }
