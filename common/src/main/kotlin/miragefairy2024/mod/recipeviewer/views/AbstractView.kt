package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    override fun initSizeCalculation(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
    }

    private lateinit var contentSizeCache: IntPoint
    final override val contentSize get() = contentSizeCache

    final override fun calculateContentSize() {
        calculateChildrenContentSize()
        contentSizeCache = calculateContentSizeImpl()
    }

    protected open fun calculateChildrenContentSize() = Unit

    protected abstract fun calculateContentSizeImpl(): IntPoint


    private var actualSizeXCache: Int? = null
    final override val actualSizeX get() = actualSizeXCache ?: throw IllegalStateException("The actual size of the X axis is not calculated yet: ${javaClass.name}")

    final override fun calculateActualSizeX(regionSizeX: Int) {
        actualSizeXCache = when (sizingX) {
            Sizing.FILL -> regionSizeX
            Sizing.WRAP -> contentSize.x
        }
        calculateChildrenActualSizeX()
    }

    protected open fun calculateChildrenActualSizeX() = Unit


    private var actualSizeYCache: Int? = null
    final override val actualSizeY get() = actualSizeYCache ?: throw IllegalStateException("The actual size of the Y axis is not calculated yet: ${javaClass.name}")

    final override fun calculateActualSizeY(regionSizeY: Int) {
        actualSizeYCache = when (sizingY) {
            Sizing.FILL -> regionSizeY
            Sizing.WRAP -> contentSize.y
        }
        calculateChildrenActualSizeY()
    }

    protected open fun calculateChildrenActualSizeY() = Unit

}
