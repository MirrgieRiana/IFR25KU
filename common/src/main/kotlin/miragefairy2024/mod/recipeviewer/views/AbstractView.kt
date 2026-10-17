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
    protected fun requireRegionSizeY() = regionSizeYProvider!!()

    private var contentSizeXCache: Int? = null
    final override val contentSizeX get() = contentSizeXCache!!

    final override fun calculateContentSizeX(regionSizeYProvider: () -> Int) {
        this.regionSizeYProvider = regionSizeYProvider
        calculateChildrenContentSizeX(regionSizeYProvider)
        contentSizeXCache = calculateContentSizeX()
        this.regionSizeYProvider = null
    }

    protected open fun calculateChildrenContentSizeX(regionSizeYProvider: () -> Int) = Unit

    protected abstract fun calculateContentSizeX(): Int


    private var regionSizeXProvider: (() -> Int)? = null

    /** Y成分の決定にX方向の利用可能なサイズが要る場合に、[miragefairy2024.mod.recipeviewer.views.AbstractView.calculateContentSizeY] の中から呼ぶのだ～🌱 */
    protected fun requireRegionSizeX() = regionSizeXProvider!!()

    private var contentSizeYCache: Int? = null
    final override val contentSizeY get() = contentSizeYCache!!

    final override fun calculateContentSizeY(regionSizeXProvider: () -> Int) {
        this.regionSizeXProvider = regionSizeXProvider
        calculateChildrenContentSizeY(regionSizeXProvider)
        contentSizeYCache = calculateContentSizeY()
        this.regionSizeXProvider = null
    }

    protected open fun calculateChildrenContentSizeY(regionSizeXProvider: () -> Int) = Unit

    protected abstract fun calculateContentSizeY(): Int


    private lateinit var actualSizeCache: IntPoint
    final override val actualSize get() = actualSizeCache

    final override fun calculateActualSize(regionSize: IntPoint) {
        actualSizeCache = IntPoint(
            when (sizingX) {
                Sizing.FILL -> regionSize.x
                Sizing.WRAP -> contentSizeX
            },
            when (sizingY) {
                Sizing.FILL -> regionSize.y
                Sizing.WRAP -> contentSizeY
            },
        )
        calculateChildrenActualSize()
    }

    protected open fun calculateChildrenActualSize() = Unit

}
