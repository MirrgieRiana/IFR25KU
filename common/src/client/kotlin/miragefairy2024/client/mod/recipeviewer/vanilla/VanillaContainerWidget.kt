package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

/**
 * 子のウィジェットを自前で保持して、描画と入力を分配するウィジェットなのだ～🌱
 * 画面へはこれ 1 個だけを登録するのだ～🌱
 * そうすると、[miragefairy2024.client.mod.recipeviewer.vanilla.VanillaContainerWidget.add] が返す [miragefairy2024.util.Remover] だけで、画面に触らずに子を外せるのだ～🌱
 */
class VanillaContainerWidget(x: Int, y: Int, width: Int, height: Int) : AbstractWidget(x, y, width, height, Component.empty()) {

    private class WidgetSlot(val widget: AbstractWidget)

    private val widgetSlots = mutableListOf<WidgetSlot>()
    var widgets = listOf<AbstractWidget>()
        private set

    fun add(widget: AbstractWidget): Remover {
        val widgetSlot = WidgetSlot(widget)
        widgetSlots += widgetSlot
        widgets = widgetSlots.map { it.widget }
        return Remover {
            widgetSlots -= widgetSlot
            widgets = widgetSlots.map { it.widget }
        }
    }


    // バニラのAbstractWidgetはisDraggingを持たないのだ～🌱 だから、掴んだ子を自分で覚えて、マウスが外へ出ても渡し続けるのだ～🌱
    private var draggedWidget: AbstractWidget? = null

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val widget = widgets.toList().firstOrNull {
            it.isMouseOver(mouseX, mouseY) && it.mouseClicked(mouseX, mouseY, button)
        }
        draggedWidget = widget
        return widget != null
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        return draggedWidget?.mouseDragged(mouseX, mouseY, button, dragX, dragY) ?: false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val widget = draggedWidget
        draggedWidget = null
        if (widget != null) return widget.mouseReleased(mouseX, mouseY, button)
        return widgets.toList().any {
            it.isMouseOver(mouseX, mouseY) && it.mouseReleased(mouseX, mouseY, button)
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return widgets.toList().any {
            it.isMouseOver(mouseX, mouseY) && it.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        }
    }


    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return widgets.toList().any {
            it.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return widgets.toList().any {
            it.keyReleased(keyCode, scanCode, modifiers)
        }
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        return widgets.toList().any {
            it.charTyped(codePoint, modifiers)
        }
    }


    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        widgets.forEach {
            it.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

}

infix fun VanillaContainerWidget.place(widget: AbstractWidget) = this.add(widget)
