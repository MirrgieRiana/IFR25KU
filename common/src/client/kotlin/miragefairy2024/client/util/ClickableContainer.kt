package miragefairy2024.client.util

import io.wispforest.owo.ui.container.WrappingParentComponent
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Component as OwoComponent

class ClickableContainer<C : OwoComponent>(horizontalSizing: Sizing, verticalSizing: Sizing, private val action: () -> Boolean, child: () -> C) : WrappingParentComponent<C>(horizontalSizing, verticalSizing, child()) {
    // これを設定してもchildが受け取ってしまうのでカーソルを設定することができない
    // see: io.wispforest.owo.ui.core.OwoUIAdapter.render
    //init {
    //    cursorStyle(CursorStyle.HAND)
    //}

    override fun canFocus(source: OwoComponent.FocusSource) = source == OwoComponent.FocusSource.KEYBOARD_CYCLE

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        super.draw(context, mouseX, mouseY, partialTicks, delta)
        drawChildren(context, mouseX, mouseY, partialTicks, delta, childView)
        if (isInBoundingBox(mouseX.toDouble(), mouseY.toDouble())) {
            context.fill(x, y, x + width, y + height, 0x80FFFFFF.toInt())
        }
    }

    override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.onMouseDown(mouseX, mouseY, button)) return true
        return action()
    }
}
