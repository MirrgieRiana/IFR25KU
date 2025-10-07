package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.toReiRectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener

class ViewRendererReiWidget<V : PlaceableView>(private val renderer: ViewRenderer<V>, private val view: V, bounds: IntRectangle) : WidgetWithBounds() {
    private val bounds2 = bounds
    private val reiBounds = bounds.toReiRectangle()
    override fun children() = listOf<GuiEventListener>()
    override fun getBounds() = reiBounds
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = renderer.render(view, bounds2, context, IntPoint(mouseX, mouseY), delta)
}
