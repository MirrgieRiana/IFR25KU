package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Remover
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
import miragefairy2024.mod.recipeviewer.view.flatten

class StackView : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                childrenWithMinSize.maxOfOrNull { it.minSize.x } ?: 0,
                childrenWithMinSize.maxOfOrNull { it.minSize.y } ?: 0,
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = childrenWithMinSize.map { it.withSize(maxSize) }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        childrenWithSize.maxOfOrNull { it.size.x } ?: 0,
                        childrenWithSize.maxOfOrNull { it.size.y } ?: 0,
                    )

                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        return childrenWithSize.map {
                            it.assemble(offset, viewPlacer)
                        }.flatten()
                    }
                }
            }
        }
    }
}
