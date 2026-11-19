package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.empty
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

abstract class VanillaScrollbarViewWidget(
    protected val view: ScrollbarView,
    private val scrollOffsetValue: ObservableValue<Double>,
    bounds: IntRectangle,
) :
    AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }), MouseCaptureContainer {


    // ライフサイクルなのだ～🌱

    private var disposed = false

    fun dispose() {
        disposed = true
        draggingScrollbar = false
        focusedSelf = false
    }


    // サイズと位置なのだ～🌱

    protected abstract val scrollbarLength: Int
    protected abstract val scrollbarThickness: Int
    protected abstract fun evaluateContentLength(): Int

    // 範囲が狭まっても、現在の位置から操作できるようにするのだ～🌱
    private fun evaluateScrollRange(): ClosedFloatingPointRange<Double> {
        val normalMaxScrollOffset = (evaluateContentLength() - view.viewportLength atLeast 0).toDouble()
        return minOf(0.0, scrollOffsetValue.value)..maxOf(normalMaxScrollOffset, scrollOffsetValue.value)
    }

    private fun evaluateThumbLength(): Int {
        if (scrollbarLength <= 0) return 0
        val range = evaluateScrollRange()
        val scrollLength = range.endInclusive - range.start
        if (scrollLength == 0.0) return scrollbarLength
        val visibleRatio = view.viewportLength / (view.viewportLength + scrollLength)
        val rawThumbLength = (scrollbarLength.toDouble() * visibleRatio).roundToInt()
        val minThumbLength = scrollbarThickness.coerceIn(1, scrollbarLength)
        return rawThumbLength.coerceIn(minThumbLength, scrollbarLength)
    }

    private fun evaluateThumbPosition(): Double {
        val range = evaluateScrollRange()
        return if (range.start == range.endInclusive) {
            0.0
        } else {
            val maxThumbPosition = scrollbarLength - evaluateThumbLength()
            val scrollRatio = (scrollOffsetValue.value - range.start) / (range.endInclusive - range.start)
            maxThumbPosition.toDouble() * scrollRatio
        }
    }

    private fun updateScroll(offset: Double): Boolean {
        val old = scrollOffsetValue.value
        val range = evaluateScrollRange()
        scrollOffsetValue.value = offset.coerceIn(range.start, range.endInclusive)
        return scrollOffsetValue.value != old
    }


    // 描画とマウス入力なのだ～🌱

    private var draggingScrollbar = false

    override fun isActive() = super.isActive() && !disposed

    override fun isMouseOver(mouseX: Double, mouseY: Double) = isActive && super.isMouseOver(mouseX, mouseY)

    protected abstract fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int)

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {

        // スクロールバーの描画なのだ～🌱
        val range = evaluateScrollRange()
        if (range.start < range.endInclusive && scrollbarThickness > 0 && evaluateThumbLength() > 0) {
            renderScrollbar(guiGraphics, evaluateThumbPosition().roundToInt(), evaluateThumbLength())
        }

    }

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
                updateScroll(scrollOffsetValue.value + scrollDelta)
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
        return updateScroll(scrollOffsetValue.value - getScrollAmount(scrollX, scrollY) * view.scrollStep)
    }


    // フォーカスとキーボード入力なのだ～🌱

    private var focusedSelf = false

    override fun isFocused() = focusedSelf

    override fun setFocused(focused: Boolean) {
        focusedSelf = focused
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {

        if (!isActive) return null

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
            backwardKey -> scrollOffsetValue.value - view.scrollStep
            forwardKey -> scrollOffsetValue.value + view.scrollStep
            GLFW.GLFW_KEY_PAGE_UP -> scrollOffsetValue.value - view.viewportLength * 0.8
            GLFW.GLFW_KEY_PAGE_DOWN -> scrollOffsetValue.value + view.viewportLength * 0.8
            GLFW.GLFW_KEY_HOME -> evaluateScrollRange().start
            GLFW.GLFW_KEY_END -> evaluateScrollRange().endInclusive
            else -> return false
        }
        return updateScroll(offset)
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

class VanillaXScrollbarViewWidget(
    view: XScrollbarView,
    bounds: IntRectangle,
) : VanillaScrollbarViewWidget(
    view,
    view.scrollOffsetX,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = width
    override val scrollbarThickness get() = height
    override fun evaluateContentLength() = view.contentLength


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x
    override fun getDragAmount(dragX: Double, dragY: Double) = dragX

    // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(
            x + start,
            y,
            x + start + length,
            y + height,
            view.scrollbarColor,
        )
    }

}

class VanillaYScrollbarViewWidget(
    view: YScrollbarView,
    bounds: IntRectangle,
) : VanillaScrollbarViewWidget(
    view,
    view.scrollOffsetY,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = height
    override val scrollbarThickness get() = width
    override fun evaluateContentLength() = view.contentLength


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y
    override fun getDragAmount(dragX: Double, dragY: Double) = dragY
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(
            x,
            y + start,
            x + width,
            y + start + length,
            view.scrollbarColor,
        )
    }

}
