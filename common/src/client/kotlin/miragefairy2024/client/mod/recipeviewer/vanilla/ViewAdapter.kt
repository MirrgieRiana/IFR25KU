package miragefairy2024.client.mod.recipeviewer.vanilla

import dev.architectury.platform.Platform
import miragefairy2024.client.util.getActualFocused
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import miragefairy2024.util.fire
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.MultiLineEditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.inventory.Slot
import org.lwjgl.glfw.GLFW

/** [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter] から画面の機能を呼び出すためのインターフェースなのだ～🌱 */
interface ViewAdapterHost {

    fun getRenderingProxy(): RenderingProxy
    fun getViewPlacerRegistry(): ViewPlacerRegistry<VanillaViewPlacerContext>

    fun getScreen(): Screen?
    fun canPlaceMenuSlots(): Boolean
    fun getRegionSizeX(): Int
    fun getRegionSizeY(): Int

    fun createRootView(): View

    fun getUiOffset(): IntPoint
    fun setUiSize(uiSize: IntPoint)

    fun init()

    fun <T> addWidget(widget: T): Remover? where T : GuiEventListener, T : NarratableEntry = null
    fun addRenderableWidget(widget: AbstractWidget): Remover
    fun addRenderableOnly(widget: AbstractWidget): Remover
    fun registerMenuSlotPlacement(slot: Slot, clipBounds: () -> IntRectangle?): Remover

}

/**
 * View のサイズ計算と配置を登録先へ中継し、ホストの入力処理を補正するのだ～🌱
 * ホストを初期化するときは [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter.init] を呼ぶのだ～🌱
 * ホストが表示終了を通知できる場合は、最後の描画を終えてから [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter.removed] を呼ぶのだ～🌱
 */
class ViewAdapter(val host: ViewAdapterHost) {

    private var isInitialized = false
    private var viewController: ViewController? = null
    private val onBeforeRender = EventRegistry<() -> Unit>()

    private class MouseClickSentinel : GuiEventListener, NarratableEntry {

        var clicked = false

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            clicked = true
            // true にすると、番兵がフォーカスを受け取っちゃう上に、スロット処理も飛ばされちゃうのだ～🌱
            return false
        }

        override fun isFocused() = false
        override fun setFocused(focused: Boolean) = Unit
        override fun isActive() = false
        override fun narrationPriority(): NarratableEntry.NarrationPriority = NarratableEntry.NarrationPriority.NONE
        override fun updateNarration(narrationElementOutput: NarrationElementOutput) = Unit

    }

    private val mouseClickSentinel = MouseClickSentinel()
    private var mouseClickSentinelRemover: Remover? = null

    /**
     * View のツリーを再構築して、各ウィジェットをホストへ直接登録するのだ～🌱
     *
     * 再初期化時は、既存のウィジェットをホスト側で登録解除した後に呼び出さなければならないのだ～🌱
     */
    fun init() {
        removed()
        val rootView = host.createRootView()
        rootView.initSizeCalculation(host.getRenderingProxy(), { host.getRegionSizeX() }, { host.getRegionSizeY() })
        host.setUiSize(rootView.evaluateActualSize())
        host.init()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = host.getUiOffset()
            override val canPlaceMenuSlots = host.canPlaceMenuSlots()
            override val onBeforeRender = this@ViewAdapter.onBeforeRender
            override fun addRenderableWidget(widget: AbstractWidget) = host.addRenderableWidget(widget)
            override fun addRenderableOnly(widget: AbstractWidget) = host.addRenderableOnly(widget)
            override fun registerMenuSlotPlacement(slot: Slot, clipBounds: () -> IntRectangle?) = host.registerMenuSlotPlacement(slot, clipBounds)
        }
        viewController = rootView.attachTo(host.getUiOffset()) { view, bounds ->
            host.getViewPlacerRegistry().place(context, view, bounds)
        }
        isInitialized = true
    }

    /** このルートのウィジェットを描き始める前に呼ぶのだ～🌱 */
    fun beforeRender() {
        onBeforeRender.fire()
    }

    /** 子の相対位置・寸法・登録を維持して移動するのだ～🌱 */
    fun setOffset(offset: IntPoint) {
        val controller = checkNotNull(viewController) { "ViewAdapter has not been initialized or has been removed" }
        controller.setOffset(offset)
    }

    /** 配置状態を無効化してから、イベント登録などの後始末を行うのだ～🌱 */
    fun removed() {
        isInitialized = false
        removeMouseClickSentinel()
        val controller = viewController
        viewController = null
        onBeforeRender.toList().forEach {
            onBeforeRender.remove(it)
        }
        if (controller != null) controller.dispose()
    }

    fun removeWidget(listener: GuiEventListener) {
        val screen = host.getScreen() ?: return
        if (screen.focused === listener && screen.children().none { it === listener }) {
            screen.focused = null
            screen.isDragging = false
        }
    }

    private fun removeMouseClickSentinel() {
        val remover = mouseClickSentinelRemover ?: return
        mouseClickSentinelRemover = null
        remover.remove()
    }

    fun beforeMouseClicked() {
        val screen = host.getScreen() ?: return
        mouseClickSentinel.clicked = false
        if (mouseClickSentinelRemover == null) {
            mouseClickSentinelRemover = host.addWidget(mouseClickSentinel)
        } else {
            if (screen.children().lastOrNull() !== mouseClickSentinel) {
                removeMouseClickSentinel()
                mouseClickSentinelRemover = host.addWidget(mouseClickSentinel)
            }
        }
    }

    fun mouseClicked(mouseX: Double, mouseY: Double) {
        val screen = host.getScreen() ?: return
        val focused = screen.focused ?: return

        // フォーカスされた子が除去済みの場合は、フォーカスを解除するのだ～🌱
        if (screen.children().none { it === focused }) {
            screen.focused = null
            screen.isDragging = false
            return
        }

        // 何かがフォーカスされていて、かつ番兵がクリックを受けた場合は、フォーカスを解除するのだ～🌱
        if (mouseClickSentinel.clicked) {

            // クリックが発生した位置が実際にフォーカスされている要素の領域内だった場合はフォーカス解除を免除するのだ～🌱
            val hoveredActualFocused = focused.getActualFocused { container ->
                // クリックされた位置が親要素の範囲外なのだ～🌱
                if (container is AbstractWidget && !container.isMouseOver(mouseX, mouseY)) return@getActualFocused false
                // フォーカスされているのが親要素自身なのだ～🌱
                if (container.focused == null) return@getActualFocused true
                // クリックされた位置が親要素によってトリムされた範囲外なら、弾くのだ～🌱
                if (!container.getChildAt(mouseX, mouseY).isPresent) return@getActualFocused false
                true
            }
            if (hoveredActualFocused != null && hoveredActualFocused.isMouseOver(mouseX, mouseY)) return

            screen.clearFocus()
            screen.isDragging = false
        }

    }

    private fun getMouseCapture(screen: Screen, button: Int): GuiEventListener? {
        val focused = screen.focused
        if (focused is MouseCaptureContainer && focused.hasMouseCapture(button)) return focused
        return screen.children().firstOrNull { it is MouseCaptureContainer && it.hasMouseCapture(button) }
    }

    fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!isInitialized) return false
        val screen = host.getScreen() ?: return false
        return screen.children().toList().any { child ->
            if (screen.children().none { it === child }) return@any false
            child.isMouseOver(mouseX, mouseY) && child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        }
    }

    /**
     * Fabric 以外ではフォーカスされた子へドラッグを中継するのだ～🌱
     * [miragefairy2024.client.mod.recipeviewer.vanilla.MouseCaptureContainer] が保持する押下先へは、プラットフォームによらず中継するのだ～🌱
     * それ以外の Fabric のドラッグ中継は、ホスト側の既存の入力経路に任せるのだ～🌱
     */
    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!isInitialized) return false
        val screen = host.getScreen() ?: return false
        val capture = getMouseCapture(screen, button)
        if (capture != null) return capture.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        if (Platform.isFabric()) return false
        val focused = screen.focused
        if (focused == null) return false
        if (button == 0 && screen.isDragging) {
            return focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        }
        return false
    }

    // 押下先を保持する子を優先し、それ以外の解放は Fabric API または画面の既存経路へ渡すのだ～🌱
    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isInitialized) return false
        val screen = host.getScreen() ?: return false
        val capture = getMouseCapture(screen, button)
        if (capture != null) {
            if (button == 0) screen.isDragging = false
            return capture.mouseReleased(mouseX, mouseY, button)
        }
        if (Platform.isFabric()) return false
        // isDragging は左クリックでのみオンになるのだ～🌱
        if (button == 0 && screen.isDragging) {
            screen.isDragging = false
            val focused = screen.focused
            if (focused != null) return focused.mouseReleased(mouseX, mouseY, button)
        }
        val listener = screen.getChildAt(mouseX, mouseY).orNull ?: return false
        return listener.mouseReleased(mouseX, mouseY, button)
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isInitialized) return false
        val screen = host.getScreen() ?: return false
        val focused = screen.focused ?: return false
        val actualFocused = focused.getActualFocused { it !is AbstractWidget || it.isActive } ?: return false
        if (actualFocused is EditBox || actualFocused is MultiLineEditBox) {
            if (focused.keyPressed(keyCode, scanCode, modifiers)) return true
            // 通常の文字入力は charTyped で処理されるため、keyPressed で未処理でもコンテナーのショートカットへ渡さないのだ～🌱
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != GLFW.GLFW_KEY_TAB) {
                if (actualFocused is EditBox && actualFocused.canConsumeInput()) return true
                if (actualFocused is MultiLineEditBox && actualFocused.isActive && actualFocused.isFocused) return true
            }
        }
        return false
    }

}
