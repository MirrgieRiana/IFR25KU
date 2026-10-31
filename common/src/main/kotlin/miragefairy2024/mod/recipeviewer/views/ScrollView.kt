package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

/** スクロールバーの見た目なのだ～🌱 */
sealed interface ScrollbarStyle {

    /** 単色の矩形を塗りつぶして、操作が止まると徐々に透明にしていく見た目なのだ～🌱 */
    data class Flat(val argb: Int) : ScrollbarStyle

    /** バニラのスクローラーのスプライトを使う見た目なのだ～🌱 */
    data object Vanilla : ScrollbarStyle

}

/**
 * 縦に溢れる子を、スクロールして見せる View なのだ～🌱
 * 子には、この View の幅と、子のコンテンツの高さが渡るのだ～🌱
 * スクロールバーの幅は、[miragefairy2024.mod.recipeviewer.views.ScrollView.scrollbarThickness] で宣言するだけで、レイアウトからは引かれないのだ～🌱
 * 中身とスクロールバーを重ねたくない場合は、子を [miragefairy2024.mod.recipeviewer.views.MarginView] で包んで右端に余白を作るのだ～🌱
 */
class ScrollView(val scrollbarThickness: Int, val scrollbarStyle: ScrollbarStyle) : WrapperView(), PlaceableView {

    override val sizingX get() = childView.sizingX

    override val sizingY = Sizing.FILL

    var scrollStep = 15.0

    /** 縦は溢れてよいため、親へ要求する高さを 0 にするのだ～🌱 */
    override fun evaluateContentSizeYImpl() = 0

    override fun evaluateChildRegionSizeY(index: Int) = childView.evaluateContentSizeY()

    /** 子がこの View からはみ出している高さなのだ～🌱 */
    val maxScrollAmount get() = (childView.evaluateActualSizeY() - evaluateActualSizeY()) max 0

    /** 一番上を 0 とした、現在のスクロール量なのだ～🌱 */
    var scrollAmount = 0
        set(value) {
            field = value min maxScrollAmount max 0
        }

    /**
     * 子を置かず、この View 自身だけを置くのだ～🌱
     * 子は、スクロール量に応じて位置が変わるため、この View を受け取ったバックエンドが、独自の領域の中へ置くのだ～🌱
     */
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        return viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }

}

fun <P> Child<P, *>.scroll(scrollbarThickness: Int, scrollbarStyle: ScrollbarStyle) = this.wrap(ScrollView(scrollbarThickness, scrollbarStyle))
