package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.Remover

/** ステートフルな UI 要素なのだ～🌱 */
interface View {

    val sizingX: Sizing
    val sizingY: Sizing

    /**
     * サイズ計算に使用する [renderingProxy] と、割り当て領域を取得する [regionSizeXProvider]・[regionSizeYProvider] を格納するのだ～🌱
     * 既に存在する子 View も初期化するのだ～🌱
     * サイズの計算や計算済みの値の破棄は行わないのだ～🌱
     */
    fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int)

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.getContentSizeX] を計算して内部状態に格納するのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.getContentSizeX] はこの View が実際に機能的に占有するサイズの X 成分より大きくても小さくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後、[miragefairy2024.mod.recipeviewer.view.View.getContentSizeX] が未計算のときに 1 度だけ呼び出すのだ～🌱
     */
    fun evaluateContentSizeX()

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.getContentSizeY] を計算して内部状態に格納するのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.getContentSizeY] はこの View が実際に機能的に占有するサイズの Y 成分より大きくても小さくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後、[miragefairy2024.mod.recipeviewer.view.View.getContentSizeY] が未計算のときに 1 度だけ呼び出すのだ～🌱
     */
    fun evaluateContentSizeY()

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.getActualSizeX] を計算して内部状態に格納するのだ～🌱
     * 実際のサイズは初期化時に登録した X 軸の割り当て領域より大きくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.evaluateContentSizeX] の後、[miragefairy2024.mod.recipeviewer.view.View.getActualSizeX] が未計算のときに 1 度だけ呼び出すのだ～🌱
     */
    fun evaluateActualSizeX()

    /**
     * [miragefairy2024.mod.recipeviewer.view.View.getActualSizeY] を計算して内部状態に格納するのだ～🌱
     * 実際のサイズは初期化時に登録した Y 軸の割り当て領域より大きくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.evaluateContentSizeY] の後、[miragefairy2024.mod.recipeviewer.view.View.getActualSizeY] が未計算のときに 1 度だけ呼び出すのだ～🌱
     */
    fun evaluateActualSizeY()

    /**
     * このViewを指定された位置に配置します。
     * このメソッドは必ず [miragefairy2024.mod.recipeviewer.view.View.evaluateActualSizeX] と [miragefairy2024.mod.recipeviewer.view.View.evaluateActualSizeY] の両方の後に呼び出すのだ～🌱
     * [Remover.remove]が呼び出されるまでに、同じViewに対してこのメソッドが複数回呼び出されることはありません。
     * [Remover.remove]は、[attachTo]の呼び出しに対して必ずしも対応して呼び出される保証はありません。
     */
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover

}

val View.contentSize get() = IntPoint(evaluateContentSizeX(), evaluateContentSizeY())
val View.actualSize get() = IntPoint(evaluateActualSizeX(), evaluateActualSizeY())
