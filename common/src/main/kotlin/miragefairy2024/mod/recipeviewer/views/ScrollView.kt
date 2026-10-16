package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.Remover
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

/**
 * 縦に溢れる子を、スクロールして見せるViewなのだ～🌱
 * 子には、このViewの幅と、子のコンテンツの高さが渡るのだ～🌱
 * スクロールバーの幅は、[scrollbarThickness]で宣言するだけで、レイアウトからは引かれないのだ～🌱
 * 中身とスクロールバーを重ねたくない場合は、子を[MarginView]で包んで右端に余白を作るのだ～🌱
 */
class ScrollView(val scrollbarThickness: Int) : WrapperView(), PlaceableView {

    override val sizingX get() = childView.sizingX

    override val sizingY = Sizing.FILL

    /** 縦は溢れてよいため、親へ要求する高さを0にするのだ～🌱 */
    override fun calculateContentSize() = IntPoint(super.calculateContentSize().x, 0)

    override fun calculateChildrenActualSize() {
        childView.calculateActualSize(IntPoint(actualSize.x, childView.contentSize.y))
    }

    /** 子がこのViewからはみ出している高さなのだ～🌱 */
    val maxScrollAmount get() = (childView.actualSize.y - actualSize.y) max 0

    /** 一番上を0とした、現在のスクロール量なのだ～🌱 */
    var scrollAmount = 0
        set(value) {
            field = value min maxScrollAmount max 0
        }

    /**
     * 子を置かず、このView自身だけを置くのだ～🌱
     * 子は、スクロール量に応じて位置が変わるため、このViewを受け取ったバックエンドが、独自の領域の中へ置くのだ～🌱
     */
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        return viewPlacer.place(this, offset.sized(actualSize))
    }

}

fun <P> Child<P, *>.scroll(scrollbarThickness: Int) = this.wrap(ScrollView(scrollbarThickness))
