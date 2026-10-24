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
        calculatingContentSizeX = false
        contentSizeYCache.reset()
        calculatingContentSizeY = false
        actualSizeXCache.reset()
        calculatingActualSizeX = false
        actualSizeYCache.reset()
        calculatingActualSizeY = false
    }

    private val contentSizeXCache = CachingEvaluator {
        calculatingContentSizeX = true
        try {
            evaluateOwnContentSizeX()
        } finally {
            calculatingContentSizeX = false
        }
    }
    private var calculatingContentSizeX = false

    final override fun evaluateContentSizeX(): Int {
        if (calculatingContentSizeX) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateContentSizeX")
        return contentSizeXCache.evaluate()
    }

    protected abstract fun evaluateOwnContentSizeX(): Int


    private val contentSizeYCache = CachingEvaluator {
        calculatingContentSizeY = true
        try {
            evaluateOwnContentSizeY()
        } finally {
            calculatingContentSizeY = false
        }
    }
    private var calculatingContentSizeY = false

    final override fun evaluateContentSizeY(): Int {
        if (calculatingContentSizeY) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateContentSizeY")
        return contentSizeYCache.evaluate()
    }

    protected abstract fun evaluateOwnContentSizeY(): Int


    private val actualSizeXCache = CachingEvaluator {
        calculatingActualSizeX = true
        try {
            when (sizingX) {
                Sizing.FILL -> regionSizeXProvider()
                Sizing.WRAP -> evaluateContentSizeX()
            }
        } finally {
            calculatingActualSizeX = false
        }
    }
    private var calculatingActualSizeX = false

    final override fun evaluateActualSizeX(): Int {
        if (calculatingActualSizeX) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateActualSizeX")
        return actualSizeXCache.evaluate()
    }


    private val actualSizeYCache = CachingEvaluator {
        calculatingActualSizeY = true
        try {
            when (sizingY) {
                Sizing.FILL -> regionSizeYProvider()
                Sizing.WRAP -> evaluateContentSizeY()
            }
        } finally {
            calculatingActualSizeY = false
        }
    }
    private var calculatingActualSizeY = false

    final override fun evaluateActualSizeY(): Int {
        if (calculatingActualSizeY) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateActualSizeY")
        return actualSizeYCache.evaluate()
    }

}
