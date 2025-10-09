package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener

class ReiViewRendererWidget<V : View>(private val renderer: ViewRenderer<V>, private val view: V, x: Int, y: Int) : WidgetWithBounds() {
    private val boundsCache by lazy { Rectangle(x, y, view.getWidth(), view.getHeight()) }
    override fun children() = listOf<GuiEventListener>()
    override fun getBounds() = boundsCache
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderer.render(view, boundsCache.x, boundsCache.y, context, mouseX, mouseY, delta)
    }
}
