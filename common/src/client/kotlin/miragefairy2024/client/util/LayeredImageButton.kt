package miragefairy2024.client.util

import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.network.chat.Component

class LayeredImageButton(
    width: Int,
    height: Int,
    private val backgroundSprites: WidgetSprites,
    private val foregroundSprites: WidgetSprites,
) : Button(0, 0, width, height, Component.empty(), {
    (it as LayeredImageButton).onClick.fire()
}, DEFAULT_NARRATION) {
    val onClick = EventRegistry<() -> Unit>()
    var selected = false

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.blitSprite(backgroundSprites.get(selected, isHoveredOrFocused), x, y, width, height)
        guiGraphics.blitSprite(foregroundSprites.get(selected, isHoveredOrFocused), x, y, width, height)
    }
}
