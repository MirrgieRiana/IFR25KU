package miragefairy2024.mod.recipeviewer.view

interface View {
    fun layout(renderingProxy: RenderingProxy)
    fun getMinWidth(): Int
    fun getMinHeight(): Int
    fun getWidth(): Int
    fun getHeight(): Int
    fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>)
}
