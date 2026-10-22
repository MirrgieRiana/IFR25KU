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


    /** この View の実際のサイズの X 成分なのだ～🌱 */
    val actualSizeX: Int

    /** この View の実際のサイズの Y 成分なのだ～🌱 */
    val actualSizeY: Int

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.actualSizeX] を計算するのだ～🌱
     * 実際のサイズは [regionSizeX] より大きくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.calculateContentSize] の後に 1 度だけ呼び出すのだ～🌱
     */
    fun calculateActualSizeX(regionSizeX: Int)

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.actualSizeY] を計算するのだ～🌱
     * 実際のサイズは [regionSizeY] より大きくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.calculateContentSize] の後に 1 度だけ呼び出すのだ～🌱
     */
    fun calculateActualSizeY(regionSizeY: Int)


    /**
     * このViewを指定された位置に配置します。
     * このメソッドは必ず [miragefairy2024.mod.recipeviewer.view.View.calculateActualSizeX] と [miragefairy2024.mod.recipeviewer.view.View.calculateActualSizeY] の両方の後に呼び出すのだ～🌱
     * [Remover.remove]が呼び出されるまでに、同じViewに対してこのメソッドが複数回呼び出されることはありません。
     * [Remover.remove]は、[attachTo]の呼び出しに対して必ずしも対応して呼び出される保証はありません。
     */
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover

}

val View.actualSize get() = IntPoint(actualSizeX, actualSizeY)

fun View.calculateActualSize(regionSize: IntPoint) {
    calculateActualSizeX(regionSize.x)
    calculateActualSizeY(regionSize.y)
}
