package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.contains
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

object MultiLineTextViewRenderer : ViewRenderer<MultiLineTextView> {

    override fun render(view: MultiLineTextView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val font = Minecraft.getInstance().font
        val color = view.color.lightModeArgb
        view.lines.evaluate().forEachIndexed { index, line ->
            val x = when (view.alignmentX) {
                Alignment.START -> bounds.x
                Alignment.CENTER -> bounds.x + view.evaluateActualSizeX() / 2 - font.width(line) / 2
                Alignment.END -> bounds.x + view.evaluateActualSizeX() - font.width(line)
            }
            graphics.drawString(
                font,
                line,
                x,
                bounds.y + (font.lineHeight + view.lineSpacing) * index,
                color,
                view.shadow,
            )
        }
        if (bounds.contains(mouseX, mouseY)) {
            val tooltip = view.tooltip
            if (tooltip != null) graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY)
        }
    }

}
