package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class VanillaViewRendererWidget<V : PlaceableView>(private val renderer: ViewRenderer<V>, private val view: V, private val bounds2: IntRectangle) :
    AbstractWidget(bounds2.x, bounds2.y, bounds2.sizeX, bounds2.sizeY, Component.empty()) {
    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = renderer.render(view, bounds2, guiGraphics, mouseX, mouseY, partialTick)
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = if (!isMouseOver(mouseX, mouseY)) false else renderer.mouseClicked(view, bounds2, mouseX.toInt(), mouseY.toInt(), button)
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = renderer.keyPressed(view, bounds2, keyCode, scanCode, modifiers)
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
}
