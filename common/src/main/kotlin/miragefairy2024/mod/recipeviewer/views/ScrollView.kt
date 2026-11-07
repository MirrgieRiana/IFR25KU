package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.FlatScrollbar
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Scrollbar
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max

/**
 * 子 View を一方向へスクロールするのだ～🌱
 * スクロール方向のコンテンツが占有する最小のサイズは `0` なのだ～🌱
 * スクロールバーが指定する占有幅は、スクロールの必要がないときも確保するのだ～🌱
 * バニラへの配置では、子孫に [miragefairy2024.mod.recipeviewer.views.MenuSlotView] を含めてはならないのだ～🌱
 */
abstract class ScrollView : WrapperView(), PlaceableView {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    var scrollbar: Scrollbar = FlatScrollbar
    var scrollbarThickness = 5
        set(value) {
            if (value < 0) throw IllegalArgumentException("Scrollbar thickness must be non-negative")
            field = value
        }
    var scrollStep = 15
        set(value) {
            if (value <= 0) throw IllegalArgumentException("Scroll step must be positive")
            field = value
        }
    var scrollbarColor = 0xA0000000.toInt()

    val reservedScrollbarThickness get() = scrollbar.getReservedThickness(scrollbarThickness)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))

}

/** 子 View を X 軸方向へスクロールするのだ～🌱 */
class XScrollView : ScrollView() {

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = childView.evaluateContentSizeY() + reservedScrollbarThickness

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() max childView.evaluateContentSizeX()
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() - reservedScrollbarThickness atLeast 0

}

/** 子 View を Y 軸方向へスクロールするのだ～🌱 */
class YScrollView : ScrollView() {

    override fun evaluateContentSizeXImpl() = childView.evaluateContentSizeX() + reservedScrollbarThickness
    override fun evaluateContentSizeYImpl() = 0

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() - reservedScrollbarThickness atLeast 0
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() max childView.evaluateContentSizeY()

}
