package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy


    private var contentSizeCache = IntPoint.ZERO
    override val contentSize get() = contentSizeCache

    override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        calculateChildrenContentSize()
        contentSizeCache = calculateContentSizeImpl()
    }

    protected open fun calculateChildrenContentSize() = Unit

    protected abstract fun calculateContentSizeImpl(): IntPoint


    private var actualSizeCache = IntPoint.ZERO
    override val actualSize get() = actualSizeCache

    override fun calculateActualSize(regionSize: IntPoint) {
        calculateChildrenActualSize(regionSize)
        actualSizeCache = calculateActualSizeImpl(regionSize)
    }

    protected open fun calculateChildrenActualSize(regionSize: IntPoint) = Unit

    protected abstract fun calculateActualSizeImpl(regionSize: IntPoint): IntPoint

}
