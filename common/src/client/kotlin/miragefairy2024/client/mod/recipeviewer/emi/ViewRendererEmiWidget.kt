package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.Widget
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.gui.GuiGraphics

class ViewRendererEmiWidget<V : View>(private val renderer: ViewRenderer<V>, private val view: V, x: Int, y: Int) : Widget() {
    private val boundsCache by lazy { Bounds(x, y, view.getWidth(), view.getHeight()) }
    override fun getBounds() = boundsCache
    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderer.render(view, boundsCache.x, boundsCache.y, draw, mouseX, mouseY, delta)
    }
}
