package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View

abstract class AbstractView : View {

    protected lateinit var rendererProxy: RendererProxy
    lateinit var calculatedMinSize: IntPoint
        private set

    final override fun calculateMinSize(rendererProxy: RendererProxy): IntPoint {
        this.rendererProxy = rendererProxy
        calculatedMinSize = calculateMinSizeImpl()
        return calculatedMinSize
    }

    protected abstract fun calculateMinSizeImpl(): IntPoint


    lateinit var calculatedSize: IntPoint
        private set

    final override fun calculateSize(regionSize: IntPoint): IntPoint {
        calculatedSize = calculateSizeImpl(regionSize)
        return calculatedSize
    }

    protected abstract fun calculateSizeImpl(regionSize: IntPoint): IntPoint

}
