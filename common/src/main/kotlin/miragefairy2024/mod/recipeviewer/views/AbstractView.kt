package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    protected lateinit var regionSizeXProvider: () -> Int
        private set

    protected lateinit var regionSizeYProvider: () -> Int
        private set

    override fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int) {
        this.renderingProxy = renderingProxy
        this.regionSizeXProvider = regionSizeXProvider
        this.regionSizeYProvider = regionSizeYProvider
    }

    private var contentSizeXCache: Int? = null
    final override fun getContentSizeX() = contentSizeXCache ?: throw IllegalStateException("The content size of the X axis is not calculated yet: ${javaClass.name}")

    final override fun evaluateContentSizeX() {
        calculateChildrenContentSizeX()
        contentSizeXCache = getOwnContentSizeX()
    }

    protected open fun calculateChildrenContentSizeX() = Unit

    protected abstract fun getOwnContentSizeX(): Int


    private var contentSizeYCache: Int? = null
    final override fun getContentSizeY() = contentSizeYCache ?: throw IllegalStateException("The content size of the Y axis is not calculated yet: ${javaClass.name}")

    final override fun evaluateContentSizeY() {
        calculateChildrenContentSizeY()
        contentSizeYCache = getOwnContentSizeY()
    }

    protected open fun calculateChildrenContentSizeY() = Unit

    protected abstract fun getOwnContentSizeY(): Int


    private var actualSizeXCache: Int? = null
    final override fun getActualSizeX() = actualSizeXCache ?: throw IllegalStateException("The actual size of the X axis is not calculated yet: ${javaClass.name}")

    final override fun evaluateActualSizeX() {
        actualSizeXCache = when (sizingX) {
            Sizing.FILL -> regionSizeXProvider()
            Sizing.WRAP -> getContentSizeX()
        }
        calculateChildrenActualSizeX()
    }

    protected open fun calculateChildrenActualSizeX() = Unit


    private var actualSizeYCache: Int? = null
    final override fun getActualSizeY() = actualSizeYCache ?: throw IllegalStateException("The actual size of the Y axis is not calculated yet: ${javaClass.name}")

    final override fun evaluateActualSizeY() {
        actualSizeYCache = when (sizingY) {
            Sizing.FILL -> regionSizeYProvider()
            Sizing.WRAP -> getContentSizeY()
        }
        calculateChildrenActualSizeY()
    }

    protected open fun calculateChildrenActualSizeY() = Unit

}
