package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.RenderOverflow
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.util.fire
import mirrg.kotlin.helium.Slot
import mirrg.kotlin.helium.atLeast
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object ImageButtonViewRenderer : ViewRenderer<ImageButtonView, Unit> {
    override fun createState(view: ImageButtonView) = Unit

    override fun isFocusable(view: ImageButtonView, state: Slot<Unit>) = view.enabled.value

    override fun mouseClicked(view: ImageButtonView, state: Slot<Unit>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int): Boolean {
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

    override fun getRenderOverflow(view: ImageButtonView, bounds: IntRectangle): RenderOverflow {
        val textureBounds = view.texture?.bounds
        val hoveredTextureBounds = view.hoveredTexture?.bounds
        val disabledTextureBounds = view.disabledTexture?.bounds
        val width = maxOf(textureBounds?.sizeX ?: 0, hoveredTextureBounds?.sizeX ?: 0, disabledTextureBounds?.sizeX ?: 0)
        val height = maxOf(textureBounds?.sizeY ?: 0, hoveredTextureBounds?.sizeY ?: 0, disabledTextureBounds?.sizeY ?: 0)
        return RenderOverflow(0, width - bounds.sizeX atLeast 0, 0, height - bounds.sizeY atLeast 0)
    }

    override fun render(view: ImageButtonView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        if (!view.enabled.value) {
            drawTexture(bounds, graphics, view.disabledTexture ?: view.texture!!)
        } else if (isHovered) {
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
