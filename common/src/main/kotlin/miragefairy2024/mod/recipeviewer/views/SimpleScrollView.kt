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
import miragefairy2024.util.registerAndInitialize
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

/** スクロール領域の寸法をバーへ同期し、両側の指定位置を共通の位置に同期するのだ～🌱 */
fun connectScrollbar(
    scrollView: SimpleScrollView,
    scrollOffset: ObservableValue<Double>,
    scrollbarView: ScrollbarView,
    scrollbarOffset: ObservableValue<Double>,
): Remover {

    val sharedOffset = ObservableValue(scrollOffset.value)

    fun connectOffset(offset: ObservableValue<Double>): Remover {
        var publishing = false

        fun publishOffset() {
            val requested = sharedOffset.value
            val wasPublishing = publishing
            publishing = true
            try {
                offset.value = requested
            } finally {
                publishing = wasPublishing
            }
            // 同じ共有値の反映中に補正された位置を、共有値に反映するのだ～🌱
            if (sharedOffset.value.equals(requested) && !offset.value.equals(requested)) sharedOffset.value = offset.value
        }

        val sharedRemover = sharedOffset.register { _, _ -> publishOffset() }
        val offsetRemover = offset.register { _, position ->
            if (publishing) return@register
            sharedOffset.value = position
            // 共有値が元の値に戻って通知を省いた場合も、その値を反映するのだ～🌱
            if (offset.value.equals(position) && !sharedOffset.value.equals(position)) publishOffset()
        }
        val remover = sharedRemover + offsetRemover
        try {
            publishOffset()
        } catch (e: Throwable) {
            remover.remove()
            throw e
        }
        return remover
    }

    var remover = scrollView.scrollBounds.registerAndInitialize { _, bounds ->
        scrollbarView.contentLength = bounds.contentLength
        scrollbarView.viewportLength = bounds.viewportLength
    }
    try {
        remover += connectOffset(scrollOffset)
        remover += connectOffset(scrollbarOffset)
    } catch (e: Throwable) {
        remover.remove()
        throw e
    }
    return remover
}
