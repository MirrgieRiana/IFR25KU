package miragefairy2024.mod.recipeviewer.view

/** ステートフルな UI 要素なのだ～🌱 */
interface View {

    val sizingX: Sizing
    val sizingY: Sizing

    /**
     * サイズ計算に使用する [renderingProxy] と、割り当て領域を取得する [regionSizeXProvider]・[regionSizeYProvider] を格納するのだ～🌱
     * 計算済みのサイズを破棄し、既に存在する子 View も初期化するのだ～🌱
     * 初期化自体ではサイズを計算しないのだ～🌱
     */
    fun initSizeCalculation(renderingProxy: RenderingProxy, regionSizeXProvider: () -> Int, regionSizeYProvider: () -> Int)

    /**
     * この View のコンテンツが占有する最小のサイズの X 成分を返すのだ～🌱
     * 未計算なら計算して内部状態に格納し、計算済みならその値を返すのだ～🌱
     * 戻り値は、親 View のレイアウトを決定するための値なのだ～🌱
     * この値はこの View が実際に機能的に占有するサイズの X 成分より大きくても小さくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後に呼び出さなければならないのだ～🌱
     */
    fun evaluateContentSizeX(): Int

    /**
     * この View のコンテンツが占有する最小のサイズの Y 成分を返すのだ～🌱
     * 未計算なら計算して内部状態に格納し、計算済みならその値を返すのだ～🌱
     * 戻り値は、親 View のレイアウトを決定するための値なのだ～🌱
     * この値はこの View が実際に機能的に占有するサイズの Y 成分より大きくても小さくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後に呼び出さなければならないのだ～🌱
     */
    fun evaluateContentSizeY(): Int

    /**
     * この View 自身の実際のサイズの X 成分を返すのだ～🌱
     * 未計算なら必要な依存先とともに計算して内部状態に格納し、計算済みならその値を返すのだ～🌱
     * 実際のサイズは初期化時に登録した X 軸の割り当て領域より大きくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後に呼び出さなければならないのだ～🌱
     */
    fun evaluateActualSizeX(): Int

    /**
     * この View 自身の実際のサイズの Y 成分を返すのだ～🌱
     * 未計算なら必要な依存先とともに計算して内部状態に格納し、計算済みならその値を返すのだ～🌱
     * 実際のサイズは初期化時に登録した Y 軸の割り当て領域より大きくてもかまわないのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後に呼び出さなければならないのだ～🌱
     */
    fun evaluateActualSizeY(): Int

    /**
     * この View を指定された位置に配置するのだ～🌱
     * [miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] の後に呼び出さなければならないのだ～🌱
     * 同じ View に再び呼び出す前に、前回の戻り値の [miragefairy2024.mod.recipeviewer.view.ViewController.remove] を呼び出さなければならないのだ～🌱
     * 前回の配置を解除し、[miragefairy2024.mod.recipeviewer.view.View.initSizeCalculation] で再初期化した後は、同じ View を再配置できるのだ～🌱
     * 戻り値の [miragefairy2024.mod.recipeviewer.view.ViewController.remove] が必ず呼び出される保証はないのだ～🌱
     */
    fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController

}

fun View.evaluateContentSize() = IntPoint(evaluateContentSizeX(), evaluateContentSizeY())
fun View.evaluateActualSize() = IntPoint(evaluateActualSizeX(), evaluateActualSizeY())
