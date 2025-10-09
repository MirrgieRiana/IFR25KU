package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class ContainerView<P> : View {

    val children = mutableListOf<PositionedView<P, *>>()

    class PositionedView<P, V : View>(val position: P, val view: V) {
        var xCache = 0
        var yCache = 0
    }

    fun add(position: P, view: View) {
        children += PositionedView(position, view)
    }

    private var minWidthCache = 0
    private var minHeightCache = 0
    private var widthCache = 0
    private var heightCache = 0

    override fun layout(rendererProxy: RendererProxy) {
        children.forEach {
            it.view.layout(rendererProxy)
        }
        minWidthCache = calculateMinWidth()
        minHeightCache = calculateMinHeight()
        widthCache = calculateWidth()
        heightCache = calculateHeight()
    }

    override fun getMinWidth() = minWidthCache
    override fun getMinHeight() = minHeightCache
    override fun getWidth() = widthCache
    override fun getHeight() = heightCache

    abstract fun calculateMinWidth(): Int
    abstract fun calculateMinHeight(): Int
    abstract fun calculateWidth(): Int
    abstract fun calculateHeight(): Int

    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>) {
        children.forEach {
            it.view.assemble(x + it.xCache, y + it.yCache, viewPlacer)
        }
    }

}

interface DefaultedContainerView {
    fun add(view: View)
}

operator fun <V : View> DefaultedContainerView.plusAssign(view: V) = this.add(view)
operator fun <P> ContainerView<P>.plusAssign(pair: Pair<P, View>) = this.add(pair.first, pair.second)
