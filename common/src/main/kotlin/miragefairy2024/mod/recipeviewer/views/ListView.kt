package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import mirrg.kotlin.helium.atLeast

abstract class ListView : ContainerView<Alignment>() {
    override fun createDefaultPosition() = Alignment.START
}

class XListView : ListView() {
    @JvmField
    var minHeight = 0
    override fun calculateMinWidth() = children.sumOf { it.view.getMinWidth() }
    override fun calculateMinHeight() = (children.maxOfOrNull { it.view.getMinHeight() } ?: 0) atLeast minHeight
    override fun calculateWidth() = children.sumOf { it.view.getWidth() }
    override fun calculateHeight() = (children.maxOfOrNull { it.view.getHeight() } ?: 0) atLeast minHeight
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        var x = 0
        children.forEach {
            it.xCache = x
            it.yCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (getHeight() - it.view.getHeight()) / 2
                Alignment.END -> getHeight() - it.view.getHeight()
            }
            x += it.view.getWidth()
        }
    }
}

class YListView : ListView() {
    @JvmField
    var minWidth = 0
    override fun calculateMinWidth() = (children.maxOfOrNull { it.view.getMinWidth() } ?: 0) atLeast minWidth
    override fun calculateMinHeight() = children.sumOf { it.view.getMinHeight() }
    override fun calculateWidth() = (children.maxOfOrNull { it.view.getWidth() } ?: 0) atLeast minWidth
    override fun calculateHeight() = children.sumOf { it.view.getHeight() }
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        var y = 0
        children.forEach {
            it.xCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (getWidth() - it.view.getWidth()) / 2
                Alignment.END -> getWidth() - it.view.getWidth()
            }
            it.yCache = y
            y += it.view.getHeight()
        }
    }
}
