package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset

class AbsoluteView(private val size: IntPoint) : ContainerView<AbsoluteView.Position>() {

    sealed class Position {
        abstract fun evaluateSizeX(contentSizeXProvider: () -> Int, regionSizeXProvider: () -> Int): Int
        abstract fun evaluateSizeY(contentSizeYProvider: () -> Int, regionSizeYProvider: () -> Int): Int
        abstract fun getOffset(): IntPoint
    }

    data object Fill : Position() {
        override fun evaluateSizeX(contentSizeXProvider: () -> Int, regionSizeXProvider: () -> Int) = regionSizeXProvider()
        override fun evaluateSizeY(contentSizeYProvider: () -> Int, regionSizeYProvider: () -> Int) = regionSizeYProvider()
        override fun getOffset() = IntPoint.Companion.ZERO
    }

    data class Offset(@JvmField val offset: IntPoint) : Position() {
        override fun evaluateSizeX(contentSizeXProvider: () -> Int, regionSizeXProvider: () -> Int) = contentSizeXProvider()
        override fun evaluateSizeY(contentSizeYProvider: () -> Int, regionSizeYProvider: () -> Int) = contentSizeYProvider()
        override fun getOffset() = offset
    }

    data class Bounds(val bounds: IntRectangle) : Position() {
        override fun evaluateSizeX(contentSizeXProvider: () -> Int, regionSizeXProvider: () -> Int) = bounds.sizeX
        override fun evaluateSizeY(contentSizeYProvider: () -> Int, regionSizeYProvider: () -> Int) = bounds.sizeY
        override fun getOffset() = bounds.offset
    }


    override fun createDefaultPosition() = Fill

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.WRAP

    override fun evaluateContentSizeXImpl() = size.x
    override fun evaluateContentSizeYImpl() = size.y

    override fun evaluateChildRegionSizeX(index: Int) = children[index].position.evaluateSizeX({ children[index].view.evaluateContentSizeX() }, { evaluateActualSizeX() })
    override fun evaluateChildRegionSizeY(index: Int) = children[index].position.evaluateSizeY({ children[index].view.evaluateContentSizeY() }, { evaluateActualSizeY() })

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return children.attachTo(viewPlacer, offset) { it.position.getOffset() }
    }

}
