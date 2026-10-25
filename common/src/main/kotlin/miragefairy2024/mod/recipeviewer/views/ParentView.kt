package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.flatten
import miragefairy2024.mod.recipeviewer.view.plus

abstract class ParentView<P> : AbstractView() {

    protected fun Iterable<Child<P, *>>.initSizeCalculation() {
        this.forEachIndexed { index, child ->
            child.view.initSizeCalculation(renderingProxy, { evaluateChildRegionSizeX(index) }, { evaluateChildRegionSizeY(index) })
        }
    }

    protected abstract fun evaluateChildRegionSizeX(index: Int): Int
    protected abstract fun evaluateChildRegionSizeY(index: Int): Int

    protected fun Iterable<Child<P, *>>.attachTo(viewPlacer: ViewPlacer<PlaceableView>, offset: IntPoint, offsetFunction: (Child<P, *>) -> IntPoint): ViewController {
        return this.map {
            it.view.attachTo(offset + offsetFunction(it), viewPlacer)
        }.flatten()
    }

    protected fun Iterable<Child<P, *>>.attachToIndexed(viewPlacer: ViewPlacer<PlaceableView>, offset: IntPoint, offsetFunction: (Int, Child<P, *>) -> IntPoint): ViewController {
        return this.mapIndexed { index, it ->
            it.view.attachTo(offset + offsetFunction(index, it), viewPlacer)
        }.flatten()
    }

    abstract fun createDefaultPosition(): P

}

class Child<P, V : View>(var position: P, val view: V)

context(Child<*, out ParentView<P>>)
inline fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit = {}) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }
