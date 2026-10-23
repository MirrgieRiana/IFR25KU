package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover

class StackView : ContainerView<Unit>() {

    override fun createDefaultPosition() = Unit

    override var sizingX = Sizing.WRAP
    override var sizingY = Sizing.WRAP

    override fun getOwnContentSizeX() = children.maxOfOrNull { it.view.contentSizeX } ?: 0
    override fun getOwnContentSizeY() = children.maxOfOrNull { it.view.contentSizeY } ?: 0

    override fun createChildRegionSizeXFunction(): (Int) -> Int = { actualSizeX }
    override fun createChildRegionSizeYFunction(): (Int) -> Int = { actualSizeY }

    override fun calculateChildrenActualSizeX() {
        children.calculateActualSizeX()
    }

    override fun calculateChildrenActualSizeY() {
        children.calculateActualSizeY()
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return children.attachTo(viewPlacer) { offset }
    }

}
