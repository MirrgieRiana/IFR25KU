package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.toReiRectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

class ReiImageButtonWidget(private val offset: IntPoint, private val view: ImageButtonView) : WidgetWithBounds() {

    val onClick = EventRegistry<() -> Unit>()

    private val reiBounds = offset.sized(view.calculatedSize).toReiRectangle()
    override fun getBounds() = reiBounds

    override fun children() = listOf<GuiEventListener>()

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (!view.enabled.value) {
            drawTexture(guiGraphics, view.disabledTexture ?: view.texture!!)
        } else if (reiBounds.contains(mouseX, mouseY)) {
            drawTexture(guiGraphics, view.hoveredTexture ?: view.texture!!)
        } else {
            drawTexture(guiGraphics, view.texture!!)
        }
    }

    private fun drawTexture(guiGraphics: GuiGraphics, texture: ViewTexture) {
        guiGraphics.blit(
            texture.id,
            offset.x,
            offset.y,
            texture.bounds.sizeX,
            texture.bounds.sizeY,
            texture.bounds.x.toFloat(),
            texture.bounds.y.toFloat(),
            texture.bounds.sizeX,
            texture.bounds.sizeY,
            texture.size.x,
            texture.size.y,
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!view.enabled.value) return false
        if (button != 0) return false
        if (!containsMouse(mouseX, mouseY)) return false
        onClick.fire()
        minecraft.soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F))
        return true
    }

}
