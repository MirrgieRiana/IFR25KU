package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Widget
import miragefairy2024.client.mod.recipeviewer.RecipeViewKeyboardTarget
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.toEmiBounds
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle

class EmiViewRendererWidget<V : PlaceableView, S>(private val renderer: ViewRenderer<V, S>, private val view: V, private val bounds2: IntRectangle, container: EmiContainerWidget?) : Widget() {
    private val state = Slot(renderer.createState(view))
    private val boundsCache = bounds2.toEmiBounds()
    val keyboardTarget = if (container != null && renderer.supportsKeyboardActivation) {
        val bounds = ScreenRectangle(container.screenOrigin.x + bounds2.x, container.screenOrigin.y + bounds2.y, bounds2.sizeX, bounds2.sizeY)
        RecipeViewKeyboardTarget(bounds, { renderer.isFocusable(view, state) && container.canFocus(this) }) { keyCode, scanCode, modifiers ->
            renderer.keyPressed(view, state, bounds2, keyCode, scanCode, modifiers, true)
        }
    } else {
        null
    }

    override fun getBounds() = boundsCache
    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) = renderer.mouseClicked(view, state, bounds2, mouseX, mouseY, button)
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = renderer.keyPressed(view, state, bounds2, keyCode, scanCode, modifiers)
    override fun render(draw: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) = renderer.render(view, state, bounds2, draw, mouseX, mouseY, bounds.contains(mouseX, mouseY), delta)
}
