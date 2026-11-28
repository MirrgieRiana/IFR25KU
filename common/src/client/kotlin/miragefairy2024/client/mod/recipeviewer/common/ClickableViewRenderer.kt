package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.x1
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y1
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.util.fire
import mirrg.kotlin.helium.Slot
import mirrg.kotlin.hydrogen.alphaOfArgb
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.CommonInputs
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object ClickableViewRenderer : ViewRenderer<ClickableView, Unit> {
    override fun createState(view: ClickableView) = Unit

    override fun isFocusable(view: ClickableView, state: Slot<Unit>) = true

    override fun getNarrationLabel(view: ClickableView) = view.narrationLabel

    override fun mouseClicked(view: ClickableView, state: Slot<Unit>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int): Boolean {
        if (button != 0) return false
        return activate(view)
    }

    override fun keyPressed(view: ClickableView, state: Slot<Unit>, bounds: IntRectangle, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!CommonInputs.selected(keyCode)) return false
        return activate(view)
    }

    override fun render(view: ClickableView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        val color = view.hoverOverlayColor
        if (isHovered && color.alphaOfArgb != 0) {
            graphics.fill(
                RenderType.guiOverlay(),
                bounds.x1,
                bounds.y1,
                bounds.x2,
                bounds.y2,
                color,
            )
        }
    }

    private fun activate(view: ClickableView): Boolean {
        view.onClick.fire {
            if (it()) {
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F))
                return true
            }
        }
        return false
    }

}
