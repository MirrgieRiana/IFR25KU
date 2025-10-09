package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.SingleView

interface View {
    fun layout(rendererProxy: RendererProxy)
    fun getMinWidth(): Int
    fun getMinHeight(): Int
    fun getWidth(): Int
    fun getHeight(): Int
    fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>)
}

fun View(block: SingleView.() -> Unit): View = SingleView { block(this) }.childView
