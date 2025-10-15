package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View

abstract class ParentView<P> : AbstractView() {

    abstract fun createDefaultPosition(): P

}

class Child<P, V : View>(var position: P, val view: V) {
    var offsetCache = IntPoint.ZERO
}

context(Child<*, out ParentView<P>>)
fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }
