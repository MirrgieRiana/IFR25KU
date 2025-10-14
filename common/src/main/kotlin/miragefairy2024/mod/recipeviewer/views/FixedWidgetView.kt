package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class FixedWidgetView(private val width: Int, private val height: Int) : View, PlaceableView {
    override fun layout(renderingProxy: RenderingProxy) = Unit
    override val minSize get() = IntPoint(width, height)
    override val size get() = IntPoint(width, height)
    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, IntRectangle(x, y, size.x, size.y))
}
