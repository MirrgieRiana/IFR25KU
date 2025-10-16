package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.Widget
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

class EmiContainerWidget : Widget() {

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

    override fun getBounds(): Bounds {
        if (widgets.isEmpty()) return Bounds.EMPTY
        val xMin = widgets.minOfOrNull { it.bounds.x }!!
        val yMin = widgets.minOfOrNull { it.bounds.y }!!
        val xMax = widgets.maxOfOrNull { it.bounds.x + it.bounds.width }!!
        val yMax = widgets.maxOfOrNull { it.bounds.y + it.bounds.height }!!
        return Bounds(xMin, yMin, xMax - xMin, yMax - yMin)
    }

    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
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
        return widgets.toList().any {
            if (it.bounds.contains(mouseX, mouseY)) {
                it.mouseClicked(mouseX, mouseY, button)
            } else {
                false
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return widgets.toList().any {
            it.keyPressed(keyCode, scanCode, modifiers)
        }
    }

}

infix fun EmiContainerWidget.place(widget: Widget) = this.add(widget)
