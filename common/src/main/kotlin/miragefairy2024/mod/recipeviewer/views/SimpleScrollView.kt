package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Remover
import miragefairy2024.util.plus
import miragefairy2024.util.register
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max

data class ScrollBounds(val contentLength: Int, val viewportLength: Int) {
    init {
        require(contentLength >= 0 && viewportLength >= 0) { "Scroll lengths must be non-negative" }
    }
}

/**
 * 子 View を一方向へスクロールするのだ～🌱
 * スクロール方向のコンテンツが占有する最小のサイズは `0` なのだ～🌱
 * バニラへの配置では、子孫に [miragefairy2024.mod.recipeviewer.views.MenuSlotView] を含めてはならないのだ～🌱
 */
abstract class SimpleScrollView : WrapperView(), PlaceableView {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    var scrollStep = 15
        set(value) {
            if (value <= 0) throw IllegalArgumentException("Scroll step must be positive")
            field = value
        }

    val scrollBounds = ObservableValue(ScrollBounds(0, 0))

    protected abstract fun evaluateContentLength(): Int
    protected abstract fun evaluateViewportLength(): Int

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        scrollBounds.value = ScrollBounds(evaluateContentLength() atLeast 0, evaluateViewportLength() atLeast 0)
        return viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }

}

/** 子 View を X 軸方向へスクロールするのだ～🌱 */
class XSimpleScrollView : SimpleScrollView() {

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = childView.evaluateContentSizeY()

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() max childView.evaluateContentSizeX()
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() atLeast 0

    override fun evaluateContentLength() = childView.evaluateActualSizeX()
    override fun evaluateViewportLength() = evaluateActualSizeX()

}

/** 子 View を Y 軸方向へスクロールするのだ～🌱 */
class YSimpleScrollView : SimpleScrollView() {

    override fun evaluateContentSizeXImpl() = childView.evaluateContentSizeX()
    override fun evaluateContentSizeYImpl() = 0

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() atLeast 0
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() max childView.evaluateContentSizeY()

    override fun evaluateContentLength() = childView.evaluateActualSizeY()
    override fun evaluateViewportLength() = evaluateActualSizeY()

}

/**
 * 領域のコンテンツ全長と表示領域長をバーへ反映し、別々の指定位置を双方向に同期するのだ～🌱
 * 両方の位置引数に同じ `ObservableValue` を渡した場合は、寸法だけを購読するのだ～🌱
 * 戻り値で接続の購読を解除するのだ～🌱
 *
 * @param scrollOffset 領域の指定位置で、初回はこの値をバーへ反映するのだ～🌱
 * @param scrollbarOffset バーの指定位置なのだ～🌱
 */
fun connectScrollbar(
    scrollView: SimpleScrollView,
    scrollOffset: ObservableValue<Double>,
    scrollbarView: ScrollbarView,
    scrollbarOffset: ObservableValue<Double>,
): Remover {

    fun updateBounds() {
        val bounds = scrollView.scrollBounds.value
        scrollbarView.contentLength = bounds.contentLength
        scrollbarView.viewportLength = bounds.viewportLength
    }

    var updatingOffset = false
    fun updateOffset(source: ObservableValue<Double>, target: ObservableValue<Double>) {
        if (updatingOffset) return
        updatingOffset = true
        try {
            while (!source.value.equals(target.value)) {
                val position = source.value
                target.value = position
                // 通知中に反映元が更新された場合は、次の反映で新しい値を使うのだ～🌱
                if (source.value.equals(position)) source.value = target.value
            }
        } finally {
            updatingOffset = false
        }
    }

    val boundsRemover = scrollView.scrollBounds.register { _, _ -> updateBounds() }
    val sharesOffset = scrollOffset === scrollbarOffset
    val remover = if (sharesOffset) {
        boundsRemover
    } else {
        val scrollOffsetRemover = scrollOffset.register { _, _ -> updateOffset(scrollOffset, scrollbarOffset) }
        val scrollbarOffsetRemover = scrollbarOffset.register { _, _ -> updateOffset(scrollbarOffset, scrollOffset) }
        boundsRemover + scrollOffsetRemover + scrollbarOffsetRemover
    }

    try {
        updateBounds()
        if (!sharesOffset) updateOffset(scrollOffset, scrollbarOffset)
    } catch (e: Throwable) {
        remover.remove()
        throw e
    }
    return remover
}
