package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.network.chat.Component

internal class VanillaClippedWidget(val widget: AbstractWidget, private val clipBounds: () -> IntRectangle) : AbstractWidget(widget.x, widget.y, widget.width, widget.height, Component.empty()) {

    private fun getEffectiveClipBounds(): IntRectangle {
        val bounds = clipBounds()
        if (widget !is VanillaClippedWidget) return bounds
        val other = widget.getEffectiveClipBounds()
        val x = bounds.x max other.x
        val y = bounds.y max other.y
        return IntRectangle(x, y, minOf(bounds.x + bounds.sizeX, other.x + other.sizeX) - x atLeast 0, minOf(bounds.y + bounds.sizeY, other.y + other.sizeY) - y atLeast 0)
    }

    private fun contains(mouseX: Double, mouseY: Double): Boolean {
        val bounds = getEffectiveClipBounds()
        return mouseX >= bounds.x && mouseY >= bounds.y && mouseX < bounds.x + bounds.sizeX && mouseY < bounds.y + bounds.sizeY
    }

    private fun intersects(): Boolean {
        val bounds = getEffectiveClipBounds()
        return bounds.sizeX > 0 && bounds.sizeY > 0 && widget.x < bounds.x + bounds.sizeX && widget.x + widget.width > bounds.x && widget.y < bounds.y + bounds.sizeY && widget.y + widget.height > bounds.y
    }

    override fun getX() = widget.x
    override fun getY() = widget.y
    override fun getWidth() = widget.width
    override fun getHeight() = widget.height
    override fun getRectangle() = widget.rectangle
    override fun getTabOrderGroup() = widget.tabOrderGroup

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (!intersects()) return
        val bounds = getEffectiveClipBounds()
        guiGraphics.enableScissor(bounds.x, bounds.y, bounds.x + bounds.sizeX, bounds.y + bounds.sizeY)
        try {
            val inside = contains(mouseX.toDouble(), mouseY.toDouble())
            widget.render(guiGraphics, if (inside) mouseX else Int.MIN_VALUE, if (inside) mouseY else Int.MIN_VALUE, partialTick)
        } finally {
            guiGraphics.disableScissor()
        }
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double) = contains(mouseX, mouseY) && widget.isMouseOver(mouseX, mouseY)
    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (contains(mouseX, mouseY)) widget.mouseMoved(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = contains(mouseX, mouseY) && widget.mouseClicked(mouseX, mouseY, button)
    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double) = contains(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double) = widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) = widget.mouseReleased(mouseX, mouseY, button)
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = widget.keyPressed(keyCode, scanCode, modifiers)
    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) = widget.keyReleased(keyCode, scanCode, modifiers)
    override fun charTyped(codePoint: Char, modifiers: Int) = widget.charTyped(codePoint, modifiers)
    override fun isFocused() = widget.isFocused
    override fun setFocused(focused: Boolean) = widget.setFocused(focused)

    private fun wrapFocusPath(path: ComponentPath?): ComponentPath? {
        if (path == null) return null
        return object : ComponentPath {
            override fun component(): GuiEventListener = this@VanillaClippedWidget
            override fun applyFocus(focused: Boolean) = path.applyFocus(focused)
        }
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? = if (intersects()) wrapFocusPath(widget.nextFocusPath(event)) else null
    override fun getCurrentFocusPath() = wrapFocusPath(widget.currentFocusPath)
    override fun isActive() = widget.isActive && intersects()
    override fun narrationPriority(): NarratableEntry.NarrationPriority = if (isActive) widget.narrationPriority() else NarratableEntry.NarrationPriority.NONE
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = widget.updateNarration(narrationElementOutput)

}
