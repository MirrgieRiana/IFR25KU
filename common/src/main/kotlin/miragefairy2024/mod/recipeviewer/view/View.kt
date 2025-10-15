package miragefairy2024.mod.recipeviewer.view

interface View {
    val contentSize: IntPoint
    fun calculateContentSize(renderingProxy: RenderingProxy)
    val actualSize: IntPoint
    fun calculateActualSize(regionSize: IntPoint)
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>)
}
