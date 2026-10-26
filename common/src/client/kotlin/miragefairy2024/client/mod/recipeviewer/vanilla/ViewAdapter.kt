package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.view.ContextViewPlacer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/**
 * Screen のインスタンスを保持せず、View の配置・描画・入力・フォーカスを管理するのだ～🌱
 * 画面では 1 個のウィジェットとして登録でき、画面以外では [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter.render] を直接呼べるのだ～🌱
 * ホストを破棄・再初期化するときは [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter.dispose] を呼ぶのだ～🌱
 * ツールチップは、現在の Screen の描画パスを使用するのだ～🌱
 *
 * @param regionSize View のサイズ計算に使う利用可能領域なのだ～🌱
 * @param origin 初期配置の絶対座標で、メニュースロットのホストが加算する原点でもあるのだ～🌱
 */
class ViewAdapter(rootView: View, regionSize: IntPoint, origin: IntPoint, renderingProxy: RenderingProxy, viewPlacer: ContextViewPlacer<VanillaViewPlacerContext, PlaceableView>) : AbstractContainerEventHandler(), Renderable, NarratableEntry {
    constructor(rootView: View, regionSize: IntPoint, origin: IntPoint) : this(rootView, regionSize, origin, renderingProxy, ContextViewPlacer { context, view, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds) })

    private class WidgetSlot(val widget: AbstractWidget, val interactive: Boolean) {
        var registered = true
    }

    private var widgetSlots = listOf<WidgetSlot>()
    private var interactiveWidgets = listOf<AbstractWidget>()
    private var viewController: ViewController? = null

    val size: IntPoint
    var offset = origin
        private set

    init {
        rootView.initSizeCalculation(renderingProxy, { regionSize.x }, { regionSize.y })
        size = rootView.evaluateActualSize()
        val context = VanillaViewPlacerContext(
            addRenderableWidget = { register(it, true) },
            addRenderableOnly = { register(it, false) },
            origin = origin,
        )
        viewController = rootView.attachTo(origin) { view, bounds -> viewPlacer.place(context, view, bounds) }
    }

    private fun register(widget: AbstractWidget, interactive: Boolean): Remover {
        val slot = WidgetSlot(widget, interactive)
        widgetSlots = widgetSlots + slot
        interactiveWidgets = widgetSlots.filter { it.interactive }.map { it.widget }
        return Remover {
            slot.registered = false
            widgetSlots = widgetSlots - slot
            interactiveWidgets = widgetSlots.filter { it.interactive }.map { it.widget }
            if (super.getFocused() === widget && interactiveWidgets.none { it === widget }) focused = null
        }
    }

    override fun children() = interactiveWidgets

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        widgetSlots.forEach {
            if (it.registered) it.widget.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double) = interactiveWidgets.any { it.isMouseOver(mouseX, mouseY) }
    override fun getRectangle(): ScreenRectangle = ScreenRectangle(offset.x, offset.y, size.x, size.y)

    /** 子の相対位置・寸法・登録を維持して移動するのだ～🌱 */
    fun setOffset(offset: IntPoint) {
        val controller = checkNotNull(viewController) { "ViewAdapter has been disposed" }
        controller.setOffset(offset)
        this.offset = offset
    }

    override fun setFocused(focused: GuiEventListener?) {
        super.setFocused(focused?.takeIf { child -> interactiveWidgets.any { it === child } })
        if (super.getFocused() == null) isDragging = false
    }

    override fun getFocused(): GuiEventListener? = super.getFocused()?.takeIf { it.isFocused }

    override fun setFocused(focused: Boolean) {
        // 親が同じアダプターを再フォーカスする際にも、選択した子を維持するのだ～🌱
        super.getFocused()?.isFocused = focused
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return widgetSlots.any {
            if (it.registered && it.interactive && it.widget.mouseClicked(mouseX, mouseY, button)) {
                // クリック処理中に削除された子は、フォーカスとドラッグの対象に戻さないのだ～🌱
                focused = it.widget
                if (button == 0) isDragging = focused != null
                true
            } else {
                false
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val focused = focused
        if (focused != null && focused.keyPressed(keyCode, scanCode, modifiers)) return true
        // 文字入力中は、未処理のキーをコンテナーのショートカットへ渡さないのだ～🌱
        return focused is EditBox && keyCode != GLFW.GLFW_KEY_ESCAPE && focused.canConsumeInput()
    }

    override fun narrationPriority(): NarratableEntry.NarrationPriority = interactiveWidgets.filter { it.isActive }.maxOfOrNull { it.narrationPriority() } ?: NarratableEntry.NarrationPriority.NONE

    override fun updateNarration(output: NarrationElementOutput) {
        val widgets = interactiveWidgets.filter { it.isActive }.sortedBy { it.tabOrderGroup }
        val widget = widgets.maxByOrNull { it.narrationPriority() } ?: return
        if (widget.narrationPriority() == NarratableEntry.NarrationPriority.NONE) return
        if (widgets.size > 1) output.add(NarratedElementType.POSITION, Component.translatable("narrator.position.screen", widgets.indexOf(widget) + 1, widgets.size))
        widget.updateNarration(output.nest())
    }

    /** イベント購読と全ウィジェットの登録を解除するのだ～🌱 */
    fun dispose() {
        val controller = viewController ?: return
        viewController = null
        controller.dispose()
        focused = null
        widgetSlots.forEach {
            it.registered = false
        }
        widgetSlots = emptyList()
        interactiveWidgets = emptyList()
    }
}
