package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.FlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.XFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW

abstract class VanillaScrollbarViewWidget(view: FlatScrollbarView, bounds: IntRectangle) : AbstractVanillaScrollbarViewWidget<FlatScrollbarView>(view, bounds) {

    override fun shouldRenderScrollbar(scrollable: Boolean) = scrollable && scrollbarThickness > 0 && scrollbarLength > 0

}

class VanillaXScrollbarViewWidget(
    view: XFlatScrollbarView,
    bounds: IntRectangle,
) : VanillaScrollbarViewWidget(
    view,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = width
    override val scrollbarThickness get() = height


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x
    override fun getDragAmount(dragX: Double, dragY: Double) = dragX

    // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int, scrollable: Boolean) {
        guiGraphics.fill(
            x + start,
            y,
            x + start + length,
            y + height,
            view.scrollbarColor,
        )
    }

}

class VanillaYScrollbarViewWidget(
    view: YFlatScrollbarView,
    bounds: IntRectangle,
) : VanillaScrollbarViewWidget(
    view,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = height
    override val scrollbarThickness get() = width


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y
    override fun getDragAmount(dragX: Double, dragY: Double) = dragY
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    // TODO 操作やホバーをしていない間はフェードアウトするのだ～🌱
    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int, scrollable: Boolean) {
        guiGraphics.fill(
            x,
            y + start,
            x + width,
            y + start + length,
            view.scrollbarColor,
        )
    }

}
