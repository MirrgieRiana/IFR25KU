package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer

abstract class SolidView(private val width: Int, private val height: Int) : View, PlaceableView {
    override fun layout(rendererProxy: RendererProxy) = Unit
    override fun getMinWidth() = width
    override fun getMinHeight() = height
    override fun getWidth() = width
    override fun getHeight() = height
    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, IntRectangle(x, y, getWidth(), getHeight()))
}
