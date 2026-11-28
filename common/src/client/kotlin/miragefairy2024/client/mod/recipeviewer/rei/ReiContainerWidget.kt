package miragefairy2024.client.mod.recipeviewer.rei

import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientScreenInputEvent
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.gui.widgets.Widget
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import miragefairy2024.util.fire
import miragefairy2024.util.removeOn
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor

// me.shedaniel.rei.impl.client.gui.widget.MergedWidget
class ReiContainerWidget : Widget() {

    companion object {
        private var focusedContainer: ReiContainerWidget? = null

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
        }

        private fun clearFocus() {
            focusedContainer?.setFocused(null)
            focusedContainer = null
        }
    }

    private class WidgetSlot(val widget: Widget)

    private val widgetSlots = mutableListOf<WidgetSlot>()
    var widgets = listOf<Widget>()
        private set

    fun add(widget: Widget): Remover {
        val widgetSlot = WidgetSlot(widget)
        widgetSlots += widgetSlot
        widgets = widgetSlots.map { it.widget }
        return Remover {
            widgetSlots -= widgetSlot
            widgets = widgetSlots.map { it.widget }
            if (focusedContainer === this && focused === widget && widgets.none { it === widget }) clearFocus()
        }
    }

    override fun children() = widgets


    override fun containsMouse(mouseX: Double, mouseY: Double): Boolean {
        return widgets.any {
            it.containsMouse(mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val screen = Minecraft.getInstance().screen
        val clicked = widgetSlots.toList().firstOrNull {
            it.widget.containsMouse(mouseX, mouseY) && it.widget.mouseClicked(mouseX, mouseY, button)
        } ?: return false
        if (Minecraft.getInstance().screen === screen && clicked in widgetSlots) {
            clearFocus()
            setFocused(clicked.widget)
            focusedContainer = this
        }
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        return widgets.toList().any {
            if (it.isDragging || it.containsMouse(mouseX, mouseY)) {
                it.mouseDragged(mouseX, mouseY, button, dragX, dragY)
            } else {
                false
            }
        }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return widgets.toList().any {
            if (it.isDragging || it.containsMouse(mouseX, mouseY)) {
                it.mouseReleased(mouseX, mouseY, button)
            } else {
                false
            }
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return widgets.toList().any {
            if (it.containsMouse(mouseX, mouseY)) {
                it.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
            } else {
                false
            }
        }
    }


    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val focused = focused
        if (focused?.keyPressed(keyCode, scanCode, modifiers) == true) return true
        return widgets.any {
            it is Slot && it !== focused && it.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return focused?.keyReleased(keyCode, scanCode, modifiers) == true
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        return focused?.charTyped(codePoint, modifiers) == true
    }


    override fun render(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        widgets.forEach {
            it.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun getZRenderingPriority(): Double {
        return widgets.maxOfOrNull {
            it.zRenderingPriority
        } ?: 0.0
    }

}

infix fun ReiContainerWidget.place(widget: Widget): ViewController {
    val onDetached = EventRegistry<() -> Unit>()
    this.add(widget).removeOn(onDetached)
    return object : ViewController {
        override fun setOffset(offset: IntPoint) = throw UnsupportedOperationException("REI does not support changing view offsets")
        override fun detach() = onDetached.fire()
    }
}
