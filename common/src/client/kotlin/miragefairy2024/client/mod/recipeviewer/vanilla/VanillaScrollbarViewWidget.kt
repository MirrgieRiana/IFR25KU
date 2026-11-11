package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
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

    protected abstract val viewportLength: Int
    protected abstract fun evaluateContentLength(): Int

    private var scrollOffset = 0.0

    private fun evaluateMaxScrollOffset() = evaluateContentLength() - viewportLength atLeast 0

    private fun evaluateThumbLength(): Int {
        if (viewportLength <= 0) return 0
        val contentLength = evaluateContentLength()
        if (contentLength <= viewportLength) return viewportLength
        val visibleRatio = viewportLength.toDouble() / contentLength.toDouble()
        val rawThumbLength = (viewportLength.toDouble() * visibleRatio).roundToInt()
        val minThumbLength = view.scrollbarThickness.coerceIn(1, viewportLength)
        return rawThumbLength.coerceIn(minThumbLength, viewportLength)
    }

    private fun evaluateThumbPosition(): Double {
        return if (evaluateMaxScrollOffset() == 0) {
            0.0
        } else {
            val maxThumbPosition = viewportLength - evaluateThumbLength()
            val scrollRatio = scrollOffset / evaluateMaxScrollOffset().toDouble()
            maxThumbPosition.toDouble() * scrollRatio
        }
    }

    private fun updateScroll(offset: Double): Boolean {
        val old = scrollOffset
        scrollOffset = offset.coerceIn(0.0, evaluateMaxScrollOffset().toDouble())
        if (scrollOffset == old) return false
        return true
    }


    // 描画とマウス入力なのだ～🌱

    private var draggingScrollbar = false
    private var scrollbarDragOffset = 0.0

    override fun isActive() = super.isActive() && !disposed

    override fun isMouseOver(mouseX: Double, mouseY: Double) = isActive && super.isMouseOver(mouseX, mouseY)

    protected abstract fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int)

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {

        updateScroll(scrollOffset)

        // スクロールバーの描画なのだ～🌱
        if (evaluateMaxScrollOffset() > 0 && view.scrollbarThickness > 0 && evaluateThumbLength() > 0) {
            renderScrollbar(guiGraphics, evaluateThumbPosition().roundToInt(), evaluateThumbLength())
        }

    }

    protected abstract fun getMousePosition(mouseX: Double, mouseY: Double): Double

    protected abstract fun getScrollAmount(scrollX: Double, scrollY: Double): Double

    private fun dragScrollbar(mouseX: Double, mouseY: Double) {
        val thumbPosition = getMousePosition(mouseX, mouseY) - scrollbarDragOffset
        val remainingThumbLength = viewportLength - evaluateThumbLength()
        val scrollRatio = thumbPosition / remainingThumbLength.toDouble()
        updateScroll(evaluateMaxScrollOffset() * scrollRatio)
    }

    override fun hasMouseCapture(button: Int) = !disposed && button == 0 && draggingScrollbar

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) draggingScrollbar = false
        if (!isActive) return false

        if (super.isMouseOver(mouseX, mouseY)) {
            // スクロールバーのドラッグ開始なのだ～🌱
            if (button != 0) return false
            if (view.scrollbarThickness == 0) return false
            if (evaluateMaxScrollOffset() == 0) return false
            if (viewportLength <= evaluateThumbLength()) return false
            draggingScrollbar = true
            val mousePosition = getMousePosition(mouseX, mouseY)
            scrollbarDragOffset = if (mousePosition >= evaluateThumbPosition() && mousePosition < evaluateThumbPosition() + evaluateThumbLength()) {
                mousePosition - evaluateThumbPosition()
            } else {
                evaluateThumbLength() / 2.0
            }
            dragScrollbar(mouseX, mouseY)
            return true
        } else {
            return false
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (button == 0 && draggingScrollbar) {
            if (viewportLength > evaluateThumbLength()) dragScrollbar(mouseX, mouseY)
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
        return updateScroll(scrollOffset - getScrollAmount(scrollX, scrollY) * view.scrollStep)
    }


    // フォーカスとキーボード入力なのだ～🌱

    private var focusedSelf = false

    override fun isFocused() = focusedSelf

    override fun setFocused(focused: Boolean) {
        focusedSelf = focused
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {

        if (!isActive) return null

        if (evaluateMaxScrollOffset() > 0 && !isFocused) {
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
            backwardKey -> scrollOffset - view.scrollStep
            forwardKey -> scrollOffset + view.scrollStep
            GLFW.GLFW_KEY_PAGE_UP -> scrollOffset - viewportLength * 0.8
            GLFW.GLFW_KEY_PAGE_DOWN -> scrollOffset + viewportLength * 0.8
            GLFW.GLFW_KEY_HOME -> 0.0
            GLFW.GLFW_KEY_END -> evaluateMaxScrollOffset().toDouble()
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
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val viewportLength get() = width
    override fun evaluateContentLength() = view.contentLength


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x

    // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(
            x + start,
            y + height - view.scrollbarThickness atLeast y,
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
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val viewportLength get() = height
    override fun evaluateContentLength() = view.contentLength


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(
            x + width - view.scrollbarThickness atLeast x,
            y + start,
            x + width,
            y + start + length,
            view.scrollbarColor,
        )
    }

}
