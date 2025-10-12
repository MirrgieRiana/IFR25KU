package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Widget
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.toEmiBounds
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

class EmiImageButtonWidget(private val offset: IntPoint, private val view: ImageButtonView) : Widget() {

    val onClick = EventRegistry<() -> Unit>()

    private val emiBounds = offset.sized(view.actualSize).toEmiBounds()
    override fun getBounds() = emiBounds

    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (!view.enabled.value) {
            drawTexture(draw, view.disabledTexture ?: view.texture!!)
        } else if (emiBounds.contains(mouseX, mouseY)) {
            drawTexture(draw, view.hoveredTexture ?: view.texture!!)
        } else {
            drawTexture(draw, view.texture!!)
        }
    }

    private fun drawTexture(draw: GuiGraphics, texture: ViewTexture) {
        draw.blit(
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

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int): Boolean {
        if (!view.enabled.value) return false
        if (button != 0) return false
        onClick.fire()
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F))
        return true
    }

}
