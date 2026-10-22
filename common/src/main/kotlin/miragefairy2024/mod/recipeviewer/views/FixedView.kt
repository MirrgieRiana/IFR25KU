package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing

abstract class FixedView(private val size: IntPoint) : AbstractView() {
    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP
    override fun calculateContentSizeImpl() = size
}
