package miragefairy2024.mod.recipeviewer.view

/**
 * ステートフルなUI要素です。
 */
interface View {

    /**
     * このViewの最小サイズを計算します。
     * このメソッドは必ず1度だけ呼び出されます。
     */
    fun calculateMinSize(rendererProxy: RendererProxy): IntPoint

    /**
     * このViewの実際のサイズを計算します。
     * 実際のサイズは必ずしも[regionSize]と一致する必要はなく、それより大きくても小さくてもかまいません。
     * このメソッドは必ず[calculateMinSize]の後に1度だけ呼び出されます。
     */
    fun calculateSize(regionSize: IntPoint): IntPoint

    /**
     * このViewを指定された位置に配置します。
     * このメソッドは必ず[calculateSize]の後に呼び出されます。
     * [Remover.remove]が呼び出されるまでに、同じViewに対してこのメソッドが複数回呼び出されることはありません。
     * [Remover.remove]は[attachTo]の呼び出しに対して、必ずしも対応して呼び出される保証はありません。
     */
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover

}
