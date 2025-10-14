package miragefairy2024.mod.recipeviewer.view

interface View {
    fun calculateActualSize(renderingProxy: RenderingProxy)
    val contentSize: IntPoint
    val actualSize: IntPoint
    fun attachTo(x: Int, y: Int, viewPlacer: ViewPlacer<PlaceableView>)
}
