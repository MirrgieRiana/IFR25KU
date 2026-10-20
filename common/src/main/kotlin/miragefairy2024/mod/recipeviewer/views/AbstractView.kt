package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    final override fun setRenderingProxy(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        setChildrenRenderingProxy()
    }

    protected open fun setChildrenRenderingProxy() = Unit


    private var regionSizeYProvider: (() -> Int)? = null

    /** X成分の決定にY方向の利用可能なサイズが要る場合に、[miragefairy2024.mod.recipeviewer.views.AbstractView.calculateContentSizeX] の中から呼ぶのだ～🌱 */
    protected fun requireRegionSizeY() = (regionSizeYProvider ?: throw IllegalStateException("The available size of the Y axis is available only while calculating the content size of the X axis: ${javaClass.name}"))()

    private var contentSizeXCache: Lazy<Int>? = null
    final override val contentSizeX get() = (contentSizeXCache ?: throw IllegalStateException("The content size of the X axis is not calculated yet: ${javaClass.name}")).value

    final override fun calculateContentSizeX(regionSizeYProvider: () -> Int) {
        this.regionSizeYProvider = regionSizeYProvider
        contentSizeXCache = cachedSize("content size of the X axis") { calculateContentSizeX() }
    }

    protected abstract fun calculateContentSizeX(): Int


    private var regionSizeXProvider: (() -> Int)? = null

    /** Y成分の決定にX方向の利用可能なサイズが要る場合に、[miragefairy2024.mod.recipeviewer.views.AbstractView.calculateContentSizeY] の中から呼ぶのだ～🌱 */
    protected fun requireRegionSizeX() = (regionSizeXProvider ?: throw IllegalStateException("The available size of the X axis is available only while calculating the content size of the Y axis: ${javaClass.name}"))()

    private var contentSizeYCache: Lazy<Int>? = null
    final override val contentSizeY get() = (contentSizeYCache ?: throw IllegalStateException("The content size of the Y axis is not calculated yet: ${javaClass.name}")).value

    final override fun calculateContentSizeY(regionSizeXProvider: () -> Int) {
        this.regionSizeXProvider = regionSizeXProvider
        contentSizeYCache = cachedSize("content size of the Y axis") { calculateContentSizeY() }
    }

    protected abstract fun calculateContentSizeY(): Int


    private var actualSizeXCache: Lazy<Int>? = null
    final override val actualSizeX get() = (actualSizeXCache ?: throw IllegalStateException("The actual size of the X axis is not calculated yet: ${javaClass.name}")).value

    final override fun calculateActualSizeX(regionSizeXProvider: () -> Int) {
        actualSizeXCache = cachedSize("actual size of the X axis") {
            when (sizingX) {
                Sizing.FILL -> regionSizeXProvider()
                Sizing.WRAP -> contentSizeX
            }
        }
        calculateChildrenActualSizeX()
    }

    protected open fun calculateChildrenActualSizeX() = Unit


    private var actualSizeYCache: Lazy<Int>? = null
    final override val actualSizeY get() = (actualSizeYCache ?: throw IllegalStateException("The actual size of the Y axis is not calculated yet: ${javaClass.name}")).value

    final override fun calculateActualSizeY(regionSizeYProvider: () -> Int) {
        actualSizeYCache = cachedSize("actual size of the Y axis") {
            when (sizingY) {
                Sizing.FILL -> regionSizeYProvider()
                Sizing.WRAP -> contentSizeY
            }
        }
        calculateChildrenActualSizeY()
    }

    protected open fun calculateChildrenActualSizeY() = Unit


    private lateinit var actualSizeCache: IntPoint
    final override val actualSize get() = actualSizeCache

    final override fun finishSizeCalculation() {
        contentSizeX
        contentSizeY
        actualSizeCache = IntPoint(actualSizeX, actualSizeY)
        finishChildrenSizeCalculation()
    }

    protected open fun finishChildrenSizeCalculation() = Unit

    private fun cachedSize(name: String, calculation: () -> Int): Lazy<Int> {
        var calculating = false
        return lazy {
            if (calculating) throw IllegalStateException("Circular reference while calculating the $name: ${javaClass.name}")
            calculating = true
            try {
                calculation()
            } finally {
                calculating = false
            }
        }
    }

}
