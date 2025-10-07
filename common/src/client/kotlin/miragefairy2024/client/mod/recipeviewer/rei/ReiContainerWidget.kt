package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.gui.widgets.Widget
import miragefairy2024.mod.recipeviewer.view.Remover
import net.minecraft.client.gui.GuiGraphics

// me.shedaniel.rei.impl.client.gui.widget.MergedWidget
class ReiContainerWidget : Widget() {
    private class WidgetSlot(val widget: Widget)

    private val widgetSlots = mutableListOf<WidgetSlot>()
    var widgets = listOf<Widget>()

    fun add(widget: Widget): Remover {
        val widgetSlot = WidgetSlot(widget)
        widgetSlots += widgetSlot
        widgets = widgetSlots.map { it.widget }
        return Remover {
            widgetSlots -= widgetSlot
            widgets = widgetSlots.map { it.widget }
        }
    }

    override fun children() = widgets

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean = widgets.toList().any { it.mouseScrolled(mouseX, mouseY, scrollX, scrollY) }
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = widgets.toList().any { it.keyPressed(keyCode, scanCode, modifiers) }
    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = widgets.toList().any { it.keyReleased(keyCode, scanCode, modifiers) }
    override fun charTyped(codePoint: Char, modifiers: Int): Boolean = widgets.toList().any { it.charTyped(codePoint, modifiers) }
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double) = widgets.toList().any { it.mouseDragged(mouseX, mouseY, button, dragX, dragY) }
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) = widgets.toList().any { it.mouseReleased(mouseX, mouseY, button) }

    override fun containsMouse(mouseX: Double, mouseY: Double) = widgets.any { it.containsMouse(mouseX, mouseY) }
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = widgets.forEach { it.render(guiGraphics, mouseX, mouseY, partialTick) }
    override fun getZRenderingPriority() = widgets.maxOfOrNull { it.zRenderingPriority } ?: 0.0
}

infix fun ReiContainerWidget.place(widget: Widget) = this.add(widget)
