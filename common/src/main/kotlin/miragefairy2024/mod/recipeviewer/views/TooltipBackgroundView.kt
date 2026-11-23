package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized

class TooltipBackgroundView : AbstractView(), PlaceableView {
    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL
    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = 0
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))
}
