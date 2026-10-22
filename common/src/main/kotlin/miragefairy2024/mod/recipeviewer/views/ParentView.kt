package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover
import miragefairy2024.util.flatten

abstract class ParentView<P> : AbstractView() {

    protected fun Iterable<Child<P, *>>.initSizeCalculation() {
        val regionSizeXFunction by lazy { createChildRegionSizeXFunction() }
        val regionSizeYFunction by lazy { createChildRegionSizeYFunction() }
        this.forEachIndexed { index, child ->
            child.view.initSizeCalculation(renderingProxy, { regionSizeXFunction(index) }, { regionSizeYFunction(index) })
        }
    }

    protected open fun createChildRegionSizeXFunction(): (Int) -> Int = { actualSizeX }
    protected open fun createChildRegionSizeYFunction(): (Int) -> Int = { actualSizeY }

    protected fun Iterable<Child<P, *>>.calculateContentSizeX() {
        this.forEach {
            it.view.calculateContentSizeX()
        }
    }

    protected fun Iterable<Child<P, *>>.calculateContentSizeY() {
        this.forEach {
            it.view.calculateContentSizeY()
        }
    }

    protected fun Iterable<Child<P, *>>.calculateActualSizeX() {
        this.forEach {
            it.view.calculateActualSizeX()
        }
    }

    protected fun Iterable<Child<P, *>>.calculateActualSizeY() {
        this.forEach {
            it.view.calculateActualSizeY()
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
