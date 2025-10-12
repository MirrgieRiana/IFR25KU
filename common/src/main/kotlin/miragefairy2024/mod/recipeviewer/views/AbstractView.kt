package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    private lateinit var calculatedContentSize: IntPoint
    final override val contentSize get() = calculatedContentSize

    final override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        calculateChildrenContentSize()
        calculatedContentSize = calculateContentSize()
    }

    protected open fun calculateChildrenContentSize() = Unit

    protected abstract fun calculateContentSize(): IntPoint


    private lateinit var calculatedActualSize: IntPoint
    final override val actualSize get() = calculatedActualSize

    final override fun calculateActualSize(regionSize: IntPoint) {
        calculatedActualSize = IntPoint(
            when (sizingX) {
                Sizing.FILL -> regionSize.x
                Sizing.WRAP -> contentSize.x
            },
            when (sizingY) {
                Sizing.FILL -> regionSize.y
                Sizing.WRAP -> contentSize.y
            },
        )
        calculateChildrenActualSize()
    }

    protected open fun calculateChildrenActualSize() = Unit

}
