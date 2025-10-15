package miragefairy2024.mod.recipeviewer.view

interface View {
    fun calculateContentSize(renderingProxy: RenderingProxy)
    fun calculateActualSize()
    val contentSize: IntPoint
    val actualSize: IntPoint
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>)
}
