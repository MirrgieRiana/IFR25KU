package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover
import miragefairy2024.util.flatten

abstract class ParentView<P> : AbstractView() {

    protected fun Iterable<Child<P, *>>.setRenderingProxy() {
        this.forEach {
            it.view.setRenderingProxy(renderingProxy)
        }
    }

    protected fun Iterable<Child<P, *>>.calculateActualSizeX(regionSizeFunction: (Child<P, *>) -> Int) {
        this.forEach {
            it.view.calculateContentSizeY { regionSizeFunction(it) }
            it.view.calculateActualSizeX { regionSizeFunction(it) }
        }
    }

    protected fun Iterable<Child<P, *>>.calculateActualSizeY(regionSizeFunction: (Child<P, *>) -> Int) {
        this.forEach {
            it.view.calculateContentSizeX { regionSizeFunction(it) }
            it.view.calculateActualSizeY { regionSizeFunction(it) }
        }
    }

    protected fun Iterable<Child<P, *>>.finishSizeCalculation() {
        this.forEach {
            it.view.finishSizeCalculation()
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
inline fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit = {}) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }
