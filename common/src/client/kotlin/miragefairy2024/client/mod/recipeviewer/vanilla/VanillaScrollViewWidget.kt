package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.util.Remover
import mirrg.kotlin.helium.atLeast
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

abstract class VanillaScrollViewWidget(protected val view: ScrollView, bounds: IntRectangle, viewport: IntRectangle, private val contentLength: Int, context: VanillaViewPlacerContext, registry: ViewPlacerRegistry<VanillaViewPlacerContext>) : AbstractContainerWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty()) {

    private val widgets = mutableListOf<AbstractWidget>()
    private val interactiveWidgets = mutableListOf<AbstractWidget>()
    protected var viewport = viewport
        private set
    protected abstract val viewportLength: Int
    private val maxScroll get() = contentLength - viewportLength atLeast 0
    private var scrollOffset = 0.0
    private val thumbLength get() = if (contentLength <= 0) viewportLength else (viewportLength.toDouble() * viewportLength / contentLength).roundToInt().coerceIn(minOf(view.scrollbarThickness atLeast 1, viewportLength), viewportLength)
    private val thumbPosition get() = if (maxScroll == 0) 0.0 else scrollOffset * (viewportLength - thumbLength) / maxScroll
    private var draggingScrollbar = false
    private val pressedButtons = mutableSetOf<Int>()
    private var dragOffset = 0.0
    private var focused2 = false
    private var lastNarratable: NarratableEntry? = null

    private val childController = view.childView.attachTo(IntPoint(x, y)) { child, childBounds ->
        registry.place(object : VanillaViewPlacerContext {
            override val origin = context.origin

            fun register(widget: AbstractWidget, interactive: Boolean): Remover {
                widgets += widget
                if (interactive) interactiveWidgets += widget
                return Remover {
                    widgets.removeAll { it === widget }
                    interactiveWidgets.removeAll { it === widget }
                    if (focused === widget) {
                        focused = null
                        isDragging = false
                        pressedButtons.clear()
                    }
                }
            }

            override fun addRenderableWidget(widget: AbstractWidget) = register(widget, true)
            override fun addRenderableOnly(widget: AbstractWidget) = register(widget, false)
        }, child, childBounds)
    }

    protected abstract fun getChildOffset(pixels: Int): IntPoint

    private fun updateScroll(offset: Double) {
        scrollOffset = if (offset.isNaN()) 0.0 else offset.coerceIn(0.0, maxScroll.toDouble())
        val pixels = scrollOffset.roundToInt()
        childController.setOffset(getChildOffset(pixels))
    }

    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        viewport = IntRectangle(x, y, viewport.sizeX, viewport.sizeY)
        updateScroll(scrollOffset)
    }

    fun dispose() {
        draggingScrollbar = false
        isDragging = false
        pressedButtons.clear()
        childController.dispose()
    }

    fun detach() = childController.detach()

    override fun children() = interactiveWidgets

    private fun isInViewport(mouseX: Double, mouseY: Double) = mouseX >= viewport.x && mouseX < viewport.x + viewport.sizeX && mouseY >= viewport.y && mouseY < viewport.y + viewport.sizeY

    private fun intersectsViewport(widget: AbstractWidget) = widget.x < viewport.x + viewport.sizeX && widget.x + widget.width > viewport.x && widget.y < viewport.y + viewport.sizeY && widget.y + widget.height > viewport.y

    protected abstract fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int)

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.enableScissor(viewport.x, viewport.y, viewport.x + viewport.sizeX, viewport.y + viewport.sizeY)
        try {
            val inside = isInViewport(mouseX.toDouble(), mouseY.toDouble())
            widgets.toList().forEach {
                if (intersectsViewport(it)) it.render(guiGraphics, if (inside) mouseX else Int.MIN_VALUE, if (inside) mouseY else Int.MIN_VALUE, partialTick)
            }
        } finally {
            guiGraphics.disableScissor()
        }
        if (maxScroll > 0 && view.scrollbarThickness > 0) {
            val start = thumbPosition.roundToInt()
            renderScrollbar(guiGraphics, start, thumbLength)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!active || !visible || !isMouseOver(mouseX, mouseY)) return false
        pressedButtons -= button
        if (button == 0) {
            draggingScrollbar = false
            isDragging = false
        }
        if (!isInViewport(mouseX, mouseY)) {
            if (button != 0 || maxScroll == 0 || viewportLength <= thumbLength) return false
            focused = null
            draggingScrollbar = true
            val position = getMousePosition(mouseX, mouseY)
            dragOffset = if (position >= thumbPosition && position < thumbPosition + thumbLength) position - thumbPosition else thumbLength / 2.0
            dragScrollbar(mouseX, mouseY)
            return true
        }
        return interactiveWidgets.toList().any { widget ->
            if (interactiveWidgets.none { it === widget } || !widget.mouseClicked(mouseX, mouseY, button)) false else {
                pressedButtons += button
                if (interactiveWidgets.any { it === widget }) {
                    focused = widget
                    if (button == 0) isDragging = true
                }
                true
            }
        }
    }

    protected abstract fun getMousePosition(mouseX: Double, mouseY: Double): Double

    private fun dragScrollbar(mouseX: Double, mouseY: Double) {
        val position = getMousePosition(mouseX, mouseY)
        updateScroll((position - dragOffset) * maxScroll / (viewportLength - thumbLength))
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (button == 0 && draggingScrollbar) {
            dragScrollbar(mouseX, mouseY)
            return true
        }
        if (button !in pressedButtons) return false
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false
            return true
        }
        if (!pressedButtons.remove(button)) return false
        if (button == 0) isDragging = false
        focused?.mouseReleased(mouseX, mouseY, button)
        return true
    }

    protected abstract fun getScrollAmount(scrollX: Double, scrollY: Double): Double

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!active || !visible || !isMouseOver(mouseX, mouseY)) return false
        if (isInViewport(mouseX, mouseY) && interactiveWidgets.toList().any { it.isMouseOver(mouseX, mouseY) && it.mouseScrolled(mouseX, mouseY, scrollX, scrollY) }) return true
        val old = scrollOffset
        val amount = getScrollAmount(scrollX, scrollY)
        updateScroll(old - amount * view.scrollStep)
        return scrollOffset != old
    }

    override fun isFocused() = focused2

    override fun setFocused(focused: Boolean) {
        focused2 = focused
        // 同じ子への再フォーカスでも false → true と呼ばれるため、子の選択とドラッグ状態は維持するのだ～🌱
        this.focused?.setFocused(focused)
    }

    protected abstract fun getFocusDelta(widget: AbstractWidget): Int

    override fun setFocused(focused: GuiEventListener?) {
        super.setFocused(focused)
        if (focused is AbstractWidget) {
            updateScroll(scrollOffset + getFocusDelta(focused))
        }
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {
        if (!active || !visible) return null
        return super.nextFocusPath(event) ?: if (!isFocused && maxScroll > 0) ComponentPath.leaf(this) else null
    }

    override fun getCurrentFocusPath(): ComponentPath? = super.getCurrentFocusPath() ?: if (isFocused) ComponentPath.leaf(this) else null

    protected abstract val backwardKey: Int
    protected abstract val forwardKey: Int

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!active || !visible || !isFocused) return false
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true
        val focused = focused
        if (focused is EditBox && keyCode != GLFW.GLFW_KEY_ESCAPE && focused.canConsumeInput()) return true
        val old = scrollOffset
        val next = when (keyCode) {
            backwardKey -> old - view.scrollStep
            forwardKey -> old + view.scrollStep
            GLFW.GLFW_KEY_PAGE_UP -> old - viewportLength * 0.8
            GLFW.GLFW_KEY_PAGE_DOWN -> old + viewportLength * 0.8
            GLFW.GLFW_KEY_HOME -> 0.0
            GLFW.GLFW_KEY_END -> maxScroll.toDouble()
            else -> return false
        }
        updateScroll(next)
        return scrollOffset != old
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        val narratables = interactiveWidgets.filter { it.isActive && intersectsViewport(it) }.sortedBy { it.tabOrderGroup }
        val result = Screen.findNarratableWidget(narratables, lastNarratable)
        if (result != null) {
            if (result.priority.isTerminal) lastNarratable = result.entry
            result.entry.updateNarration(narrationElementOutput.nest())
        }
    }

}

class VanillaXScrollViewWidget(view: XScrollView, bounds: IntRectangle, context: VanillaViewPlacerContext, registry: ViewPlacerRegistry<VanillaViewPlacerContext>) : VanillaScrollViewWidget(
    view,
    bounds,
    IntRectangle(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY - view.scrollbarThickness atLeast 0),
    view.childView.evaluateActualSizeX(),
    context,
    registry,
) {

    override val viewportLength get() = viewport.sizeX

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getChildOffset(pixels: Int) = IntPoint(x - pixels, y)

    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(x + start, viewport.y + viewport.sizeY, x + start + length, y + height, view.scrollbarColor)
    }

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x

    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    override fun getFocusDelta(widget: AbstractWidget): Int {
        val start = widget.x - x
        val length = widget.width
        return if (start < 0) start else if (start + length > viewportLength) minOf(start, start + length - viewportLength) else 0
    }

}

class VanillaYScrollViewWidget(view: YScrollView, bounds: IntRectangle, context: VanillaViewPlacerContext, registry: ViewPlacerRegistry<VanillaViewPlacerContext>) : VanillaScrollViewWidget(
    view,
    bounds,
    IntRectangle(bounds.x, bounds.y, bounds.sizeX - view.scrollbarThickness atLeast 0, bounds.sizeY),
    view.childView.evaluateActualSizeY(),
    context,
    registry,
) {

    override val viewportLength get() = viewport.sizeY

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getChildOffset(pixels: Int) = IntPoint(x, y - pixels)

    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        guiGraphics.fill(viewport.x + viewport.sizeX, y + start, x + width, y + start + length, view.scrollbarColor)
    }

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y

    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    override fun getFocusDelta(widget: AbstractWidget): Int {
        val start = widget.y - y
        val length = widget.height
        return if (start < 0) start else if (start + length > viewportLength) minOf(start, start + length - viewportLength) else 0
    }

}
