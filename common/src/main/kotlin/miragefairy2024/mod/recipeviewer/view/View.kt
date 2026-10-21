package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.Remover

/**
 * ステートフルなUI要素です。
 */
interface View {

    val sizingX: Sizing
    val sizingY: Sizing

    /**
     * [miragefairy2024.mod.recipeviewer.view.RenderingProxy] を受け取り、サイズ計算を初期化するのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.calculateContentSizeX] と [miragefairy2024.mod.recipeviewer.view.View.calculateContentSizeY] より前に 1 度だけ呼ばれるのだ～🌱
     */
    fun initSizeCalculation(renderingProxy: RenderingProxy)


    /**
     * このViewのコンテンツの最小サイズのX成分なのだ～🌱
     * 親Viewがレイアウトを決めるときに使うのだ～🌱
     */
    val contentSizeX: Int

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.contentSizeX] を計算するのだ～🌱
     * この値は、このViewが実際に機能的に占有するサイズより大きくても小さくてもかまわないのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後に 1 度だけ呼ばれるのだ～🌱
     */
    fun calculateContentSizeX()


    /**
     * このViewのコンテンツの最小サイズのY成分なのだ～🌱
     * 親Viewがレイアウトを決めるときに使うのだ～🌱
     */
    val contentSizeY: Int

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.contentSizeY] を計算するのだ～🌱
     * この値は、このViewが実際に機能的に占有するサイズより大きくても小さくてもかまわないのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後に 1 度だけ呼ばれるのだ～🌱
     */
    fun calculateContentSizeY()


    /**
     * このViewの実際のサイズ。
     */
    val actualSize: IntPoint

    val actualSizeX: Int
    val actualSizeY: Int

    /** X 方向の実サイズを [regionSizeX] から計算するのだ～🌱 */
    fun calculateActualSizeX(regionSizeX: Int)

    /** Y 方向の実サイズを [regionSizeY] から計算するのだ～🌱 */
    fun calculateActualSizeY(regionSizeY: Int)

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.actualSize] を計算するのだ～🌱
     * この値は [regionSize] より大きくてもかまわないのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.calculateContentSizeX] と [miragefairy2024.mod.recipeviewer.view.View.calculateContentSizeY] の後に 1 度だけ呼ばれるのだ～🌱
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

/** このViewのコンテンツの最小サイズなのだ～🌱 */
val View.contentSize get() = IntPoint(contentSizeX, contentSizeY)

/** この View の両軸の最小サイズと実サイズを順に計算するのだ～🌱 */
fun View.calculateSize(renderingProxy: RenderingProxy, regionSize: IntPoint) {
    initSizeCalculation(renderingProxy)
    calculateContentSizeX()
    calculateContentSizeY()
    calculateActualSize(regionSize)
}
