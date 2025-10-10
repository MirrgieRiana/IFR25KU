package miragefairy2024.client.util

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.WrappingParentComponent
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import miragefairy2024.util.EventRegistry

class ClickableContainer(horizontalSizing: Sizing, verticalSizing: Sizing) : WrappingParentComponent<OwoComponent>(horizontalSizing, verticalSizing, Components.spacer(0)) {
    // これを設定してもchildが受け取ってしまうのでカーソルを設定することができない
    // see: io.wispforest.owo.ui.core.OwoUIAdapter.render
    //init {
    //    cursorStyle(CursorStyle.HAND)
    //}

    override fun canFocus(source: FocusSource) = source == FocusSource.KEYBOARD_CYCLE

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        super.draw(context, mouseX, mouseY, partialTicks, delta)
        drawChildren(context, mouseX, mouseY, partialTicks, delta, childView)
        if (isInBoundingBox(mouseX.toDouble(), mouseY.toDouble())) {
            context.fill(x, y, x + width, y + height, 0x80FFFFFF.toInt())
        }
    }

    val onClick = EventRegistry<() -> Boolean>()
    override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.onMouseDown(mouseX, mouseY, button)) return true
        return onClick.listeners.any { it() }
    }
}
