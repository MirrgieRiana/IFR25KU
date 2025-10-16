package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.widget.Widget
import miragefairy2024.client.util.drawScrollingString
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.toEmiBounds
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

class EmiTextWidget(private val offset: IntPoint, private val view: TextView) : Widget() {

    private val emiBounds = offset.sized(view.actualSize).toEmiBounds()
    override fun getBounds() = emiBounds

    private val startNanoTime by lazy { System.nanoTime() }

    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val font = Minecraft.getInstance().font
        val color = view.color.lightModeArgb
        if (view.scroll && font.width(view.text.value) > view.actualSize.x) {
            draw.drawScrollingString(
                font,
                view.text.value,
                offset.x,
                offset.x + view.actualSize.x,
                offset.y,
                startNanoTime,
                30.0,
                7.0,
                color,
                view.shadow,
            )
        } else {
            val x = when (view.alignmentX) {
                Alignment.START -> offset.x
                Alignment.CENTER -> offset.x + view.actualSize.x / 2 - font.width(view.text.value) / 2
                Alignment.END -> offset.x + view.actualSize.x - font.width(view.text.value)
            }
            draw.drawString(
                font,
                view.text.value,
                x,
                offset.y,
                color,
                view.shadow,
            )
        }
    }

    override fun getTooltip(mouseX: Int, mouseY: Int) = view.tooltip?.let { tooltip -> tooltip.map { ClientTooltipComponent.create(it.visualOrderText) } } ?: listOf()

}
