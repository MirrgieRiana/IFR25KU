package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var renderingProxy: RenderingProxy
        private set

    private lateinit var contentSizeCache: IntPoint
    final override val contentSize get() = contentSizeCache

    final override fun calculateContentSize(renderingProxy: RenderingProxy) {
        this.renderingProxy = renderingProxy
        calculateChildrenContentSize()
        contentSizeCache = calculateContentSize()
    }

    protected open fun calculateChildrenContentSize() = Unit

    protected abstract fun calculateContentSize(): IntPoint


    private lateinit var actualSizeCache: IntPoint
    final override val actualSize get() = actualSizeCache

    final override fun calculateActualSize(regionSize: IntPoint) {
        actualSizeCache = IntPoint(
            when (sizingX) {
                Sizing.FILL -> regionSize.x
                Sizing.WRAP -> contentSize.x
            },
            when (sizingY) {
                Sizing.FILL -> regionSize.y
                Sizing.WRAP -> contentSize.y
            },
        )
        calculateChildrenActualSize()
        if (sizingY == Sizing.WRAP) actualSizeCache = IntPoint(actualSizeCache.x, calculateActualSizeY())
    }

    protected open fun calculateChildrenActualSize() = Unit

    /**
     * 子の実測が済んだ後の、縦の実測値なのだ～🌱
     * [miragefairy2024.mod.recipeviewer.views.MultiLineTextView]のように、[miragefairy2024.mod.recipeviewer.views.AbstractView.contentSize]より実測の方が高くなる子を包んだときに、その分を親へ伝えるのだ～🌱
     */
    protected open fun calculateActualSizeY() = contentSize.y

}
