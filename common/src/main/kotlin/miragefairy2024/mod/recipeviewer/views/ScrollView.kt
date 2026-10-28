package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized

/**
 * 子 View を一方向へスクロールするのだ～🌱
 * スクロール方向のコンテンツが占有する最小のサイズは 0 なのだ～🌱
 * スクロールバーの領域は、スクロールの必要がないときも確保するのだ～🌱
 */
abstract class ScrollView : WrapperView(), PlaceableView {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    var scrollbarThickness = 5
        set(value) {
            require(value >= 0) { "Scrollbar thickness must be non-negative" }
            field = value
        }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))

}

/** 子 View を X 軸方向へスクロールするのだ～🌱 */
class XScrollView : ScrollView() {

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = childView.evaluateContentSizeY() + scrollbarThickness

    override fun evaluateChildRegionSizeX(index: Int) = maxOf(evaluateActualSizeX(), childView.evaluateContentSizeX())
    override fun evaluateChildRegionSizeY(index: Int) = (evaluateActualSizeY() - scrollbarThickness).coerceAtLeast(0)

}

/** 子 View を Y 軸方向へスクロールするのだ～🌱 */
class YScrollView : ScrollView() {

    override fun evaluateContentSizeXImpl() = childView.evaluateContentSizeX() + scrollbarThickness
    override fun evaluateContentSizeYImpl() = 0

    override fun evaluateChildRegionSizeX(index: Int) = (evaluateActualSizeX() - scrollbarThickness).coerceAtLeast(0)
    override fun evaluateChildRegionSizeY(index: Int) = maxOf(evaluateActualSizeY(), childView.evaluateContentSizeY())

}
