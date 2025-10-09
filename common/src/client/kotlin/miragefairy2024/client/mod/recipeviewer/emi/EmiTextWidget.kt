package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Widget
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.toEmiBounds
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

class EmiTextWidget(private val offset: IntPoint, private val view: TextView) : Widget() {

    private val emiBounds = offset.sized(view.calculatedSize).toEmiBounds()
    override fun getBounds() = emiBounds

    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val font = Minecraft.getInstance().font
        val x = when (view.alignmentX) {
            Alignment.START -> offset.x
            Alignment.CENTER -> offset.x + view.calculatedSize.x / 2 - font.width(view.text.value) / 2
            Alignment.END -> offset.x + view.calculatedSize.x - font.width(view.text.value)
        }
        draw.drawString(font, view.text.value, x, offset.y, view.color.lightModeArgb, view.shadow)
    }

    override fun getTooltip(mouseX: Int, mouseY: Int) = view.tooltip?.let { tooltip -> tooltip.map { ClientTooltipComponent.create(it.visualOrderText) } } ?: listOf()

}
