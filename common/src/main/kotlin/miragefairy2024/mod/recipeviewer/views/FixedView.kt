package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint

abstract class FixedView(private val size: IntPoint) : AbstractView() {
    override fun calculateContentSizeImpl() = size
    override fun calculateActualSizeImpl() = size
}
