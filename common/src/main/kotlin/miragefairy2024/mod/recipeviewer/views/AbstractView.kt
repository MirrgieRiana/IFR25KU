package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    private lateinit var contentSizeCache: IntPoint
    final override val contentSize get() = contentSizeCache

    final override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        calculateChildrenContentSize()
        contentSizeCache = calculateContentSize()
    }

    protected open fun calculateChildrenContentSize() = Unit

    protected abstract fun calculateContentSize(): IntPoint


    private var actualSizeXCache: Int? = null
    final override val actualSizeX get() = actualSizeXCache ?: throw IllegalStateException("The actual size of the X axis is not calculated yet: ${javaClass.name}")

    final override fun calculateActualSizeX(regionSizeX: Int) {
        actualSizeXCache = when (sizingX) {
            Sizing.FILL -> regionSizeX
            Sizing.WRAP -> contentSize.x
        }
        if (actualSizeYCache != null) calculateChildrenActualSize()
    }


    private var actualSizeYCache: Int? = null
    final override val actualSizeY get() = actualSizeYCache ?: throw IllegalStateException("The actual size of the Y axis is not calculated yet: ${javaClass.name}")

    final override fun calculateActualSizeY(regionSizeY: Int) {
        actualSizeYCache = when (sizingY) {
            Sizing.FILL -> regionSizeY
            Sizing.WRAP -> contentSize.y
        }
        if (actualSizeXCache != null) calculateChildrenActualSize()
    }


    protected open fun calculateChildrenActualSize() = Unit

}
