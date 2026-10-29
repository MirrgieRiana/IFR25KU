package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import mirrg.kotlin.helium.atLeast
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

abstract class VanillaScrollViewWidget(
    bounds: IntRectangle,
    viewport: IntRectangle,
    private val contentLength: Int,
) : AbstractWidget(
    bounds.x,
    bounds.y,
    bounds.sizeX,
    bounds.sizeY,
    Component.empty(),
) {

    internal var viewport = viewport
        private set
    protected abstract val viewportLength: Int
    private val maxScroll get() = contentLength - viewportLength atLeast 0
    private var scrollOffset = 0.0

    protected abstract fun getChildOffset(pixels: Int): IntPoint

    internal fun getChildOffset() = getChildOffset(scrollOffset.roundToInt())

    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        viewport = IntRectangle(x, y, viewport.sizeX, viewport.sizeY)
        scrollOffset = scrollOffset.coerceIn(0.0, maxScroll.toDouble())
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

}

class VanillaXScrollViewWidget(
    view: XScrollView,
    bounds: IntRectangle,
) : VanillaScrollViewWidget(
    bounds,
    IntRectangle(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY - view.scrollbarThickness atLeast 0),
    view.childView.evaluateActualSizeX(),
) {

    override val viewportLength get() = viewport.sizeX

    override fun getChildOffset(pixels: Int) = IntPoint(x - pixels, y)

}

class VanillaYScrollViewWidget(
    view: YScrollView,
    bounds: IntRectangle,
) : VanillaScrollViewWidget(
    bounds,
    IntRectangle(bounds.x, bounds.y, bounds.sizeX - view.scrollbarThickness atLeast 0, bounds.sizeY),
    view.childView.evaluateActualSizeY(),
) {

    override val viewportLength get() = viewport.sizeY

    override fun getChildOffset(pixels: Int) = IntPoint(x, y - pixels)

}

internal fun placeScrollView(context: VanillaViewPlacerContext, scrollWidget: VanillaScrollViewWidget, childView: View, registry: ViewPlacerRegistry<VanillaViewPlacerContext>): ViewController {
    val remover = context.addRenderableOnly(scrollWidget)
    val childContext: VanillaViewPlacerContext = object : VanillaViewPlacerContext by context {
        override fun addRenderableWidget(widget: AbstractWidget) = context.addRenderableWidget(VanillaClippedWidget(widget) { scrollWidget.viewport })
        override fun addRenderableOnly(widget: AbstractWidget) = context.addRenderableOnly(VanillaClippedWidget(widget) { scrollWidget.viewport })
    }
    val childController = childView.attachTo(scrollWidget.getChildOffset()) { child, bounds ->
        registry.place(childContext, child, bounds)
    }
    return object : ViewController {
        override fun setOffset(offset: IntPoint) {
            scrollWidget.setPosition(offset.x, offset.y)
            childController.setOffset(scrollWidget.getChildOffset())
        }

        override fun detach() {
            childController.detach()
            remover.remove()
        }

        override fun dispose() = childController.dispose()
    }
}
