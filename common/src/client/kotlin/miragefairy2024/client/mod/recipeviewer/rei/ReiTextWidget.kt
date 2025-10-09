package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.REIRuntime
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.toReiRectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener

class ReiTextWidget(private val offset: IntPoint, private val view: TextView) : WidgetWithBounds() {

    private val reiBounds = offset.sized(view.calculatedSize).toReiRectangle()
    override fun getBounds() = reiBounds

    override fun children() = listOf<GuiEventListener>()

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val x = when (view.alignmentX) {
            Alignment.START -> offset.x
            Alignment.CENTER -> offset.x + view.calculatedSize.x / 2 - font.width(view.text.value) / 2
            Alignment.END -> offset.x + view.calculatedSize.x - font.width(view.text.value)
        }
        val color = if (REIRuntime.getInstance().isDarkThemeEnabled) view.color.darkModeArgb else view.color.lightModeArgb
        guiGraphics.drawString(font, view.text.value, x, offset.y, color, view.shadow)
        if (reiBounds.contains(mouseX, mouseY)) {
            val tooltip = view.tooltip
            if (tooltip != null) Tooltip.create(tooltip).queue()
        }
    }

}
