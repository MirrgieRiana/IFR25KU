package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.util.Remover
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.network.chat.Component

/**
 * 子のウィジェットを自前で保持して、描画と入力を分配するウィジェットなのだ～🌱
 * 画面へはこれ 1 個だけを登録するのだ～🌱
 * そうすると、[miragefairy2024.client.mod.recipeviewer.vanilla.VanillaContainerWidget.add] が返す [miragefairy2024.util.Remover] だけで、画面に触らずに子を外せるのだ～🌱
 *
 * 入力の分配は [net.minecraft.client.gui.components.events.ContainerEventHandler] の既定の実装に任せるのだ～🌱
 * 画面はこれ 1 個しか知らないから、フォーカスの配分と読み上げの集約を、画面の代わりにこれが引き受けるのだ～🌱
 */
class VanillaContainerWidget(x: Int, y: Int, width: Int, height: Int) : AbstractWidget(x, y, width, height, Component.empty()), ContainerEventHandler {

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
            // 外した子がフォーカスを持っていると、その子へ入力が届き続けるのだ～🌱
            if (focusedChild === widget) focusedChild = null
        }
    }

    override fun children() = widgets


    private var dragging = false

    override fun isDragging() = dragging

    override fun setDragging(dragging: Boolean) {
        this.dragging = dragging
    }

    private var focusedChild: GuiEventListener? = null

    override fun getFocused() = focusedChild

    override fun setFocused(focused: GuiEventListener?) {
        focusedChild = focused
    }


    // 以下のメソッドは [net.minecraft.client.gui.components.AbstractWidget] の側にも実装があるため、どちらを使うかを明示するのだ～🌱

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = super<ContainerEventHandler>.mouseClicked(mouseX, mouseY, button)

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double) = super<ContainerEventHandler>.mouseDragged(mouseX, mouseY, button, dragX, dragY)

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) = super<ContainerEventHandler>.mouseReleased(mouseX, mouseY, button)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double) = super<ContainerEventHandler>.mouseScrolled(mouseX, mouseY, scrollX, scrollY)

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = super<ContainerEventHandler>.keyPressed(keyCode, scanCode, modifiers)

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) = super<ContainerEventHandler>.keyReleased(keyCode, scanCode, modifiers)

    override fun charTyped(codePoint: Char, modifiers: Int) = super<ContainerEventHandler>.charTyped(codePoint, modifiers)

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? = super<ContainerEventHandler>.nextFocusPath(event)

    // このコンテナ自身がフォーカスを持つことはなく、子のどれかが持つのだ～🌱 だから、子の側を見る実装を使うのだ～🌱
    override fun isFocused() = super<ContainerEventHandler>.isFocused()

    override fun setFocused(focused: Boolean) = super<ContainerEventHandler>.setFocused(focused)


    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        widgets.forEach {
            it.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        // 画面の読み上げの対象はこれ 1 個だけだから、子のうち優先度が最も高いものを選んで、その読み上げを代わりに出すのだ～🌱
        val widget = widgets.filter { it.narrationPriority() != NarratableEntry.NarrationPriority.NONE }.maxByOrNull { it.narrationPriority() } ?: return
        widget.updateNarration(narrationElementOutput)
    }

}

infix fun VanillaContainerWidget.place(widget: AbstractWidget) = this.add(widget)
