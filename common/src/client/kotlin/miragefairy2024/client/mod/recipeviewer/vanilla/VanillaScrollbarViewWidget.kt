package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.util.empty
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

abstract class VanillaScrollbarViewWidget<V : ScrollbarView>(
    protected val view: V,
    bounds: IntRectangle,
) :
    AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }), MouseCaptureContainer {


    // ライフサイクルなのだ～🌱

    private var disposed = false

    open fun dispose() {
        disposed = true
        draggingScrollbar = false
        focusedSelf = false
    }


    // サイズと位置なのだ～🌱

    protected abstract val scrollbarLength: Int
    protected abstract val scrollbarThickness: Int

    // 範囲が狭まっても、現在の位置から操作できるようにするのだ～🌱
    protected fun evaluateScrollRange(): ClosedFloatingPointRange<Double> {
        val normalMaxScrollOffset = (view.contentLength - view.viewportLength atLeast 0).toDouble()
        return minOf(0.0, view.scrollOffset.value)..maxOf(normalMaxScrollOffset, view.scrollOffset.value)
    }

    protected fun evaluateThumbLength(): Int {
        if (scrollbarLength <= 0) return 0
        val range = evaluateScrollRange()
        val scrollLength = range.endInclusive - range.start
        if (scrollLength == 0.0) return scrollbarLength
        val visibleRatio = view.viewportLength / (view.viewportLength + scrollLength)
        val rawThumbLength = (scrollbarLength.toDouble() * visibleRatio).roundToInt()
        val minThumbLength = scrollbarThickness.coerceIn(1, scrollbarLength)
        return rawThumbLength.coerceIn(minThumbLength, scrollbarLength)
    }

    protected fun evaluateThumbPosition(): Double {
        val range = evaluateScrollRange()
        return if (range.start == range.endInclusive) {
            0.0
        } else {
            val maxThumbPosition = scrollbarLength - evaluateThumbLength()
            val scrollRatio = (view.scrollOffset.value - range.start) / (range.endInclusive - range.start)
            maxThumbPosition.toDouble() * scrollRatio
        }
    }


    // スクロール操作なのだ～🌱

    private fun updateScroll(offset: Double): Boolean {
        val old = view.scrollOffset.value
        val range = evaluateScrollRange()
        view.scrollOffset.value = offset.coerceIn(range.start, range.endInclusive)
        return view.scrollOffset.value != old
    }


    // マウス入力なのだ～🌱

    private var draggingScrollbar = false

    override fun isActive() = super.isActive() && !disposed

    override fun isMouseOver(mouseX: Double, mouseY: Double) = isActive && super.isMouseOver(mouseX, mouseY)

    protected abstract fun getMousePosition(mouseX: Double, mouseY: Double): Double

    protected abstract fun getDragAmount(dragX: Double, dragY: Double): Double

    protected abstract fun getScrollAmount(scrollX: Double, scrollY: Double): Double

    override fun hasMouseCapture(button: Int) = !disposed && button == 0 && draggingScrollbar

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) draggingScrollbar = false
        if (!isActive) return false

        if (super.isMouseOver(mouseX, mouseY)) {
            // スクロールバーのドラッグ開始なのだ～🌱
            if (button != 0) return false
            val range = evaluateScrollRange()
            if (range.start == range.endInclusive) return false
            val thumbLength = evaluateThumbLength()
            val remainingThumbLength = scrollbarLength - thumbLength
            if (remainingThumbLength <= 0) return false
            draggingScrollbar = true
            val mousePosition = getMousePosition(mouseX, mouseY)
            val thumbPosition = evaluateThumbPosition()
            if (mousePosition < thumbPosition || mousePosition >= thumbPosition + thumbLength) {
                val scrollRatio = ((mousePosition - thumbLength / 2.0) / remainingThumbLength).coerceIn(0.0, 1.0)
                updateScroll(range.start + (range.endInclusive - range.start) * scrollRatio)
            }
            return true
        } else {
            return false
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (button == 0 && draggingScrollbar) {
            val remainingThumbLength = scrollbarLength - evaluateThumbLength()
            if (remainingThumbLength > 0) {
                val range = evaluateScrollRange()
                val scrollDelta = getDragAmount(dragX, dragY) * (range.endInclusive - range.start) / remainingThumbLength
                updateScroll(view.scrollOffset.value + scrollDelta)
            }
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false
            return true
        }
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!isMouseOver(mouseX, mouseY)) return false
        val amount = getScrollAmount(scrollX, scrollY)
        if (amount == 0.0) return false
        val handled = updateScroll(view.scrollOffset.value - amount * view.scrollStep)
        view.onScrollInput.fire()
        return handled
    }


    // フォーカスとキーボード入力なのだ～🌱

    private var focusedSelf = false

    override fun isFocused() = focusedSelf

    override fun setFocused(focused: Boolean) {
        focusedSelf = focused
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {

        if (!isActive || view.isAttached) return null

        val range = evaluateScrollRange()
        if (range.start < range.endInclusive && !isFocused) {
            return object : ComponentPath {
                override fun component() = this@VanillaScrollbarViewWidget

                override fun applyFocus(focused: Boolean) {
                    this@VanillaScrollbarViewWidget.isFocused = focused
                }
            }
        }
        return null
    }

    override fun getCurrentFocusPath(): ComponentPath? {
        if (isFocused) return ComponentPath.leaf(this)
        return null
    }

    protected abstract val backwardKey: Int
    protected abstract val forwardKey: Int

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isActive) return false
        if (!isFocused) return false
        val offset = when (keyCode) {
            backwardKey -> view.scrollOffset.value - view.scrollStep
            forwardKey -> view.scrollOffset.value + view.scrollStep
            GLFW.GLFW_KEY_PAGE_UP -> view.scrollOffset.value - view.viewportLength * 0.8
            GLFW.GLFW_KEY_PAGE_DOWN -> view.scrollOffset.value + view.viewportLength * 0.8
            GLFW.GLFW_KEY_HOME -> evaluateScrollRange().start
            GLFW.GLFW_KEY_END -> evaluateScrollRange().endInclusive
            else -> return false
        }
        val handled = updateScroll(offset)
        view.onScrollInput.fire()
        return handled
    }


    // 読み上げなのだ～🌱

    override fun narrationPriority(): NarratableEntry.NarrationPriority {
        if (!isActive) return NarratableEntry.NarrationPriority.NONE
        if (isFocused) return NarratableEntry.NarrationPriority.FOCUSED
        return NarratableEntry.NarrationPriority.NONE
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        if (isFocused) {
            narrationElementOutput.add(NarratedElementType.TITLE, text { SCROLL_AREA_TRANSLATION() })
            return
        }
    }

}
