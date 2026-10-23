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

    final override fun evaluateContentSizeX(): Int {
        val old = contentSizeXCache
        if (old != null) {
            return old
        } else {
            val new = getOwnContentSizeX()
            contentSizeXCache = new
            return new
        }
    }

    protected abstract fun getOwnContentSizeX(): Int


    private var contentSizeYCache: Int? = null

    final override fun evaluateContentSizeY(): Int {
        val old = contentSizeYCache
        if (old != null) {
            return old
        } else {
            val new = getOwnContentSizeY()
            contentSizeYCache = new
            return new
        }
    }

    protected abstract fun getOwnContentSizeY(): Int


    private var actualSizeXCache: Int? = null

    final override fun evaluateActualSizeX(): Int {
        val old = actualSizeXCache
        if (old != null) {
            return old
        } else {
            val new = when (sizingX) {
                Sizing.FILL -> regionSizeXProvider()
                Sizing.WRAP -> evaluateContentSizeX()
            }
            actualSizeXCache = new
            return new
        }
    }


    private var actualSizeYCache: Int? = null

    final override fun evaluateActualSizeY(): Int {
        val old = actualSizeYCache
        if (old != null) {
            return old
        } else {
            val new = when (sizingY) {
                Sizing.FILL -> regionSizeYProvider()
                Sizing.WRAP -> evaluateContentSizeY()
            }
            actualSizeYCache = new
            return new
        }
    }

}
