package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.util.findFocusedLeaf
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.util.Remover
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import java.util.Optional
import kotlin.math.roundToInt

// TODO 子 Widget の描画・入力・フォーカス・読み上げを中継するスクロール領域なのだ～🌱
abstract class VanillaScrollViewWidget(
    protected val view: ScrollView,
    bounds: IntRectangle,
    private var viewport: IntRectangle,
    private val contentLength: Int,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) :
    AbstractContainerWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty()) {

    private class WidgetRegistration(val widget: AbstractWidget, val interactive: Boolean)

    private val widgetRegistrations = mutableListOf<WidgetRegistration>()
    private var disposed = false
    private inner class PlacerContext(override val origin: IntPoint) : VanillaViewPlacerContext {
        override fun addRenderableWidget(widget: AbstractWidget) = register(widget, true)
        override fun addRenderableOnly(widget: AbstractWidget) = register(widget, false)
    }

    private val childController: ViewController

    init {
        var controllers: MutableList<ViewController>? = mutableListOf()
        val childContext = PlacerContext(context.origin)
        try {
            childController = view.childView.attachTo(IntPoint(x, y)) { child, childBounds ->
                if (child is MenuSlotView) throw UnsupportedOperationException("MenuSlotView cannot be placed inside a scroll view")
                registry.place(childContext, child, childBounds).also {
                    controllers?.add(it)
                }
            }
        } catch (exception: Throwable) {
            controllers!!.asReversed().forEach {
                it.dispose()
                it.detach()
            }
            throw exception
        }
        // 初期化に失敗した場合だけ巻き戻すため、成功後は配置履歴を破棄するのだ～🌱
        controllers = null
    }

    private fun register(widget: AbstractWidget, interactive: Boolean): Remover {
        val widgetRegistration = WidgetRegistration(widget, interactive)
        widgetRegistrations += widgetRegistration
        return Remover {
            widgetRegistrations.remove(widgetRegistration)
            if (widgetRegistrations.none { it.interactive && it.widget === widget }) {
                if (focused === widget) focused = null
            }
        }
    }

    override fun children() = widgetRegistrations.filter { it.interactive }.map { it.widget }

    fun dispose() {
        if (disposed) return
        disposed = true
        isDragging = false
        focused = null
        childController.dispose()
    }

    fun detach() = childController.detach()


    protected abstract val viewportLength: Int
    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        viewport = IntRectangle(x, y, viewport.sizeX, viewport.sizeY)
        childController.setOffset(IntPoint(x, y))
    }


    override fun isActive() = super.isActive() && !disposed

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // TODO 表示領域内の子 Widget を描画するのだ～🌱
    }


    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        // TODO 子 Widget の読み上げを中継するのだ～🌱
    }

}

class VanillaXScrollViewWidget(
    view: XScrollView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) : VanillaScrollViewWidget(
    view,
    bounds,
    IntRectangle(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY - view.scrollbarThickness atLeast 0),
    view.childView.evaluateActualSizeX(),
    context,
    registry,
) {

    override val viewportLength get() = width


    // TODO X 軸の残りのスクロール操作を実装するのだ～🌱

}

class VanillaYScrollViewWidget(
    view: YScrollView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) : VanillaScrollViewWidget(
    view,
    bounds,
    IntRectangle(bounds.x, bounds.y, bounds.sizeX - view.scrollbarThickness atLeast 0, bounds.sizeY),
    view.childView.evaluateActualSizeY(),
    context,
    registry,
) {

    override val viewportLength get() = height


    // TODO Y 軸の残りのスクロール操作を実装するのだ～🌱

}
