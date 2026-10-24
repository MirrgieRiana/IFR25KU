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
        contentSizeXCache = null
        calculatingContentSizeX = false
        contentSizeYCache = null
        calculatingContentSizeY = false
        actualSizeXCache = null
        calculatingActualSizeX = false
        actualSizeYCache = null
        calculatingActualSizeY = false
    }

    private var contentSizeXCache: Int? = null
    private var calculatingContentSizeX = false

    final override fun evaluateContentSizeX(): Int {
        if (calculatingContentSizeX) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateContentSizeX")
        val old = contentSizeXCache
        if (old != null) {
            return old
        } else {
            calculatingContentSizeX = true
            try {
                val new = evaluateOwnContentSizeX()
                contentSizeXCache = new
                return new
            } finally {
                calculatingContentSizeX = false
            }
        }
    }

    protected abstract fun evaluateOwnContentSizeX(): Int


    private var contentSizeYCache: Int? = null
    private var calculatingContentSizeY = false

    final override fun evaluateContentSizeY(): Int {
        if (calculatingContentSizeY) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateContentSizeY")
        val old = contentSizeYCache
        if (old != null) {
            return old
        } else {
            calculatingContentSizeY = true
            try {
                val new = evaluateOwnContentSizeY()
                contentSizeYCache = new
                return new
            } finally {
                calculatingContentSizeY = false
            }
        }
    }

    protected abstract fun evaluateOwnContentSizeY(): Int


    private var actualSizeXCache: Int? = null
    private var calculatingActualSizeX = false

    final override fun evaluateActualSizeX(): Int {
        if (calculatingActualSizeX) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateActualSizeX")
        val old = actualSizeXCache
        if (old != null) {
            return old
        } else {
            calculatingActualSizeX = true
            try {
                val new = when (sizingX) {
                    Sizing.FILL -> regionSizeXProvider()
                    Sizing.WRAP -> evaluateContentSizeX()
                }
                actualSizeXCache = new
                return new
            } finally {
                calculatingActualSizeX = false
            }
        }
    }


    private var actualSizeYCache: Int? = null
    private var calculatingActualSizeY = false

    final override fun evaluateActualSizeY(): Int {
        if (calculatingActualSizeY) throw IllegalStateException("Cyclic size dependency: ${javaClass.name}.evaluateActualSizeY")
        val old = actualSizeYCache
        if (old != null) {
            return old
        } else {
            calculatingActualSizeY = true
            try {
                val new = when (sizingY) {
                    Sizing.FILL -> regionSizeYProvider()
                    Sizing.WRAP -> evaluateContentSizeY()
                }
                actualSizeYCache = new
                return new
            } finally {
                calculatingActualSizeY = false
            }
        }
    }

}
