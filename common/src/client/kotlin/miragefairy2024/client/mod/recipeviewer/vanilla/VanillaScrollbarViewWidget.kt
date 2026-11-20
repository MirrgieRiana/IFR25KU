package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.util.ObservableValue
import net.minecraft.client.gui.GuiGraphics

abstract class VanillaScrollbarViewWidget(
    private val view: ScrollbarView,
    scrollOffsetValue: ObservableValue<Double>,
    bounds: IntRectangle,
    horizontal: Boolean,
) : AbstractVanillaScrollbarViewWidget(view, scrollOffsetValue, bounds, horizontal) {

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int) {
        if (horizontal) {
            guiGraphics.fill(x + start, y, x + start + length, y + height, view.scrollbarColor)
        } else {
            guiGraphics.fill(x, y + start, x + width, y + start + length, view.scrollbarColor)
        }
    }

}

class VanillaXScrollbarViewWidget(
    view: XScrollbarView,
    bounds: IntRectangle,
) : VanillaScrollbarViewWidget(view, view.scrollOffsetX, bounds, horizontal = true)

class VanillaYScrollbarViewWidget(
    view: YScrollbarView,
    bounds: IntRectangle,
) : VanillaScrollbarViewWidget(view, view.scrollOffsetY, bounds, horizontal = false)
