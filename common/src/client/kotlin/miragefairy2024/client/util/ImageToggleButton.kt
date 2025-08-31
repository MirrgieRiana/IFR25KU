package miragefairy2024.client.util

import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.fire
import miragefairy2024.util.register
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.network.chat.Component

class ImageToggleButton(width: Int, height: Int, private val sprites: WidgetSprites) : Button(0, 0, width, height, Component.empty(), {
    (it as ImageToggleButton).onClick.fire()
}, DEFAULT_NARRATION) {
    val onClick = EventRegistry<() -> Unit>()
    val value = ObservableValue(false)

    init {
        onClick.register {
            toggle()
        }
    }

    fun toggle() {
        value.value = !value.value
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.blitSprite(sprites.get(value.value, isHoveredOrFocused), x, y, width, height)
    }
}
