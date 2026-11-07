package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

/** 自身の範囲と現在値だけを使用し、スクロール対象には依存しないバーなのだ～🌱 */
abstract class VanillaScrollbarViewWidget(protected val view: ScrollbarView, bounds: IntRectangle) : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { SCROLL_AREA_TRANSLATION() }), MouseCaptureContainer {
    private var disposed = false
    private var draggingScrollbar = false
    private var scrollbarDragOffset = 0.0

    fun dispose() {
        disposed = true
        draggingScrollbar = false
        isFocused = false
    }

    protected abstract val trackLength: Int
    protected abstract val crossLength: Int
    protected abstract fun getMousePosition(mouseX: Double, mouseY: Double): Double
    protected abstract fun getScrollbarBounds(start: Int, length: Int): IntRectangle
    protected abstract fun getScrollAmount(scrollX: Double, scrollY: Double): Double
    protected abstract val backwardKey: Int
    protected abstract val forwardKey: Int

    protected val thickness get() = view.scrollbarThickness.coerceAtMost(crossLength.coerceAtLeast(0))

    private fun evaluateThumbLength(): Int {
        if (trackLength <= 0) return 0
        val state = view.scrollState
        if (state.contentLength <= state.viewportLength) return trackLength
        val rawLength = (trackLength.toDouble() * state.viewportLength / state.contentLength).roundToInt()
        return rawLength.coerceIn(view.scrollbarThickness.coerceIn(1, trackLength), trackLength)
    }

    private fun evaluateThumbPosition(): Double {
        val state = view.scrollState
        if (state.maxValue == 0.0) return 0.0
        return (trackLength - evaluateThumbLength()) * state.value / state.maxValue
    }

    override fun isActive() = super.isActive() && !disposed && view.scrollState.maxValue > 0.0 && thickness > 0 && trackLength > 0

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        if (!isActive) return false
        val bounds = getScrollbarBounds(0, trackLength)
        return mouseX >= bounds.x && mouseX < bounds.x2 && mouseY >= bounds.y && mouseY < bounds.y2
    }

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (!isActive) return
        val bounds = getScrollbarBounds(evaluateThumbPosition().roundToInt(), evaluateThumbLength())
        guiGraphics.fill(bounds.x, bounds.y, bounds.x2, bounds.y2, view.scrollbarColor)
    }

    private fun dragScrollbar(mouseX: Double, mouseY: Double) {
        val remainingLength = trackLength - evaluateThumbLength()
        if (remainingLength <= 0) return
        val position = getMousePosition(mouseX, mouseY) - scrollbarDragOffset
        view.scrollState.value = view.scrollState.maxValue * position / remainingLength
    }

    override fun hasMouseCapture(button: Int) = !disposed && button == 0 && draggingScrollbar

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button != 0) return false
        draggingScrollbar = false
        if (!isMouseOver(mouseX, mouseY)) return false
        val length = evaluateThumbLength()
        if (trackLength <= length) return false
        val position = getMousePosition(mouseX, mouseY)
        val start = evaluateThumbPosition()
        scrollbarDragOffset = if (position >= start && position < start + length) position - start else length / 2.0
        draggingScrollbar = true
        dragScrollbar(mouseX, mouseY)
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!hasMouseCapture(button)) return false
        dragScrollbar(mouseX, mouseY)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!hasMouseCapture(button)) return false
        draggingScrollbar = false
        return true
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!isMouseOver(mouseX, mouseY)) return false
        return updateScroll(view.scrollState.value - getScrollAmount(scrollX, scrollY) * view.scrollStep)
    }

    private fun updateScroll(value: Double): Boolean {
        val old = view.scrollState.value
        view.scrollState.value = value
        return view.scrollState.value != old
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isActive || !isFocused) return false
        val state = view.scrollState
        val value = when (keyCode) {
            backwardKey -> state.value - view.scrollStep
            forwardKey -> state.value + view.scrollStep
            GLFW.GLFW_KEY_PAGE_UP -> state.value - state.viewportLength * 0.8
            GLFW.GLFW_KEY_PAGE_DOWN -> state.value + state.viewportLength * 0.8
            GLFW.GLFW_KEY_HOME -> 0.0
            GLFW.GLFW_KEY_END -> state.maxValue
            else -> return false
        }
        return updateScroll(value)
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? = if (isActive) super.nextFocusPath(event) else null

    override fun narrationPriority(): NarratableEntry.NarrationPriority = if (isActive) super.narrationPriority() else NarratableEntry.NarrationPriority.NONE

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = narrationElementOutput.add(NarratedElementType.TITLE, message)
}

class VanillaXScrollbarViewWidget(view: XScrollbarView, bounds: IntRectangle) : VanillaScrollbarViewWidget(view, bounds) {
    override val trackLength get() = width
    override val crossLength get() = height
    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT
    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x
    override fun getScrollbarBounds(start: Int, length: Int) = IntRectangle(x + start, y + height - thickness, length, thickness)
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY
}

class VanillaYScrollbarViewWidget(view: YScrollbarView, bounds: IntRectangle) : VanillaScrollbarViewWidget(view, bounds) {
    override val trackLength get() = height
    override val crossLength get() = width
    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN
    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y
    override fun getScrollbarBounds(start: Int, length: Int) = IntRectangle(x + width - thickness, y + start, thickness, length)
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY
}
