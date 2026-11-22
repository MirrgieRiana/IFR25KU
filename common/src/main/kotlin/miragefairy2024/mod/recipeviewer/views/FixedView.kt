package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing
import mirrg.kotlin.helium.atLeast

abstract class FixedView(private val size: IntPoint) : AbstractView() {
    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP
    override fun evaluateContentSizeXImpl() = size.x atLeast 0
    override fun evaluateContentSizeYImpl() = size.y atLeast 0
}
