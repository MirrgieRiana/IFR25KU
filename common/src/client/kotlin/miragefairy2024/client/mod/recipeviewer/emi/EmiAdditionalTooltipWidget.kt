package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.Widget
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

class EmiAdditionalTooltipWidget(private val widget: Widget, private val tooltip: List<ClientTooltipComponent>) : Widget() {
    override fun getBounds(): Bounds = widget.bounds
    override fun getTooltip(mouseX: Int, mouseY: Int) = tooltip
    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = Unit
}
