package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Remover
import miragefairy2024.util.fire
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
 * スクロール位置はコンテンツ座標で保持し、再配置時にも引き継ぐのだ～🌱
 * バニラでは、親のコンテキストが許可していれば、子孫に [miragefairy2024.mod.recipeviewer.views.MenuSlotView] を配置できるのだ～🌱
 */
abstract class ScrollContainerView : WrapperView(), PlaceableView {

    override var sizingX = Sizing.FILL
    override var sizingY = Sizing.FILL

    var scrollStep = 15
        set(value) {
            if (value <= 0) throw IllegalArgumentException("Scroll step must be positive")
            field = value
        }

    val scrollBounds = ObservableValue(ScrollBounds(0, 0))

    val onScrollInput = EventRegistry<() -> Unit>()

    protected abstract fun evaluateContentLength(): Int
    protected abstract fun evaluateViewportLength(): Int

    /** 寸法計算の初期化後に、現在のスクロール位置を通常範囲へ収めるのだ～🌱 */
    abstract fun clampScrollOffset()

    protected fun coerceScrollOffset(scrollOffset: Double): Double {
        val maxScrollOffset = (evaluateContentLength() - evaluateViewportLength() atLeast 0).toDouble()
        return scrollOffset.coerceIn(0.0, maxScrollOffset)
    }

    fun updateScrollBounds() {
        scrollBounds.value = ScrollBounds(evaluateContentLength() atLeast 0, evaluateViewportLength() atLeast 0)
    }

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        updateScrollBounds()
        return viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }

}

/** 子 View を X 軸方向へスクロールするのだ～🌱 */
class XScrollContainerView : ScrollContainerView() {

    val scrollOffsetX = ObservableValue(0.0)

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = childView.evaluateContentSizeY()

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() max childView.evaluateContentSizeX()
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() atLeast 0

    override fun evaluateContentLength() = childView.evaluateActualSizeX()
    override fun evaluateViewportLength() = evaluateActualSizeX()

    override fun clampScrollOffset() {
        scrollOffsetX.value = coerceScrollOffset(scrollOffsetX.value)
    }

}

/** 子 View を Y 軸方向へスクロールするのだ～🌱 */
class YScrollContainerView : ScrollContainerView() {

    val scrollOffsetY = ObservableValue(0.0)

    override fun evaluateContentSizeXImpl() = childView.evaluateContentSizeX()
    override fun evaluateContentSizeYImpl() = 0

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() atLeast 0
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() max childView.evaluateContentSizeY()

    override fun evaluateContentLength() = childView.evaluateActualSizeY()
    override fun evaluateViewportLength() = evaluateActualSizeY()

    override fun clampScrollOffset() {
        scrollOffsetY.value = coerceScrollOffset(scrollOffsetY.value)
    }

}

/**
 * X 軸の領域とバーを接続し、その間のコンテンツ座標の指定位置と領域の寸法を共有するのだ～🌱
 * 解除した両側は、解除時の位置からそれぞれ操作できるのだ～🌱
 */
fun connectScrollbar(scrollView: XScrollContainerView, scrollbarView: XFlatScrollbarView): Remover {
    return connectScrollbarImpl(scrollView, scrollbarView, scrollView.scrollOffsetX)
}

/**
 * Y 軸の領域とバーを接続し、その間のコンテンツ座標の指定位置と領域の寸法を共有するのだ～🌱
 * 解除した両側は、解除時の位置からそれぞれ操作できるのだ～🌱
 */
fun connectScrollbar(scrollView: YScrollContainerView, scrollbarView: YFlatScrollbarView): Remover {
    return connectScrollbarImpl(scrollView, scrollbarView, scrollView.scrollOffsetY)
}

private fun connectScrollbarImpl(
    scrollView: ScrollContainerView,
    scrollbarView: FlatScrollbarView,
    scrollViewOffset: ObservableValue<Double>,
): Remover {
    val initialScrollViewOffset = scrollViewOffset.value
    val initialScrollbarViewOffset = scrollbarView.scrollOffset.value
    var remover = Remover.NONE
    val sharedOffset = ObservableValue(initialScrollViewOffset)

    // 領域の寸法→スクロールバーのパラメータ
    remover += scrollView.scrollBounds.register { _, bounds ->
        scrollbarView.contentLength = bounds.contentLength
        scrollbarView.viewportLength = bounds.viewportLength
    }
    remover += scrollView.onScrollInput.register {
        scrollbarView.onScrollInput.fire()
    }

    fun connectOffset(offset: ObservableValue<Double>) {
        // 共有値→View固有値
        remover += sharedOffset.register { _, _ ->
            offset.value = sharedOffset.value
        }
        // View固有値→共有値
        remover += offset.register { _, position ->
            sharedOffset.value = position
        }
    }
    connectOffset(scrollViewOffset)
    connectOffset(scrollbarView.scrollOffset)

    try {
        scrollbarView.contentLength = scrollView.scrollBounds.value.contentLength
        scrollbarView.viewportLength = scrollView.scrollBounds.value.viewportLength
        scrollViewOffset.value = sharedOffset.value
        scrollbarView.scrollOffset.value = sharedOffset.value
    } catch (e: Throwable) {
        remover.remove()

        fun restoreOffset(offset: ObservableValue<Double>, position: Double) {
            try {
                offset.value = position
            } catch (e2: Throwable) {
                if (e2 !== e) e.addSuppressed(e2)
            }
        }

        restoreOffset(scrollViewOffset, initialScrollViewOffset)
        restoreOffset(scrollbarView.scrollOffset, initialScrollbarViewOffset)
        throw e
    }
    return remover
}
