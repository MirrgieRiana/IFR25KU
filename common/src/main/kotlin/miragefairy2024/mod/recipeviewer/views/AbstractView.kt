package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    final override fun initSizeCalculation(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        initChildrenSizeCalculation()
    }

    protected open fun initChildrenSizeCalculation() = Unit


    private var contentSizeXCache: Int? = null
    final override val contentSizeX get() = contentSizeXCache ?: throw IllegalStateException("The content size of the X axis is not calculated yet: ${javaClass.name}")

    final override fun calculateContentSizeX() {
        calculateChildrenContentSizeX()
        contentSizeXCache = calculateContentSizeXImpl()
    }

    protected open fun calculateChildrenContentSizeX() = Unit

    protected abstract fun calculateContentSizeXImpl(): Int


    private var contentSizeYCache: Int? = null
    final override val contentSizeY get() = contentSizeYCache ?: throw IllegalStateException("The content size of the Y axis is not calculated yet: ${javaClass.name}")

    final override fun calculateContentSizeY() {
        calculateChildrenContentSizeY()
        contentSizeYCache = calculateContentSizeYImpl()
    }

    protected open fun calculateChildrenContentSizeY() = Unit

    protected abstract fun calculateContentSizeYImpl(): Int


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
