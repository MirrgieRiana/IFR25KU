package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    private lateinit var contentSizeCache: IntPoint
    final override val contentSize get() = contentSizeCache

    final override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        calculateChildrenContentSize()
        contentSizeCache = calculateContentSizeImpl()
    }

    protected open fun calculateChildrenContentSize() = Unit

    protected abstract fun calculateContentSizeImpl(): IntPoint


    private lateinit var actualSizeCache: IntPoint
    final override val actualSize get() = actualSizeCache

    final override fun calculateActualSize(regionSize: IntPoint) {
        calculateChildrenActualSize(regionSize)
        actualSizeCache = calculateActualSizeImpl(regionSize)
    }

    protected open fun calculateChildrenActualSize(regionSize: IntPoint) = Unit

    protected abstract fun calculateActualSizeImpl(regionSize: IntPoint): IntPoint

}
