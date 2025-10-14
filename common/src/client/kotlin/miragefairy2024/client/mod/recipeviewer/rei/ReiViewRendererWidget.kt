package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.toReiRectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener

class ReiViewRendererWidget<V : PlaceableView>(private val renderer: ViewRenderer<V>, private val view: V, private val bounds2: IntRectangle) : WidgetWithBounds() {
    private val boundsCache = bounds2.toReiRectangle()
    override fun getBounds() = boundsCache
    override fun children() = listOf<GuiEventListener>()
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = renderer.mouseClicked(view, bounds2, mouseX.toInt(), mouseY.toInt(), button)
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = renderer.keyPressed(view, bounds2, keyCode, scanCode, modifiers)
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = renderer.render(view, bounds2, context, mouseX, mouseY, delta)
}
