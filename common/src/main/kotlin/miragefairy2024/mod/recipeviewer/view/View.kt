package miragefairy2024.mod.recipeviewer.view

interface View {
    fun calculateActualSize(renderingProxy: RenderingProxy)
    val contentSize: IntPoint
    val actualSize: IntPoint
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>)
}
