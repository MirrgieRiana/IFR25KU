package miragefairy2024.mod.recipeviewer.view

fun interface ViewGenerator {
    fun generateViews(rendererProxy: RendererProxy, regionSize: IntPoint): List<View>
}
