package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy

abstract class FixedView(size: IntPoint) : AbstractView() {
    override fun calculateActualSize(renderingProxy: RenderingProxy) = Unit
    override val contentSize = size
    override val actualSize = size
}
