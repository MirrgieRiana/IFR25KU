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

    protected abstract fun createChildRegionSizeXFunction(): (Int) -> Int
    protected abstract fun createChildRegionSizeYFunction(): (Int) -> Int

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
