package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.util.drawScrollingString
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.contains
import miragefairy2024.mod.recipeviewer.views.TextView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

object TextViewRenderer : ViewRenderer<TextView> {

    private val startNanoTime by lazy { System.nanoTime() }

    override fun render(view: TextView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val font = Minecraft.getInstance().font
        val color = view.color.lightModeArgb
        if (view.scroll && font.width(view.text.value) > view.actualSize.x) {
            graphics.drawScrollingString(
                font,
                view.text.value,
                bounds.x,
                bounds.x + view.actualSize.x,
                bounds.y,
                startNanoTime,
                30.0,
                7.0,
                color,
                view.shadow,
            )
        } else {
            val x = when (view.alignmentX) {
                Alignment.START -> bounds.x
                Alignment.CENTER -> bounds.x + view.actualSize.x / 2 - font.width(view.text.value) / 2
                Alignment.END -> bounds.x + view.actualSize.x - font.width(view.text.value)
            }
            graphics.drawString(
                font,
                view.text.value,
                x,
                bounds.y,
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
