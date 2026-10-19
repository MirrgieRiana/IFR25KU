package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.Remover

/**
 * ステートフルなUI要素です。
 */
interface View {

    val sizingX: Sizing
    val sizingY: Sizing

    /**
     * 描画に関する計算のための [miragefairy2024.mod.recipeviewer.view.RenderingProxy] を受け取るのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.calculateContentSizeX] と [miragefairy2024.mod.recipeviewer.view.View.calculateContentSizeY] より前に 1 度だけ呼ばれるのだ～🌱
     */
    fun setRenderingProxy(renderingProxy: RenderingProxy)


    /**
     * このViewのコンテンツの最小サイズのX成分なのだ～🌱
     * 親Viewがレイアウトを決めるときに使うのだ～🌱
     */
    val contentSizeX: Int

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.contentSizeX] を計算するのだ～🌱
     * この値は、このViewが実際に機能的に占有するサイズより大きくても小さくてもかまわないのだ～🌱
     * [regionSizeYProvider] は、X成分の決定にY方向の利用可能なサイズが要る場合にだけ呼ぶのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.setRenderingProxy] の後に 1 度だけ呼ばれるのだ～🌱
     */
    fun calculateContentSizeX(regionSizeYProvider: () -> Int)


    /**
     * このViewのコンテンツの最小サイズのY成分なのだ～🌱
     * 親Viewがレイアウトを決めるときに使うのだ～🌱
     */
    val contentSizeY: Int

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.contentSizeY] を計算するのだ～🌱
     * この値は、このViewが実際に機能的に占有するサイズより大きくても小さくてもかまわないのだ～🌱
     * [regionSizeXProvider] は、Y成分の決定にX方向の利用可能なサイズが要る場合にだけ呼ぶのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.setRenderingProxy] の後に 1 度だけ呼ばれるのだ～🌱
     */
    fun calculateContentSizeY(regionSizeXProvider: () -> Int)


    /**
     * このViewの実際のサイズ。
     */
    val actualSize: IntPoint

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

/**
 * 親を持たないViewのために、両方の軸のコンテンツの最小サイズを順に計算するのだ～🌱
 * X成分を先に確定させるから、Y成分の計算ではX成分を参照できて、その逆はできないのだ～🌱
 */
fun View.calculateSize(renderingProxy: RenderingProxy, regionSize: IntPoint) {
    setRenderingProxy(renderingProxy)
    calculateContentSizeX { throw IllegalStateException("The root view cannot refer to the available size of the Y axis.") }
    calculateContentSizeY { contentSizeX }
    calculateActualSize(regionSize)
}
