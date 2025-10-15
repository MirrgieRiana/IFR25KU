package miragefairy2024.mod.recipeviewer.view

interface View {
    fun calculateContentSize(renderingProxy: RenderingProxy)
    fun calculateActualSize(regionSize: IntPoint)
    val contentSize: IntPoint
    val actualSize: IntPoint
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>)
}
