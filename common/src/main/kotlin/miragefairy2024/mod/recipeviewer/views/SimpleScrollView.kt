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
import miragefairy2024.util.flatten
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
 * 領域とバーの指定位置を [sharedOffset] と同期し、領域の寸法をバーへ反映するのだ～🌱
 * 初期同期では [sharedOffset] の指定位置を使い、戻り値で位置と寸法の購読を解除するのだ～🌱
 */
fun connectScrollbar(
    scrollView: SimpleScrollView,
    scrollOffset: ObservableValue<Double>,
    scrollbarView: ScrollbarView,
    scrollbarOffset: ObservableValue<Double>,
    sharedOffset: ObservableValue<Double> = ObservableValue(scrollOffset.value),
): Remover {

    val offsets = listOf(scrollOffset, scrollbarOffset).distinct().filter { it !== sharedOffset }

    fun updateBounds() {
        val bounds = scrollView.scrollBounds.value
        scrollbarView.contentLength = bounds.contentLength
        scrollbarView.viewportLength = bounds.viewportLength
    }

    fun publishOffset(offset: ObservableValue<Double>) {
        val requested = sharedOffset.value
        offset.value = requested
        // 購読先が前回通知された値へ戻ると通知が省かれるので、代入後の値も確認するのだ～🌱
        if (sharedOffset.value.equals(requested)) sharedOffset.value = offset.value
    }

    val scrollBoundsRemover = scrollView.scrollBounds.register { _, _ ->
        updateBounds()
    }
    val sharedRemover = sharedOffset.register { _, _ ->
        offsets.forEach { publishOffset(it) }
    }
    val offsetRemovers = offsets.map { offset ->
        offset.register { _, position ->
            sharedOffset.value = position
            // 共通位置が元に戻って再通知されなかった場合も、入力元まで同期するのだ～🌱
            if (offset.value.equals(position) && !sharedOffset.value.equals(position)) publishOffset(offset)
        }
    }

    val remover = (offsetRemovers + scrollBoundsRemover + sharedRemover).flatten()
    try {
        updateBounds()
        offsets.forEach { publishOffset(it) }
    } catch (e: Throwable) {
        remover.remove()
        throw e
    }
    return remover
}
