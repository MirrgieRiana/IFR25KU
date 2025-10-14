package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.toReiRectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener

class ReiViewRendererWidget<V : View>(private val renderer: ViewRenderer<V>, private val view: V, private val bounds2: IntRectangle) : WidgetWithBounds() {
    private val boundsCache = bounds2.toReiRectangle()
    override fun children() = listOf<GuiEventListener>()
    override fun getBounds() = boundsCache
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderer.render(view, bounds2, context, mouseX, mouseY, delta)
    }
}
