package miragefairy2024.client.mod.recipeviewer.emi

import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import dev.emi.emi.api.EmiApi
import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.api.widget.Widget
import dev.emi.emi.screen.EmiScreenManager
import dev.emi.emi.screen.RecipeScreen
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import miragefairy2024.util.fire
import miragefairy2024.util.removeOn
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

class EmiContainerWidget : Widget() {

    companion object {
        private var focusedContainer: EmiContainerWidget? = null

        fun initKeyboardFocus() {
            ClientGuiEvent.SET_SCREEN.register {
                clearFocus()
                CompoundEventResult.pass()
            }
            ClientGuiEvent.INIT_PRE.register { _, _ ->
                clearFocus()
                EventResult.pass()
            }
            ClientScreenInputEvent.MOUSE_CLICKED_PRE.register { _, _, _, _, _ ->
                clearFocus()
                EventResult.pass()
            }
            ClientScreenInputEvent.KEY_PRESSED_PRE.register { _, screen, keyCode, scanCode, modifiers ->
                val container = focusedContainer
                // EMI はカーソルの下にある Widget にしかキー入力を送らないのだ～🌱
                if (container != null && screen is RecipeScreen && screen.getGroup(container) != null && !EmiApi.isSearchFocused() &&
                    container.focusedSlot?.widget?.keyPressed(keyCode, scanCode, modifiers) == true
                ) {
                    EventResult.interruptFalse()
                } else {
                    EventResult.pass()
                }
            }
        }

        private fun clearFocus() {
            focusedContainer?.focusedSlot = null
            focusedContainer = null
        }
    }

    private class WidgetSlot(val widget: Widget)

    private val widgetSlots = mutableListOf<WidgetSlot>()
    private var focusedSlot: WidgetSlot? = null
    var widgets = listOf<Widget>()
        private set

    fun add(widget: Widget): Remover {
        val widgetSlot = WidgetSlot(widget)
        widgetSlots += widgetSlot
        widgets = widgetSlots.map { it.widget }
        return Remover {
            widgetSlots -= widgetSlot
            widgets = widgetSlots.map { it.widget }
            if (focusedSlot === widgetSlot) clearFocus()
        }
    }

    override fun getBounds(): Bounds {
        if (widgets.isEmpty()) return Bounds.EMPTY
        val xMin = widgets.minOfOrNull { it.bounds.x }!!
        val yMin = widgets.minOfOrNull { it.bounds.y }!!
        val xMax = widgets.maxOfOrNull { it.bounds.x + it.bounds.width }!!
        val yMax = widgets.maxOfOrNull { it.bounds.y + it.bounds.height }!!
        return Bounds(xMin, yMin, xMax - xMin, yMax - yMin)
    }

    override fun render(draw: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        widgets.forEach {
            it.render(draw, mouseX, mouseY, delta)
        }
    }

    override fun getTooltip(mouseX: Int, mouseY: Int): List<ClientTooltipComponent?> {
        return widgets.flatMap {
            if (it.bounds.contains(mouseX, mouseY)) {
                it.getTooltip(mouseX, mouseY)
            } else {
                listOf()
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int): Boolean {
        val screen = Minecraft.getInstance().screen
        val clicked = widgetSlots.toList().firstOrNull {
            it.widget.bounds.contains(mouseX, mouseY) && it.widget.mouseClicked(mouseX, mouseY, button)
        } ?: return false
        val widget = clicked.widget
        if (widget is EmiViewRendererWidget<*, *> && widget.isFocusable() && Minecraft.getInstance().screen === screen && clicked in widgetSlots) {
            clearFocus()
            focusedSlot = clicked
            focusedContainer = this
        }
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val screen = Minecraft.getInstance().screen as? RecipeScreen ?: return false
        val group = screen.getGroup(this) ?: return false
        val mouseX = EmiScreenManager.lastMouseX - group.x()
        val mouseY = EmiScreenManager.lastMouseY - group.y()
        return widgets.any {
            it is SlotWidget && it.bounds.contains(mouseX, mouseY) && it.keyPressed(keyCode, scanCode, modifiers)
        }
    }

}

infix fun EmiContainerWidget.place(widget: Widget): ViewController {
    val onDetached = EventRegistry<() -> Unit>()
    this.add(widget).removeOn(onDetached)
    return object : ViewController {
        override fun setOffset(offset: IntPoint) = throw UnsupportedOperationException("EMI does not support changing view offsets")
        override fun detach() = onDetached.fire()
    }
}
