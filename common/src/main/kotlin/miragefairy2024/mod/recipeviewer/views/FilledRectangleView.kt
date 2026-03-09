package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue

class FilledRectangleView : AbstractView(), PlaceableView {
    val color = ObservableValue(0)
    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL
    override fun calculateContentSize() = IntPoint.ZERO
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}
