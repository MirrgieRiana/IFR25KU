package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.util.getActualFocused
import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.intersect
import miragefairy2024.mod.recipeviewer.views.ScrollBounds
import miragefairy2024.mod.recipeviewer.views.ScrollContainerView
import miragefairy2024.mod.recipeviewer.views.XScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Remover
import miragefairy2024.util.empty
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.observeAndInitialize
import miragefairy2024.util.text
import miragefairy2024.util.translate
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.glfw.GLFW
import java.util.Optional
import kotlin.math.roundToInt

/** 子 Widget の描画・入力・フォーカス・読み上げを中継するスクロール領域なのだ～🌱 */
abstract class VanillaScrollContainerViewWidget(
    protected val view: ScrollContainerView,
    private val scrollOffsetValue: ObservableValue<Double>,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) :
    AbstractContainerWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }), MouseCaptureContainer {


    // Widget の登録なのだ～🌱

    // mutableSetOf が返す Set の登録順を、描画順と入力順に利用するのだ～🌱
    private val widgetRegistrations = mutableSetOf<WidgetRegistration>()

    private fun register(widgetRegistration: WidgetRegistration): Remover {
        val widget = widgetRegistration.widget
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
    private val onClosed = EventRegistry<() -> Unit>()

    fun dispose() {
        disposed = true
        isDragging = false
        pressedWidgetRegistrations.clear()
        focused = null
        focusedSelf = false
        onClosed.fire()
        childController.dispose()
    }

    fun detach() {
        childController.detach()
    }


    // サイズと位置なのだ～🌱

    protected abstract val viewportLength: Int
    protected abstract fun evaluateContentLength(): Int

    private fun evaluateMaxScrollOffset() = evaluateContentLength() - viewportLength atLeast 0

    protected abstract fun getChildOffset(scrollOffset: Int): IntPoint

    private fun updateScroll(offset: Double): Boolean {
        val old = scrollOffsetValue.value
        scrollOffsetValue.value = offset
        return scrollOffsetValue.value != old
    }

    private fun scrollBy(delta: Double): Boolean {
        val scrollOffset = scrollOffsetValue.value
        val maxScrollOffset = evaluateMaxScrollOffset().toDouble()
        // 範囲外から戻る途中も、入力の移動量で少しずつ戻れるようにするのだ～🌱
        return when {
            delta < 0.0 && scrollOffset > 0.0 -> updateScroll(scrollOffset + delta atLeast 0.0)
            delta > 0.0 && scrollOffset < maxScrollOffset -> updateScroll(scrollOffset + delta atMost maxScrollOffset)
            else -> false
        }
    }

    fun updateScrollBounds() {
        if (!disposed) view.scrollBounds.value = ScrollBounds(evaluateContentLength(), viewportLength)
    }

    override fun setWidth(width: Int) {
        super.setWidth(width)
        updateScrollBounds()
    }

    override fun setHeight(height: Int) {
        super.setHeight(height)
        updateScrollBounds()
    }

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        updateScrollBounds()
    }

    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        childController.setOffset(getChildOffset(scrollOffsetValue.value.roundToInt()))
    }


    // 描画とマウス入力なのだ～🌱

    private val pressedWidgetRegistrations = mutableMapOf<Int, WidgetRegistration?>()

    private fun containsViewport(mouseX: Double, mouseY: Double): Boolean {
        if (mouseX < x) return false
        if (mouseY < y) return false
        if (mouseX >= x + width) return false
        if (mouseY >= y + height) return false
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

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {

        // 子要素の描画なのだ～🌱
        val xMin = x
        val xMax = xMin + width
        val yMin = y
        val yMax = yMin + height
        guiGraphics.enableScissor(xMin, yMin, xMax, yMax)
        try {
            widgetRegistrations.forEach skip@{ widgetRegistration ->
                val widget = widgetRegistration.widget

                run force@{

                    // 前回のホバー状態を解除するために、ホバーフラグが立っている要素は描画を呼び出すのだ～🌱
                    if (widget.isHovered) return@force
                    // キーボードフォーカスによって生じるツールチップを予約するために、フォーカス中の要素は描画を呼び出すのだ～🌱
                    if (focused === widget) return@force
                    // ドラッグに反応する要素のために、ドラッグ中の要素は描画を呼び出すのだ～🌱
                    if (widgetRegistration in pressedWidgetRegistrations.values) return@force

                    // 親要素自体が潰れているのだ～🌱
                    if (xMin == xMax || yMin == yMax) return@skip

                    val overflow = widgetRegistration.renderOverflow

                    // 子要素が潰れているのだ～🌱
                    if (widget.width == 0 && overflow.xMin == 0 && overflow.xMax == 0) return@skip
                    if (widget.height == 0 && overflow.yMin == 0 && overflow.yMax == 0) return@skip

                    // はみ出しが無限でなく、その方向に見切れている場合は、描画を飛ばすのだ～🌱
                    if (overflow.xMin != Int.MAX_VALUE && widget.x - overflow.xMin >= xMax) return@skip
                    if (overflow.yMin != Int.MAX_VALUE && widget.y - overflow.yMin >= yMax) return@skip
                    if (overflow.xMax != Int.MAX_VALUE && widget.x + widget.width + overflow.xMax <= xMin) return@skip
                    if (overflow.yMax != Int.MAX_VALUE && widget.y + widget.height + overflow.yMax <= yMin) return@skip

                }

                widget.render(guiGraphics, mouseX, mouseY, partialTick)
            }
        } finally {
            guiGraphics.disableScissor()
        }

    }

    /** ホイール入力から、このコンテナのスクロール軸に適用する入力値を返すのだ～🌱 */
    protected abstract fun getScrollAmount(scrollX: Double, scrollY: Double): Double

    override fun hasMouseCapture(button: Int) = !disposed && button in pressedWidgetRegistrations

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        pressedWidgetRegistrations -= button
        if (button == 0) isDragging = false
        if (!isActive) return false

        if (!containsViewport(mouseX, mouseY)) return false

        // 子要素のクリック処理なのだ～🌱
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
        if (button !in pressedWidgetRegistrations) return false
        pressedWidgetRegistrations[button]?.widget?.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) isDragging = false
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
        if (!isMouseOver(mouseX, mouseY)) return false
        if (containsViewport(mouseX, mouseY)) {
            val handled = widgetRegistrations.toList().any { widgetRegistration ->
                if (!widgetRegistration.interactive) return@any false
                if (widgetRegistration !in widgetRegistrations) return@any false
                val widget = widgetRegistration.widget
                widget.isMouseOver(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
            }
            if (handled) return true
        }
        val amount = getScrollAmount(scrollX, scrollY)
        if (amount == 0.0) return false
        val handled = scrollBy(-amount * view.scrollStep)
        view.onScrollInput.fire()
        return handled
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

        // 無効なコンテナなら null を返すのだ～🌱
        if (!isActive) return null

        // バニラの他コンテナの実装では、InitialFocus 時に最初の要素を選択したりしているから、その代わりなのだ～🌱
        val actualEvent = if (event is FocusNavigationEvent.InitialFocus) FocusNavigationEvent.TabNavigation(true) else event

        // super.nextFocusPath() に子の探索を任せるのだ～🌱
        val path = super.nextFocusPath(actualEvent)

        // 子への経路があればそれを、なければ自身をフォーカス先にするのだ～🌱
        if (path != null) {
            // 子を次のフォーカス先にするのだ～🌱
            return object : ComponentPath {
                override fun component() = path.component()

                override fun applyFocus(focused: Boolean) {
                    path.applyFocus(focused)
                    // 子のフォーカスが変わったら、適宜それを収めるようにスクロールするのだ～🌱
                    if (focused) {
                        val actualFocused = this@VanillaScrollContainerViewWidget.focused?.getActualFocused { true } ?: return
                        val rectangle = actualFocused.rectangle
                        if (rectangle.width() <= 0 || rectangle.height() <= 0) return
                        updateScroll(scrollOffsetValue.value + getScrollOffsetDeltaOnFocus(rectangle))
                    }
                }
            }
        } else {
            // スクロールの余地がなく、かつ現在位置が初期位置の場合は、移動の操作が不可能なので、自身をフォーカスしないのだ～🌱
            if (evaluateMaxScrollOffset() <= 0 && scrollOffsetValue.value == 0.0) return null
            // 既にフォーカスされている場合は、次のフォーカス先に選ばないのだ～🌱
            if (isFocused) return null
            // 自身を次のフォーカス先にするのだ～🌱
            return object : ComponentPath {
                override fun component() = this@VanillaScrollContainerViewWidget

                override fun applyFocus(focused: Boolean) {
                    // その経路の適用時に、以前の子の参照を解除するのだ～🌱
                    if (focused) this@VanillaScrollContainerViewWidget.focused = null
                    this@VanillaScrollContainerViewWidget.isFocused = focused
                }
            }
        }
    }

    override fun getCurrentFocusPath(): ComponentPath? {
        // 子へフォーカスされていれば、super が作る「コンテナ → 子 → …」の経路を返すのだ～🌱
        val path = super.getCurrentFocusPath()
        if (path != null) return path
        // 子への経路がなく、コンテナ自身がフォーカスされていれば、自身だけの経路を返すのだ～🌱
        if (isFocused) return ComponentPath.leaf(this)
        // どちらでもなければ null なのだ～🌱
        return null
    }

    protected abstract val backwardKey: Int
    protected abstract val forwardKey: Int

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isActive) return false
        if (!isFocused) return false
        val listener = focused
        if (listener != null && listener.keyPressed(keyCode, scanCode, modifiers)) return true
        // 子が選択されている間は、矢印キーをスクロールで消費せず、Screen のフォーカス移動へ渡すのだ～🌱
        if (listener != null && (keyCode == backwardKey || keyCode == forwardKey)) return false
        val handled = when (keyCode) {
            backwardKey -> scrollBy(-view.scrollStep.toDouble())
            forwardKey -> scrollBy(view.scrollStep.toDouble())
            GLFW.GLFW_KEY_PAGE_UP -> scrollBy(-viewportLength * 0.8)
            GLFW.GLFW_KEY_PAGE_DOWN -> scrollBy(viewportLength * 0.8)
            GLFW.GLFW_KEY_HOME -> updateScroll(0.0)
            GLFW.GLFW_KEY_END -> updateScroll(evaluateMaxScrollOffset().toDouble())
            else -> return false
        }
        view.onScrollInput.fire()
        return handled
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
        if (isFocused && focused == null) return NarratableEntry.NarrationPriority.FOCUSED
        return getNarratables().fold(NarratableEntry.NarrationPriority.NONE) { priority, widget -> priority max widget.narrationPriority() }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        if (isFocused && focused == null) {
            narrationElementOutput.add(NarratedElementType.TITLE, text { SCROLL_AREA_TRANSLATION() })
            return
        }
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
            override val canPlaceMenuSlots = context.canPlaceMenuSlots
            override val onBeforeRender = context.onBeforeRender
            override fun addRenderable(registration: WidgetRegistration) = register(registration)
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover {
                return context.registerMenuSlotPlacement(slotId) {
                    val viewportBounds = IntRectangle(x, y, width, height)
                    val childClipBounds = clipBounds()
                    if (childClipBounds == null) viewportBounds else viewportBounds.intersect(childClipBounds)
                }
            }
        }
        updateScrollBounds()
        val initialScrollOffset = scrollOffsetValue.value.roundToInt()
        childController = view.childView.attachTo(getChildOffset(initialScrollOffset)) { childView, childBounds ->
            registry.place(childContext, childView, childBounds)
        }
        scrollOffsetValue.observeAndInitialize(onClosed) { old, new ->
            if (disposed) return@observeAndInitialize
            val scrollOffset = scrollOffsetValue.value.roundToInt()
            // attachTo 内で scrollOffsetValue が書き替えられた場合にも発火するのだ～🌱
            if (scrollOffset == initialScrollOffset && old == new) return@observeAndInitialize
            childController.setOffset(getChildOffset(scrollOffset))
        }
    }

}

class VanillaXScrollContainerViewWidget(
    view: XScrollContainerView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) : VanillaScrollContainerViewWidget(
    view,
    view.scrollOffsetX,
    bounds,
    context,
    registry,
) {

    // 領域とサイズなのだ～🌱

    override val viewportLength get() = width
    override fun evaluateContentLength() = view.childView.evaluateActualSizeX()


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getChildOffset(scrollOffset: Int) = IntPoint(x - scrollOffset, y)

    // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    override fun getScrollOffsetDeltaOnFocus(rectangle: ScreenRectangle): Int {
        val viewPortX1 = rectangle.left() - x
        if (viewPortX1 < 0) return viewPortX1
        val viewPortX2 = viewPortX1 + rectangle.width()
        if (viewPortX2 > viewportLength) return viewPortX2 - viewportLength min viewPortX1
        return 0
    }

}

class VanillaYScrollContainerViewWidget(
    view: YScrollContainerView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) : VanillaScrollContainerViewWidget(
    view,
    view.scrollOffsetY,
    bounds,
    context,
    registry,
) {

    // 領域とサイズなのだ～🌱

    override val viewportLength get() = height
    override fun evaluateContentLength() = view.childView.evaluateActualSizeY()


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getChildOffset(scrollOffset: Int) = IntPoint(x, y - scrollOffset)
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    override fun getScrollOffsetDeltaOnFocus(rectangle: ScreenRectangle): Int {
        val viewPortY1 = rectangle.top() - y
        if (viewPortY1 < 0) return viewPortY1
        val viewPortY2 = viewPortY1 + rectangle.height()
        if (viewPortY2 > viewportLength) return viewPortY2 - viewportLength min viewPortY1
        return 0
    }

}
