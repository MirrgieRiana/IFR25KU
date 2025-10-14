package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class ContainerView<P> : View {

    val children = mutableListOf<Child<P, *>>()

    fun add(child: Child<P, *>) {
        children += child
    }

    private var minWidthCache = 0
    private var minHeightCache = 0
    private var widthCache = 0
    private var heightCache = 0

    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        children.forEach {
            it.view.calculateActualSize(renderingProxy)
        }
        minWidthCache = calculateMinWidth()
        minHeightCache = calculateMinHeight()
        widthCache = calculateWidth()
        heightCache = calculateHeight()
    }

    override val contentSize get() = IntPoint(minWidthCache, minHeightCache)
    override val actualSize get() = IntPoint(widthCache, heightCache)

    abstract fun calculateMinWidth(): Int
    abstract fun calculateMinHeight(): Int
    abstract fun calculateWidth(): Int
    abstract fun calculateHeight(): Int

    override fun attachTo(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>) {
        children.forEach {
            it.view.attachTo(x + it.xCache, y + it.yCache, viewPlacer)
        }
    }

    abstract fun createDefaultPosition(): P

}

class Child<P, V : View>(var position: P, val view: V) {
    var xCache = 0
    var yCache = 0
}

context(Child<*, out ContainerView<P>>)
fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }

operator fun <P> ContainerView<P>.plusAssign(view: View) = this.add(Child(this.createDefaultPosition(), view))
operator fun <P> ContainerView<P>.plusAssign(child: Child<P, *>) = this.add(child)
