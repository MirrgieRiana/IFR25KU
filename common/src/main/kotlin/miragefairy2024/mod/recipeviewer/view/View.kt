package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.SingleView

interface View {
    fun layout(renderingProxy: RenderingProxy)
    fun getMinWidth(): Int
    fun getMinHeight(): Int
    fun getWidth(): Int
    fun getHeight(): Int
    fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>)
}

fun View(block: Child<Unit, SingleView>.() -> Unit): View = Child(Unit, SingleView()).apply { block() }.view.childView
