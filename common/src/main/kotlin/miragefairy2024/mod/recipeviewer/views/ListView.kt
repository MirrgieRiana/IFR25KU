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
    override fun calculateMinWidth() = children.sumOf { it.view.contentSize.x }
    override fun calculateMinHeight() = (children.maxOfOrNull { it.view.contentSize.y } ?: 0) atLeast minHeight
    override fun calculateWidth() = children.sumOf { it.view.actualSize.x }
    override fun calculateHeight() = (children.maxOfOrNull { it.view.actualSize.y } ?: 0) atLeast minHeight
    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        var x = 0
        children.forEach {
            it.xCache = x
            it.yCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSize.y - it.view.actualSize.y) / 2
                Alignment.END -> actualSize.y - it.view.actualSize.y
            }
            x += it.view.actualSize.x
        }
    }
}

class YListView : ListView() {
    @JvmField
    var minWidth = 0
    override fun calculateMinWidth() = (children.maxOfOrNull { it.view.contentSize.x } ?: 0) atLeast minWidth
    override fun calculateMinHeight() = children.sumOf { it.view.contentSize.y }
    override fun calculateWidth() = (children.maxOfOrNull { it.view.actualSize.x } ?: 0) atLeast minWidth
    override fun calculateHeight() = children.sumOf { it.view.actualSize.y }
    override fun calculateActualSize(renderingProxy: RenderingProxy) {
        super.calculateActualSize(renderingProxy)
        var y = 0
        children.forEach {
            it.xCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (actualSize.x - it.view.actualSize.x) / 2
                Alignment.END -> actualSize.x - it.view.actualSize.x
            }
            it.yCache = y
            y += it.view.actualSize.y
        }
    }
}
