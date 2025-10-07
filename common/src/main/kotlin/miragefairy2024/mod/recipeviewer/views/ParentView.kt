package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize

abstract class ParentView<P> : View {

    protected inner class ChildWithMinSize(val position: P, val viewWithMinSize: ViewWithMinSize)
    protected inner class ChildWithSize(val position: P, val viewWithSize: ViewWithSize)

    @JvmName("withMinSizeOfChild")
    protected fun Child<P, *>.withMinSize(rendererProxy: RendererProxy) = ChildWithMinSize(this.position, this.view.calculateMinSize(rendererProxy))

    @get:JvmName("getMinSizeOfChildWithMinSize")
    protected val ParentView<P>.ChildWithMinSize.minSize get() = this.viewWithMinSize.minSize

    @JvmName("withSizeOfChildWithMinSize")
    protected fun ParentView<P>.ChildWithMinSize.withSize(maxSize: IntPoint) = ChildWithSize(this.position, this.viewWithMinSize.calculateSize(maxSize))

    @get:JvmName("getSizeOfChildWithSize")
    protected val ParentView<P>.ChildWithSize.size get() = this.viewWithSize.size

    @JvmName("assembleOfChildWithSize")
    protected fun ParentView<P>.ChildWithSize.assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = this.viewWithSize.assemble(offset, viewPlacer)

    abstract fun createDefaultPosition(): P

}

class Child<P, V : View>(var position: P, val view: V)

context(Child<*, out ParentView<P>>)
fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }
