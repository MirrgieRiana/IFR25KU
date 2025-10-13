package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.contains
import miragefairy2024.mod.recipeviewer.view.x1
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y1
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.util.fire
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object ClickableViewRenderer : ViewRenderer<ClickableView> {

    override fun mouseClicked(view: ClickableView, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int): Boolean {
        if (button != 0) return false
        view.onClick.fire {
            if (it()) {
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F))
                return true
            }
        }
        return false
    }

    override fun render(view: ClickableView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (bounds.contains(mouseX, mouseY)) {
            graphics.fill(
                RenderType.guiOverlay(),
                bounds.x1,
                bounds.y1,
                bounds.x2,
                bounds.y2,
                0x80FFFFFF.toInt(),
            )
        }
    }

}
