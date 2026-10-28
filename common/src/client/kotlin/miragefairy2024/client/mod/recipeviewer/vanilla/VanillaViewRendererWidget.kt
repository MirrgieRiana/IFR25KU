package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class VanillaViewRendererWidget<V : PlaceableView>(private val renderer: ViewRenderer<V>, private val view: V, private var bounds2: IntRectangle) :
    AbstractWidget(bounds2.x, bounds2.y, bounds2.sizeX, bounds2.sizeY, Component.empty()) {

    private val pressedButtons = mutableSetOf<Int>()

    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        bounds2 = IntRectangle(x, y, width, height)
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = renderer.render(view, bounds2, guiGraphics, mouseX, mouseY, partialTick)

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        // クリックイベントが対で届かなかった場合の掃除のために、念のため消すのだ～🌱
        pressedButtons -= button
        if (!isMouseOver(mouseX, mouseY)) return false
        if (renderer.mouseClicked(view, bounds2, mouseX.toInt(), mouseY.toInt(), button)) {
            // 実際にクリックを消費した場合のみ、押下中にするのだ～🌱
            pressedButtons += button
            return true
        }
        return false
    }

    // クリックを消費しているときは、ドラッグも消費するのだ～🌱
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double) = button in pressedButtons

    // クリックを消費しているときは、離すイベントも消費するのだ～🌱
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) = pressedButtons.remove(button)

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = renderer.keyPressed(view, bounds2, keyCode, scanCode, modifiers)

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

}
