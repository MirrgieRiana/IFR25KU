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

    private var contentSizeXCache: Int? = null
    final override val contentSizeX get() = contentSizeXCache ?: throw IllegalStateException("The content size of the X axis is not calculated yet: ${javaClass.name}")

    final override fun calculateContentSizeX(regionSizeYProvider: () -> Int) {
        // 逆側の軸の要求が自分自身へ戻ってくると、ここが入れ子で呼ばれるのだ～🌱 その場合にスタックオーバーフローではなくViewのクラス名を伴う例外を出すのだ～🌱
        if (this.regionSizeYProvider != null) throw IllegalStateException("Circular reference while calculating the content size of the X axis: ${javaClass.name}")
        this.regionSizeYProvider = regionSizeYProvider
        calculateChildrenContentSizeX(regionSizeYProvider)
        contentSizeXCache = calculateContentSizeX()
        this.regionSizeYProvider = null
    }

    protected open fun calculateChildrenContentSizeX(regionSizeYProvider: () -> Int) = Unit

    protected abstract fun calculateContentSizeX(): Int


    private var regionSizeXProvider: (() -> Int)? = null

    /** Y成分の決定にX方向の利用可能なサイズが要る場合に、[miragefairy2024.mod.recipeviewer.views.AbstractView.calculateContentSizeY] の中から呼ぶのだ～🌱 */
    protected fun requireRegionSizeX() = (regionSizeXProvider ?: throw IllegalStateException("The available size of the X axis is available only while calculating the content size of the Y axis: ${javaClass.name}"))()

    private var contentSizeYCache: Int? = null
    final override val contentSizeY get() = contentSizeYCache ?: throw IllegalStateException("The content size of the Y axis is not calculated yet: ${javaClass.name}")

    final override fun calculateContentSizeY(regionSizeXProvider: () -> Int) {
        if (this.regionSizeXProvider != null) throw IllegalStateException("Circular reference while calculating the content size of the Y axis: ${javaClass.name}")
        this.regionSizeXProvider = regionSizeXProvider
        calculateChildrenContentSizeY(regionSizeXProvider)
        contentSizeYCache = calculateContentSizeY()
        this.regionSizeXProvider = null
    }

    protected open fun calculateChildrenContentSizeY(regionSizeXProvider: () -> Int) = Unit

    protected abstract fun calculateContentSizeY(): Int


    private var actualSizeXCache: Int? = null
    final override val actualSizeX get() = actualSizeXCache ?: throw IllegalStateException("The actual size of the X axis is not calculated yet: ${javaClass.name}")

    final override fun calculateActualSizeX(regionSizeX: Int) {
        actualSizeXCache = when (sizingX) {
            Sizing.FILL -> regionSizeX
            Sizing.WRAP -> contentSizeX
        }
    }


    private var actualSizeYCache: Int? = null
    final override val actualSizeY get() = actualSizeYCache ?: throw IllegalStateException("The actual size of the Y axis is not calculated yet: ${javaClass.name}")

    final override fun calculateActualSizeY(regionSizeY: Int) {
        actualSizeYCache = when (sizingY) {
            Sizing.FILL -> regionSizeY
            Sizing.WRAP -> contentSizeY
        }
    }


    final override val actualSize get() = IntPoint(actualSizeX, actualSizeY)

    final override fun calculateActualSize(regionSize: IntPoint) {
        calculateActualSizeX(regionSize.x)
        calculateActualSizeY(regionSize.y)
        calculateChildrenActualSize()
    }

    protected open fun calculateChildrenActualSize() = Unit

}
