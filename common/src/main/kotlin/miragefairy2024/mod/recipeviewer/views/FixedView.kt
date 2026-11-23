package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.Sizing

abstract class FixedView(private val size: IntPoint) : AbstractView() {

    init {
        require(size.x >= 0 && size.y >= 0) { "Fixed view dimensions must be non-negative" }
    }

    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP
    override fun evaluateContentSizeXImpl() = size.x
    override fun evaluateContentSizeYImpl() = size.y
}
