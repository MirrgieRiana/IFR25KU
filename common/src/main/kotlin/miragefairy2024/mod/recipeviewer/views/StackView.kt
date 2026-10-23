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

    override fun getOwnContentSizeX() = children.maxOfOrNull { it.view.evaluateContentSizeX() } ?: 0
    override fun getOwnContentSizeY() = children.maxOfOrNull { it.view.evaluateContentSizeY() } ?: 0

    override fun createChildRegionSizeXFunction(): (Int) -> Int = { evaluateActualSizeX() }
    override fun createChildRegionSizeYFunction(): (Int) -> Int = { evaluateActualSizeY() }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return children.attachTo(viewPlacer) { offset }
    }

}
