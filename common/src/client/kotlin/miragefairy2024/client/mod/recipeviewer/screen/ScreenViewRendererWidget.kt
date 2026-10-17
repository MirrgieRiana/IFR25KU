package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

/** [miragefairy2024.client.mod.recipeviewer.ViewRenderer]による描画と入力を、バニラの画面のウィジェットとして振る舞わせるのだ～🌱 */
class ScreenViewRendererWidget<V : PlaceableView>(
    private val renderer: ViewRenderer<V>,
    private val view: V,
    private val viewBounds: IntRectangle,
) : AbstractWidget(viewBounds.x, viewBounds.y, viewBounds.sizeX, viewBounds.sizeY, Component.empty()) {

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderer.render(view, viewBounds, guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isMouseOver(mouseX, mouseY)) return false
        return renderer.mouseClicked(view, viewBounds, mouseX.toInt(), mouseY.toInt(), button)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

}
