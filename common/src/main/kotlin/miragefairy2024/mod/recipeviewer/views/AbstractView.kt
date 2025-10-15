package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy

    private var contentSizeCache = IntPoint.ZERO
    private var actualSizeCache = IntPoint.ZERO

    override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        calculateChildrenContentSize()
        contentSizeCache = calculateContentSizeImpl()
    }

    override fun calculateActualSize() {
        calculateChildrenActualSize()
        actualSizeCache = calculateActualSizeImpl()
    }

    override val contentSize get() = contentSizeCache
    override val actualSize get() = actualSizeCache

    protected open fun calculateChildrenContentSize() = Unit
    protected open fun calculateChildrenActualSize() = Unit
    protected abstract fun calculateContentSizeImpl(): IntPoint
    protected abstract fun calculateActualSizeImpl(): IntPoint

}
