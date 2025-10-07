package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.mod.recipeviewer.views.Child

fun interface ChildrenGenerator<P> {
    fun generateChildren(rendererProxy: RendererProxy, regionSize: IntPoint): List<Child<P, *>>
}
