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

    override fun calculateContentSize(): IntPoint {
        return IntPoint(
            children.maxOfOrNull { it.view.contentSize.x } ?: 0,
            children.maxOfOrNull { it.view.contentSize.y } ?: 0,
        )
    }

    override fun calculateChildrenActualSize() {
        children.calculateActualSize { actualSize }
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return children.attachTo(viewPlacer) { offset }
    }

}
