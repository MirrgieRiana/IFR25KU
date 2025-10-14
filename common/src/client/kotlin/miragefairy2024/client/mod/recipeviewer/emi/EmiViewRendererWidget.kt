package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Widget
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.toEmiBounds
import net.minecraft.client.gui.GuiGraphics

class EmiViewRendererWidget<V : PlaceableView>(private val renderer: ViewRenderer<V>, private val view: V, private val bounds2: IntRectangle) : Widget() {
    private val boundsCache = bounds2.toEmiBounds()
    override fun getBounds() = boundsCache
    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderer.render(view, bounds2, draw, mouseX, mouseY, delta)
    }
}
