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
     * [miragefairy2024.mod.recipeviewer.view.View.contentSizeX] を要求時に計算するための取得関数を設定するのだ～🌱
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
     * [miragefairy2024.mod.recipeviewer.view.View.contentSizeY] を要求時に計算するための取得関数を設定するのだ～🌱
     * この値は、このViewが実際に機能的に占有するサイズより大きくても小さくてもかまわないのだ～🌱
     * [regionSizeXProvider] は、Y成分の決定にX方向の利用可能なサイズが要る場合にだけ呼ぶのだ～🌱
     * 必ず [miragefairy2024.mod.recipeviewer.view.View.setRenderingProxy] の後に 1 度だけ呼ばれるのだ～🌱
     */
    fun calculateContentSizeY(regionSizeXProvider: () -> Int)


    /**
     * このViewの実際のサイズ。
     */
    val actualSize: IntPoint

    val actualSizeX: Int
    val actualSizeY: Int

    /** X 方向の利用可能なサイズの取得関数を設定し、子の X 方向の領域配分を準備するのだ～🌱 */
    fun calculateActualSizeX(regionSizeXProvider: () -> Int)

    /** Y 方向の利用可能なサイズの取得関数を設定し、子の Y 方向の領域配分を準備するのだ～🌱 */
    fun calculateActualSizeY(regionSizeYProvider: () -> Int)

    /** 両軸の取得関数を設定した後に、子を含めたサイズ計算を完了するのだ～🌱 */
    fun finishSizeCalculation()


    /**
     * このViewを指定された位置に配置します。
     * このメソッドは必ず [miragefairy2024.mod.recipeviewer.view.View.finishSizeCalculation] の後に呼び出されるのだ～🌱
     * [Remover.remove]が呼び出されるまでに、同じViewに対してこのメソッドが複数回呼び出されることはありません。
     * [Remover.remove]は、[attachTo]の呼び出しに対して必ずしも対応して呼び出される保証はありません。
     */
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover

}

/** このViewのコンテンツの最小サイズなのだ～🌱 */
val View.contentSize get() = IntPoint(contentSizeX, contentSizeY)

/**
 * 親を持たない View の両軸の取得関数を準備し、依存関係に従ってサイズを計算するのだ～🌱
 */
fun View.calculateSize(renderingProxy: RenderingProxy, regionSize: IntPoint) {
    setRenderingProxy(renderingProxy)
    calculateContentSizeX { regionSize.y }
    calculateContentSizeY { regionSize.x }
    calculateActualSizeX { regionSize.x }
    calculateActualSizeY { regionSize.y }
    finishSizeCalculation()
}
