package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.Remover
import miragefairy2024.util.flatten

class StackView : ContainerView<Unit>() {

    override fun createDefaultPosition() = Unit


    private lateinit var childrenWithMinSize: List<ParentView<Unit>.ChildWithMinSize>

    override fun calculateMinSizeImpl(): IntPoint {
        childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return IntPoint(
            childrenWithMinSize.maxOfOrNull { it.minSize.x } ?: 0,
            childrenWithMinSize.maxOfOrNull { it.minSize.y } ?: 0,
        )
    }


    private lateinit var childrenWithSize: List<ParentView<Unit>.ChildWithSize>

    override fun calculateSizeImpl(regionSize: IntPoint): IntPoint {
        childrenWithSize = childrenWithMinSize.map { it.withSize(regionSize) }
        return IntPoint(
            childrenWithSize.maxOfOrNull { it.size.x } ?: 0,
            childrenWithSize.maxOfOrNull { it.size.y } ?: 0,
        )
    }


    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return childrenWithSize.map {
            it.attachTo(offset, viewPlacer)
        }.flatten()
    }

}
