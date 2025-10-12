package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.Remover

/**
 * ステートフルなUI要素です。
 */
interface View {

    val sizingX: Sizing
    val sizingY: Sizing

    /**
     * このViewのコンテンツの最小サイズ。
     * 親Viewはレイアウトの決定の際にこのサイズを使用します。
     */
    val contentSize: IntPoint

    /**
     * [contentSize]を計算します。
     * [contentSize]はこのViewが実際に機能的に占有するサイズより大きくても小さくてもかまいません。
     * このメソッドは必ず1度だけ呼び出されます。
     */
    fun calculateContentSize(renderingProxy: RenderingProxy)


    /**
     * このViewの実際のサイズ。
     */
    val actualSize: IntPoint

    /**
     * [actualSize]を計算します。
     * [actualSize]は[regionSize]より大きくてもかまいません。
     * このメソッドは必ず[calculateContentSize]の後に1度だけ呼び出されます。
     */
    fun calculateActualSize(regionSize: IntPoint)


    /**
     * このViewを指定された位置に配置します。
     * このメソッドは必ず[calculateActualSize]の後に呼び出されます。
     * [Remover.remove]が呼び出されるまでに、同じViewに対してこのメソッドが複数回呼び出されることはありません。
     * [Remover.remove]は、[attachTo]の呼び出しに対して必ずしも対応して呼び出される保証はありません。
     */
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover

}
