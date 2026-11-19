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

    val connectedScrollOffset = ObservableValue<ObservableValue<Double>?>(null)

    protected abstract fun evaluateContentLength(): Int
    protected abstract fun evaluateViewportLength(): Int

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
        scrollBounds.value = ScrollBounds(evaluateContentLength() atLeast 0, evaluateViewportLength() atLeast 0)
        return viewPlacer.place(this, offset.sized(evaluateActualSize()))
    }

}

/** 子 View を X 軸方向へスクロールするのだ～🌱 */
class XSimpleScrollView : SimpleScrollView() {

    val scrollOffsetX = ObservableValue(0.0)

    override fun evaluateContentSizeXImpl() = 0
    override fun evaluateContentSizeYImpl() = childView.evaluateContentSizeY()

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() max childView.evaluateContentSizeX()
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() atLeast 0

    override fun evaluateContentLength() = childView.evaluateActualSizeX()
    override fun evaluateViewportLength() = evaluateActualSizeX()

}

/** 子 View を Y 軸方向へスクロールするのだ～🌱 */
class YSimpleScrollView : SimpleScrollView() {

    val scrollOffsetY = ObservableValue(0.0)

    override fun evaluateContentSizeXImpl() = childView.evaluateContentSizeX()
    override fun evaluateContentSizeYImpl() = 0

    override fun evaluateChildRegionSizeX(index: Int) = evaluateActualSizeX() atLeast 0
    override fun evaluateChildRegionSizeY(index: Int) = evaluateActualSizeY() max childView.evaluateContentSizeY()

    override fun evaluateContentLength() = childView.evaluateActualSizeY()
    override fun evaluateViewportLength() = evaluateActualSizeY()

}

/**
 * 領域とバーを接続し、その間のコンテンツ座標の指定位置と領域の寸法を共有するのだ～🌱
 * 解除した両側は、解除時の位置からそれぞれ操作できるのだ～🌱
 */
fun connectScrollbar(scrollView: SimpleScrollView, scrollbarView: ScrollbarView): Remover {
    check(scrollView.connectedScrollOffset.value == null) { "The scroll view is already connected" }
    check(scrollbarView.connectedScrollOffset.value == null) { "The scrollbar is already connected" }

    val scrollViewOffset = when (scrollView) {
        is XSimpleScrollView -> scrollView.scrollOffsetX
        is YSimpleScrollView -> scrollView.scrollOffsetY
        else -> throw IllegalArgumentException("Unsupported view: $scrollView")
    }
    val scrollbarViewOffset = when (scrollbarView) {
        is XScrollbarView -> scrollbarView.scrollOffsetX
        is YScrollbarView -> scrollbarView.scrollOffsetY
        else -> throw IllegalArgumentException("Unsupported view: $scrollbarView")
    }
    val sharedOffset = ObservableValue(scrollViewOffset.value)

    fun updateBounds(bounds: ScrollBounds) {
        scrollbarView.contentLength = bounds.contentLength
        scrollbarView.viewportLength = bounds.viewportLength
    }

    val boundsRemover = scrollView.scrollBounds.register { _, bounds -> updateBounds(bounds) }

    fun disconnect(position: Double?) {
        boundsRemover.remove()

        fun releaseOffset(connection: ObservableValue<ObservableValue<Double>?>, standaloneOffset: ObservableValue<Double>) {
            if (connection.value !== sharedOffset) return
            try {
                if (position != null) standaloneOffset.value = position
            } finally {
                if (connection.value === sharedOffset) connection.value = null
            }
        }

        try {
            releaseOffset(scrollView.connectedScrollOffset, scrollViewOffset)
        } finally {
            releaseOffset(scrollbarView.connectedScrollOffset, scrollbarViewOffset)
        }
    }

    try {
        updateBounds(scrollView.scrollBounds.value)
        scrollView.connectedScrollOffset.value = sharedOffset
        scrollbarView.connectedScrollOffset.value = sharedOffset
    } catch (e: Throwable) {
        try {
            disconnect(null)
        } catch (cleanupFailure: Throwable) {
            if (cleanupFailure !== e) e.addSuppressed(cleanupFailure)
        }
        throw e
    }
    return Remover { disconnect(sharedOffset.value) }
}
