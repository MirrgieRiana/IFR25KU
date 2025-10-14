package miragefairy2024.mod.recipeviewer.view

interface View {
    fun layout(renderingProxy: RenderingProxy)
    val contentSize: IntPoint
    val actualSize: IntPoint
    fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>)
}
