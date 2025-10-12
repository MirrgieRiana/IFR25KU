package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.REIRuntime
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import miragefairy2024.client.util.drawScrollingString
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.toReiRectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener

class ReiTextWidget(private val offset: IntPoint, private val view: TextView) : WidgetWithBounds() {

    private val reiBounds = offset.sized(view.actualSize).toReiRectangle()
    override fun getBounds() = reiBounds

    override fun children() = listOf<GuiEventListener>()

    private val startNanoTime by lazy { System.nanoTime() }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val color = if (REIRuntime.getInstance().isDarkThemeEnabled) view.color.darkModeArgb else view.color.lightModeArgb
        if (view.scroll && font.width(view.text.value) > view.actualSize.x) {
            guiGraphics.drawScrollingString(
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
            guiGraphics.drawString(
                font,
                view.text.value,
                x,
                offset.y,
                color,
                view.shadow,
            )
        }
        if (reiBounds.contains(mouseX, mouseY)) {
            val tooltip = view.tooltip
            if (tooltip != null) Tooltip.create(tooltip).queue()
        }
    }

}
