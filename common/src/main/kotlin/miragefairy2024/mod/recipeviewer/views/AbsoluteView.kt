package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.plus
import miragefairy2024.util.Remover

class AbsoluteView(private val size: IntPoint) : ContainerView<AbsoluteView.Position>() {

    sealed class Position {
        abstract fun getSizeX(contentSizeX: Int, regionSizeX: Int): Int
        abstract fun getSizeY(contentSizeY: Int, regionSizeY: Int): Int
        abstract fun getOffset(): IntPoint
    }

    data object Fill : Position() {
        override fun getSizeX(contentSizeX: Int, regionSizeX: Int) = regionSizeX
        override fun getSizeY(contentSizeY: Int, regionSizeY: Int) = regionSizeY
        override fun getOffset() = IntPoint.Companion.ZERO
    }

    data class Offset(@JvmField val offset: IntPoint) : Position() {
        override fun getSizeX(contentSizeX: Int, regionSizeX: Int) = contentSizeX
        override fun getSizeY(contentSizeY: Int, regionSizeY: Int) = contentSizeY
        override fun getOffset() = offset
    }

    data class Bounds(val bounds: IntRectangle) : Position() {
        override fun getSizeX(contentSizeX: Int, regionSizeX: Int) = bounds.sizeX
        override fun getSizeY(contentSizeY: Int, regionSizeY: Int) = bounds.sizeY
        override fun getOffset() = bounds.offset
    }


    override fun createDefaultPosition() = Fill

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.WRAP

    override fun getOwnContentSizeX() = size.x
    override fun getOwnContentSizeY() = size.y

    override fun createChildRegionSizeXFunction(): (Int) -> Int = { children[it].position.getSizeX(children[it].view.getContentSizeX(), getActualSizeX()) }
    override fun createChildRegionSizeYFunction(): (Int) -> Int = { children[it].position.getSizeY(children[it].view.getContentSizeY(), getActualSizeY()) }

    override fun calculateChildrenActualSizeX() {
        children.calculateActualSizeX()
    }

    override fun calculateChildrenActualSizeY() {
        children.calculateActualSizeY()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return children.attachTo(viewPlacer) { offset + it.position.getOffset() }
    }

}
