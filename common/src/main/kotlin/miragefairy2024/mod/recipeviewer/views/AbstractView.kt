package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import mirrg.kotlin.hydrogen.CachingEvaluator

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
        contentSizeXCache.reset()
        contentSizeYCache.reset()
        actualSizeXCache.reset()
        actualSizeYCache.reset()
    }

    private val contentSizeXCache = CachingEvaluator {
        evaluateContentSizeXImpl()
    }

    final override fun evaluateContentSizeX() = contentSizeXCache.evaluate()

    protected abstract fun evaluateContentSizeXImpl(): Int


    private val contentSizeYCache = CachingEvaluator {
        evaluateContentSizeYImpl()
    }

    final override fun evaluateContentSizeY() = contentSizeYCache.evaluate()

    protected abstract fun evaluateContentSizeYImpl(): Int


    private val actualSizeXCache = CachingEvaluator {
        when (sizingX) {
            Sizing.FILL -> regionSizeXProvider()
            Sizing.WRAP -> evaluateContentSizeX()
        }
    }

    final override fun evaluateActualSizeX() = actualSizeXCache.evaluate()


    private val actualSizeYCache = CachingEvaluator {
        when (sizingY) {
            Sizing.FILL -> regionSizeYProvider()
            Sizing.WRAP -> evaluateContentSizeY()
        }
    }

    final override fun evaluateActualSizeY() = actualSizeYCache.evaluate()

}
