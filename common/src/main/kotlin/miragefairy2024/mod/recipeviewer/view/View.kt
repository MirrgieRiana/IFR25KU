package miragefairy2024.mod.recipeviewer.view

interface View {
    fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize
}

interface ViewWithMinSize {
    val minSize: IntPoint
    fun calculateSize(maxSize: IntPoint): ViewWithSize
}

interface ViewWithSize {
    val size: IntPoint

    /**
     * このメソッドは1個の[miragefairy2024.mod.recipeviewer.views.View]に対して[Remover]による除去を挟まずに連続で複数回呼び出されることはありません。
     */
    fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover
}

fun interface Remover {
    companion object {
        val NONE = Remover { }
    }

    /**
     * [miragefairy2024.mod.recipeviewer.views.View]に対してイベント登録を行う場合、その解除も行う必要があります。
     */
    fun remove()
}

fun Iterable<Remover>.flatten() = Remover { this.forEach { it.remove() } }
