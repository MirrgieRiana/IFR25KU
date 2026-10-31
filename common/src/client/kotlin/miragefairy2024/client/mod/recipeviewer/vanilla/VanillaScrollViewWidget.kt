package miragefairy2024.client.mod.recipeviewer.vanilla

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
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import java.util.Optional

// TODO 子 Widget の描画・入力・フォーカス・読み上げを中継するスクロール領域なのだ～🌱
abstract class VanillaScrollViewWidget(
    protected val view: ScrollView,
    bounds: IntRectangle,
    context: VanillaViewPlacerContext,
    registry: ViewPlacerRegistry<VanillaViewPlacerContext>,
) :
    AbstractContainerWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty()) {


    // Widget の登録なのだ～🌱

    private class WidgetRegistration(val widget: AbstractWidget, val interactive: Boolean)

    private val widgetRegistrations = mutableListOf<WidgetRegistration>()

    private fun register(widget: AbstractWidget, interactive: Boolean): Remover {
        val widgetRegistration = WidgetRegistration(widget, interactive)
        widgetRegistrations += widgetRegistration
        return Remover {
            widgetRegistrations -= widgetRegistration
            // フォーカスを持っていた子が消えた場合は、フォーカスを解除するのだ～🌱
            if (widgetRegistrations.none { it.interactive && it.widget === widget }) {
                if (focused === widget) focused = null
            }
        }
    }

    override fun children() = widgetRegistrations.mapNotNull { if (it.interactive) it.widget else null }


    // ライフサイクルなのだ～🌱

    private var disposed = false

    fun dispose() {
        disposed = true
        isDragging = false
        focused = null
        childController.dispose()
    }

    fun detach() {
        childController.detach()
    }


    // サイズと位置なのだ～🌱

    protected abstract fun getReservedRight(): Int
    protected abstract fun getReservedBottom(): Int

    protected abstract val viewportLength: Int
    protected abstract fun evaluateContentLength(): Int

    // TODO スクロール上限とつまみの長さを、`evaluateContentLength()` の評価結果から計算するのだ～🌱

    override fun setPosition(x: Int, y: Int) {
        super.setPosition(x, y)
        childController.setOffset(IntPoint(x, y))
    }


    // 描画とマウス入力なのだ～🌱

    private fun containsViewport(mouseX: Double, mouseY: Double): Boolean {
        if (mouseX < x) return false
        if (mouseY < y) return false
        if (mouseX >= x + (width - getReservedRight() atLeast 0)) return false
        if (mouseY >= y + (height - getReservedBottom() atLeast 0)) return false
        return true
    }

    override fun isActive() = super.isActive() && !disposed

    override fun isMouseOver(mouseX: Double, mouseY: Double) = isActive && super.isMouseOver(mouseX, mouseY)

    // 有効である子のみを返すのだ～🌱
    override fun getChildAt(mouseX: Double, mouseY: Double): Optional<GuiEventListener> {
        if (!isActive) return Optional.empty()
        if (!containsViewport(mouseX, mouseY)) return Optional.empty()
        return super.getChildAt(mouseX, mouseY)
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.enableScissor(x, y, x + (width - getReservedRight() atLeast 0), y + (height - getReservedBottom() atLeast 0))
        try {
            widgetRegistrations.forEach { widgetRegistration ->
                widgetRegistration.widget.render(guiGraphics, mouseX, mouseY, partialTick)
            }
        } finally {
            guiGraphics.disableScissor()
        }
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (!isActive) return
        if (!containsViewport(mouseX, mouseY)) {
            // TODO ボタンごとの押下先の導入後は、領域外でも各押下先へ実座標を中継するのだ～🌱
            if (isDragging) focused?.mouseMoved(mouseX, mouseY)
            return
        }
        children().forEach {
            it.mouseMoved(mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isActive) return false
        if (!containsViewport(mouseX, mouseY)) return false
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!isActive) return false
        if (!containsViewport(mouseX, mouseY)) return false
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }


    // 読み上げなのだ～🌱

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        // TODO 子 Widget の読み上げを中継するのだ～🌱
    }


    // 子 View の配置なのだ～🌱

    private val childController: ViewController

    init {
        val childContext: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = context.origin
            override fun addRenderableWidget(widget: AbstractWidget) = register(widget, true)
            override fun addRenderableOnly(widget: AbstractWidget) = register(widget, false)
        }
        childController = view.childView.attachTo(IntPoint(x, y)) { childView, childBounds ->
            // TODO 後続のテストでは Slot の配置拒否を検証し、初期化失敗時の巻き戻しは要求しないのだ～🌱
            // TODO スクロール領域内の MenuSlotView の配置に対応するのだ～🌱
            if (childView is MenuSlotView) throw UnsupportedOperationException("MenuSlotView cannot be placed inside a scroll view")
            registry.place(childContext, childView, childBounds)
        }
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
    context,
    registry,
) {

    // 領域とサイズなのだ～🌱

    override fun getReservedRight() = 0
    override fun getReservedBottom() = view.scrollbarThickness

    override val viewportLength get() = width
    override fun evaluateContentLength() = view.childView.evaluateActualSizeX()


    // スクロール操作なのだ～🌱

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
    context,
    registry,
) {

    // 領域とサイズなのだ～🌱

    override fun getReservedRight() = view.scrollbarThickness
    override fun getReservedBottom() = 0

    override val viewportLength get() = height
    override fun evaluateContentLength() = view.childView.evaluateActualSizeY()


    // スクロール操作なのだ～🌱

    // TODO Y 軸の残りのスクロール操作を実装するのだ～🌱

}
