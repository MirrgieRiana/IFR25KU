package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import mirrg.kotlin.helium.atLeast

abstract class ListView : ContainerView<Alignment>() {
    override fun createDefaultPosition() = Alignment.START
}

class XListView : ListView() {
    @JvmField
    var minHeight = 0
    override fun calculateMinWidth() = children.sumOf { it.view.minSize.x }
    override fun calculateMinHeight() = (children.maxOfOrNull { it.view.minSize.y } ?: 0) atLeast minHeight
    override fun calculateWidth() = children.sumOf { it.view.size.x }
    override fun calculateHeight() = (children.maxOfOrNull { it.view.size.y } ?: 0) atLeast minHeight
    override fun layout(renderingProxy: RenderingProxy) {
        super.layout(renderingProxy)
        var x = 0
        children.forEach {
            it.xCache = x
            it.yCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (size.y - it.view.size.y) / 2
                Alignment.END -> size.y - it.view.size.y
            }
            x += it.view.size.x
        }
    }
}

class YListView : ListView() {
    @JvmField
    var minWidth = 0
    override fun calculateMinWidth() = (children.maxOfOrNull { it.view.minSize.x } ?: 0) atLeast minWidth
    override fun calculateMinHeight() = children.sumOf { it.view.minSize.y }
    override fun calculateWidth() = (children.maxOfOrNull { it.view.size.x } ?: 0) atLeast minWidth
    override fun calculateHeight() = children.sumOf { it.view.size.y }
    override fun layout(renderingProxy: RenderingProxy) {
        super.layout(renderingProxy)
        var y = 0
        children.forEach {
            it.xCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (size.x - it.view.size.x) / 2
                Alignment.END -> size.x - it.view.size.x
            }
            it.yCache = y
            y += it.view.size.y
        }
    }
}
