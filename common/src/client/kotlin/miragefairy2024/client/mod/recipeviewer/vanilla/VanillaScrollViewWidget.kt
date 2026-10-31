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
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

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

    override fun children() = widgetRegistrations.filter { it.interactive }.map { it.widget }


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

    override fun isActive() = super.isActive() && !disposed

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // TODO 右と下の予約領域を除いた幅と高さを 0 以上に制限し、Widget の座標と合わせて子の描画をクリップするのだ～🌱
    }

    // TODO 子へのクリック・ホイールの中継と対象探索は、右と下の予約領域を除いた表示領域内に制限するのだ～🌱


    // 読み上げなのだ～🌱

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
