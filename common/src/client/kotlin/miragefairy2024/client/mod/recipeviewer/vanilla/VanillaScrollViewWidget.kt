package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.util.getActualFocused
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.util.Remover
import miragefairy2024.util.text
import miragefairy2024.util.translate
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import java.util.Optional
import kotlin.math.roundToInt

/** 子 Widget の描画・入力・フォーカス・読み上げを中継するスクロール領域なのだ～🌱 */
abstract class VanillaScrollViewWidget(
    protected val view: ScrollView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) :
    AbstractContainerWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty()), MouseCaptureContainer {


    // Widget の登録なのだ～🌱

    private class WidgetRegistration(val widget: AbstractWidget, val interactive: Boolean)

    // mutableSetOf が返す Set の登録順を、描画順と入力順に利用するのだ～🌱
    private val widgetRegistrations = mutableSetOf<WidgetRegistration>()

    private fun register(widget: AbstractWidget, interactive: Boolean): Remover {
        val widgetRegistration = WidgetRegistration(widget, interactive)
        widgetRegistrations += widgetRegistration
        return Remover {

            widgetRegistrations -= widgetRegistration

            // 押下のフラグだけ残して、解除された子への参照は消すのだ～🌱
            pressedWidgetRegistrations.entries.forEach {
                if (it.value == widgetRegistration) it.setValue(null)
            }

            // 入力登録が残っていない子への参照を解除するのだ～🌱
            if (focused === widget || lastNarratable === widget) {
                if (widgetRegistrations.none { it.interactive && it.widget === widget }) {
                    if (focused === widget) focused = null
                    if (lastNarratable === widget) lastNarratable = null
                }
            }

        }
    }

    override fun children() = widgetRegistrations.mapNotNull { if (it.interactive) it.widget else null }


    // ライフサイクルなのだ～🌱

    private var disposed = false

    fun dispose() {
        disposed = true
        draggingScrollbar = false
        isDragging = false
        pressedWidgetRegistrations.clear()
        focused = null
        focusedSelf = false
        childController.dispose()
    }

    fun detach() {
        childController.detach()
    }


    // サイズと位置なのだ～🌱

    protected abstract fun getReservedRight(): Int
    protected abstract fun getReservedBottom(): Int

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

    protected abstract fun getChildOffset(scrollOffset: Int): IntPoint

    private fun updateScroll(offset: Double): Boolean {
        val old = scrollOffset
        scrollOffset = offset.coerceIn(0.0, evaluateMaxScrollOffset().toDouble())
        if (scrollOffset == old) return false
        childController.setOffset(getChildOffset(scrollOffset.roundToInt()))
        return true
    }

    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        childController.setOffset(getChildOffset(scrollOffset.roundToInt()))
    }


    // 描画とマウス入力なのだ～🌱

    private val pressedWidgetRegistrations = mutableMapOf<Int, WidgetRegistration?>()
    private var draggingScrollbar = false
    private var scrollbarDragOffset = 0.0

    private fun containsViewport(mouseX: Double, mouseY: Double): Boolean {
        if (mouseX < x) return false
        if (mouseY < y) return false
        if (mouseX >= x + (width - getReservedRight() atLeast 0)) return false
        if (mouseY >= y + (height - getReservedBottom() atLeast 0)) return false
        return true
    }

    override fun isActive() = super.isActive() && !disposed

    override fun isMouseOver(mouseX: Double, mouseY: Double) = isActive && super.isMouseOver(mouseX, mouseY)

    // 有効である子のみを返すのだ～🌱
    override fun getChildAt(mouseX: Double, mouseY: Double): Optional<GuiEventListener> {
        if (!isActive) return Optional.empty()
        if (!containsViewport(mouseX, mouseY)) return Optional.empty()
        return super.getChildAt(mouseX, mouseY)
    }

    protected abstract fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int)

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {

        // 子要素の描画なのだ～🌱
        guiGraphics.enableScissor(x, y, x + (width - getReservedRight() atLeast 0), y + (height - getReservedBottom() atLeast 0))
        try {
            widgetRegistrations.forEach { widgetRegistration ->
                widgetRegistration.widget.render(guiGraphics, mouseX, mouseY, partialTick)
            }
        } finally {
            guiGraphics.disableScissor()
        }

        // スクロールバーの描画なのだ～🌱
        if (evaluateMaxScrollOffset() > 0 && view.scrollbarThickness > 0 && evaluateThumbLength() > 0) {
            renderScrollbar(guiGraphics, evaluateThumbPosition().roundToInt(), evaluateThumbLength())
        }

    }

    protected abstract fun getMousePosition(mouseX: Double, mouseY: Double): Double

    /** ホイール入力から、このコンテナのスクロール軸に適用する入力値を返すのだ～🌱 */
    protected abstract fun getScrollAmount(scrollX: Double, scrollY: Double): Double

    private fun dragScrollbar(mouseX: Double, mouseY: Double) {
        updateScroll((getMousePosition(mouseX, mouseY) - scrollbarDragOffset) * evaluateMaxScrollOffset() / (viewportLength - evaluateThumbLength()))
    }

    override fun hasMouseCapture(button: Int) = !disposed && (button == 0 && draggingScrollbar || button in pressedWidgetRegistrations)

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        pressedWidgetRegistrations -= button
        if (button == 0) {
            draggingScrollbar = false
            isDragging = false
        }
        if (!isActive) return false
        if (!containsViewport(mouseX, mouseY)) {
            if (!super.isMouseOver(mouseX, mouseY)) return false
            if (button != 0 || evaluateMaxScrollOffset() == 0 || viewportLength <= evaluateThumbLength()) return false
            draggingScrollbar = true
            val position = getMousePosition(mouseX, mouseY)
            scrollbarDragOffset = if (position >= evaluateThumbPosition() && position < evaluateThumbPosition() + evaluateThumbLength()) position - evaluateThumbPosition() else evaluateThumbLength() / 2.0
            dragScrollbar(mouseX, mouseY)
            return true
        }
        return widgetRegistrations.toList().any { widgetRegistration ->
            // 操作可能でない子は除くのだ～🌱
            if (!widgetRegistration.interactive) return@any false
            // 途中で除外された子は除くのだ～🌱
            if (widgetRegistration !in widgetRegistrations) return@any false
            if (widgetRegistration.widget.mouseClicked(mouseX, mouseY, button)) {
                // クリック処理中に解除・破棄される場合があるため、処理後の登録状態を確認するのだ～🌱
                if (!disposed) {
                    if (widgetRegistration in widgetRegistrations) {
                        pressedWidgetRegistrations[button] = widgetRegistration
                        focused = widgetRegistration.widget
                        if (button == 0) isDragging = true
                    } else {
                        pressedWidgetRegistrations[button] = null
                    }
                }
                true
            } else {
                false
            }
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (button == 0 && draggingScrollbar) {
            dragScrollbar(mouseX, mouseY)
            return true
        }
        if (button !in pressedWidgetRegistrations) return false
        pressedWidgetRegistrations[button]?.widget?.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) isDragging = false
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false
            return true
        }
        if (button !in pressedWidgetRegistrations) return false
        pressedWidgetRegistrations.remove(button)?.widget?.mouseReleased(mouseX, mouseY, button)
        return true
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (!isActive) return
        if (!containsViewport(mouseX, mouseY)) {
            // いずれかのボタンでドラッグしている子は、表示領域外でもマウス移動を受け取るのだ～🌱
            widgetRegistrations.forEach { widgetRegistration ->
                if (pressedWidgetRegistrations.values.any { it == widgetRegistration }) {
                    widgetRegistration.widget.mouseMoved(mouseX, mouseY)
                }
            }
            return
        }
        children().forEach {
            it.mouseMoved(mouseX, mouseY)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        // スクロールバー上でも判定を受け付けるため、containsViewport ではなく isMouseOver を使うのだ～🌱
        if (!isMouseOver(mouseX, mouseY)) return false
        val child = getChildAt(mouseX, mouseY).orNull
        if (child != null && child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true
        return updateScroll(scrollOffset - getScrollAmount(scrollX, scrollY) * view.scrollStep)
    }


    // フォーカスとキーボード入力なのだ～🌱

    private var focusedSelf = false

    protected abstract fun getScrollOffsetDeltaOnFocus(rectangle: ScreenRectangle): Int

    override fun isFocused() = focusedSelf

    override fun setFocused(focused: Boolean) {
        focusedSelf = focused
        // 同じコンテナへの再フォーカスでも、選択中の子と押下先は維持するのだ～🌱
        this.focused?.isFocused = focused
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {
        if (!isActive) return null
        // バニラの他コンテナの実装では、InitialFocus 時に最初の要素を選択したりしているから、その代わりなのだ～🌱
        val navigation = if (event is FocusNavigationEvent.InitialFocus) FocusNavigationEvent.TabNavigation(true) else event
        val path = super.nextFocusPath(navigation)
        if (path == null) {
            if (isFocused || evaluateMaxScrollOffset() <= 0) return null
            return object : ComponentPath {
                override fun component() = this@VanillaScrollViewWidget

                override fun applyFocus(focused: Boolean) {
                    if (focused) this@VanillaScrollViewWidget.focused = null
                    this@VanillaScrollViewWidget.isFocused = focused
                }
            }
        }
        return object : ComponentPath {
            override fun component() = path.component()

            override fun applyFocus(focused: Boolean) {
                path.applyFocus(focused)
                // 子のフォーカスが変わったら、適宜それを収めるようにスクロールするのだ～🌱
                if (focused) {
                    val actualFocused = this@VanillaScrollViewWidget.focused?.getActualFocused { true } ?: return
                    val rectangle = actualFocused.rectangle
                    if (rectangle.width() <= 0 || rectangle.height() <= 0) return
                    updateScroll(scrollOffset + getScrollOffsetDeltaOnFocus(rectangle))
                }
            }
        }
    }

    override fun getCurrentFocusPath(): ComponentPath? {
        val path = super.getCurrentFocusPath()
        if (path != null) return path
        return if (isFocused) ComponentPath.leaf(this) else null
    }

    protected abstract val backwardKey: Int
    protected abstract val forwardKey: Int

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isActive) return false
        if (!isFocused) return false
        val listener = focused
        if (listener != null && listener.keyPressed(keyCode, scanCode, modifiers)) return true
        if (listener != null && (keyCode == backwardKey || keyCode == forwardKey)) return false
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

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isActive) return false
        if (!isFocused) return false
        val listener = focused ?: return false
        return listener.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (!isActive) return false
        if (!isFocused) return false
        val listener = focused ?: return false
        return listener.charTyped(codePoint, modifiers)
    }


    // 読み上げなのだ～🌱

    private var lastNarratable: NarratableEntry? = null

    private fun getNarratables() = children().filter { it.isActive }

    override fun narrationPriority(): NarratableEntry.NarrationPriority {
        if (!isActive) return NarratableEntry.NarrationPriority.NONE
        return getNarratables().fold(NarratableEntry.NarrationPriority.NONE) { priority, widget -> priority max widget.narrationPriority() }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        val narratables = getNarratables().sortedBy { it.tabOrderGroup }
        val result = Screen.findNarratableWidget(narratables, lastNarratable) ?: return
        if (result.priority.isTerminal) lastNarratable = result.entry
        if (narratables.size > 1) {
            narrationElementOutput.add(NarratedElementType.POSITION, text { translate("narrator.position.object_list", result.index + 1, narratables.size) })
            if (result.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                narrationElementOutput.add(NarratedElementType.USAGE, text { translate("narration.component_list.usage") })
            }
        }
        result.entry.updateNarration(narrationElementOutput.nest())
    }


    // 子 View の配置なのだ～🌱

    private val childController: ViewController

    init {
        val childContext: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = context.origin
            override fun addRenderableWidget(widget: AbstractWidget) = register(widget, true)
            override fun addRenderableOnly(widget: AbstractWidget) = register(widget, false)
        }
        childController = view.childView.attachTo(IntPoint(x, y)) { childView, childBounds ->
            // TODO スクロール領域内の MenuSlotView の配置に対応するのだ～🌱
            if (childView is MenuSlotView) throw UnsupportedOperationException("MenuSlotView cannot be placed inside a scroll view")
            registry.place(childContext, childView, childBounds)
        }
    }

}

class VanillaXScrollViewWidget(
    view: XScrollView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) : VanillaScrollViewWidget(
    view,
    bounds,
    context,
    registry,
) {

    // 領域とサイズなのだ～🌱

    override fun getReservedRight() = 0
    override fun getReservedBottom() = view.scrollbarThickness

    override val viewportLength get() = width
    override fun evaluateContentLength() = view.childView.evaluateActualSizeX()


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getChildOffset(scrollOffset: Int) = IntPoint(x - scrollOffset, y)
    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x

    // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    // TODO 操作やホバーしていない間はフェードアウト
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(
            x + start,
            y + height - view.scrollbarThickness atLeast y,
            x + start + length,
            y + height,
            view.scrollbarColor,
        )
    }

    override fun getScrollOffsetDeltaOnFocus(rectangle: ScreenRectangle): Int {
        val viewPortX1 = rectangle.left() - x
        if (viewPortX1 < 0) return viewPortX1
        val viewPortX2 = viewPortX1 + rectangle.width()
        if (viewPortX2 > viewportLength) return viewPortX2 - viewportLength min viewPortX1
        return 0
    }

}

class VanillaYScrollViewWidget(
    view: YScrollView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) : VanillaScrollViewWidget(
    view,
    bounds,
    context,
    registry,
) {

    // 領域とサイズなのだ～🌱

    override fun getReservedRight() = view.scrollbarThickness
    override fun getReservedBottom() = 0

    override val viewportLength get() = height
    override fun evaluateContentLength() = view.childView.evaluateActualSizeY()


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getChildOffset(scrollOffset: Int) = IntPoint(x, y - scrollOffset)
    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    // TODO 操作やホバーしていない間はフェードアウト
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(
            x + width - view.scrollbarThickness atLeast x,
            y + start,
            x + width,
            y + start + length,
            view.scrollbarColor,
        )
    }

    override fun getScrollOffsetDeltaOnFocus(rectangle: ScreenRectangle): Int {
        val viewPortY1 = rectangle.top() - y
        if (viewPortY1 < 0) return viewPortY1
        val viewPortY2 = viewPortY1 + rectangle.height()
        if (viewPortY2 > viewportLength) return viewPortY2 - viewportLength min viewPortY1
        return 0
    }

}
