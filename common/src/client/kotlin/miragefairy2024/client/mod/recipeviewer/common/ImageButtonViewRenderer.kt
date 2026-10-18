package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.contains
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.util.fire
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object ImageButtonViewRenderer : ViewRenderer<ImageButtonView> {

    override fun mouseClicked(view: ImageButtonView, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int): Boolean {
        if (!view.enabled.value) return false
        if (button != 0) return false
        view.onClick.fire {
            if (it()) {
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F))
                return true
            }
        }
        return false
    }

    override fun render(view: ImageButtonView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (!view.enabled.value) {
            drawTexture(bounds, graphics, view.disabledTexture ?: view.texture!!)
        } else if (bounds.contains(mouseX, mouseY)) {
            drawTexture(bounds, graphics, view.hoveredTexture ?: view.texture!!)
        } else {
            drawTexture(bounds, graphics, view.texture!!)
        }
    }

    private fun drawTexture(bounds: IntRectangle, graphics: GuiGraphics, texture: ViewTexture) {
        graphics.blit(
            texture.id,
            bounds.x,
            bounds.y,
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

}
