package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover
import miragefairy2024.util.flatten

abstract class ParentView<P> : AbstractView() {

    protected fun Iterable<Child<P, *>>.calculateContentSize() {
        this.forEach {
            it.view.calculateContentSize(renderingProxy)
        }
    }

    protected fun Iterable<Child<P, *>>.calculateActualSize(regionSizeFunction: (Child<P, *>) -> IntPoint) {
        this.forEach {
            it.view.calculateActualSize(regionSizeFunction(it))
        }
    }

    protected fun Iterable<Child<P, *>>.attachTo(viewPlacer: ViewPlacer<PlaceableView>, offsetFunction: (Child<P, *>) -> IntPoint): Remover {
        return this.map {
            it.view.attachTo(offsetFunction(it), viewPlacer)
        }.flatten()
    }

    abstract fun createDefaultPosition(): P

}

class Child<P, V : View>(var position: P, val view: V)

context(Child<*, out ParentView<P>>)
inline fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }
