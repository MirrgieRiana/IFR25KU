package miragefairy2024.client.mod.recipeviewer.vanilla

import dev.architectury.platform.Platform
import miragefairy2024.client.util.findFocusedLeaf
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.util.Remover
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.MultiLineEditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.glfw.GLFW

/** [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter] から画面の機能を呼び出すためのインターフェースなのだ～🌱 */
interface ViewAdapterHost {

    fun getRenderingProxy(): RenderingProxy
    fun getViewPlacerRegistry(): ViewPlacerRegistry<VanillaViewPlacerContext>

    fun getScreen(): Screen?
    fun getRegionSizeX(): Int
    fun getRegionSizeY(): Int

    fun createRootView(): View

    fun getUiOffset(): IntPoint
    fun setUiSize(uiSize: IntPoint)

    fun init()

    fun addRenderableWidget(widget: AbstractWidget): Remover
    fun addRenderableOnly(widget: AbstractWidget): Remover

}

/**
 * View のサイズ計算と配置を登録先へ中継し、ホストの入力処理を補正するのだ～🌱
 * ホストを初期化するときは [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter.init] を呼ぶのだ～🌱
 * ホストを破棄するときは [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter.removed] を呼ぶのだ～🌱
 */
class ViewAdapter(val host: ViewAdapterHost) {

    private var isInitialized = false
    private var viewController: ViewController? = null

    /**
     * View のツリーを再構築して、各ウィジェットをホストへ直接登録するのだ～🌱
     * 画面の再構築時には、[net.minecraft.client.gui.screens.Screen.rebuildWidgets] が既存のウィジェットを破棄してから、[miragefairy2024.client.mod.recipeviewer.vanilla.ViewContainerScreen.init] を経由して呼び出すのだ～🌱
     */
    fun init() {
        removed()
        val rootView = host.createRootView()
        rootView.initSizeCalculation(host.getRenderingProxy(), { host.getRegionSizeX() }, { host.getRegionSizeY() })
        host.setUiSize(rootView.evaluateActualSize())
        host.init()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = host.getUiOffset()
            override fun addRenderableWidget(widget: AbstractWidget) = host.addRenderableWidget(widget)
            override fun addRenderableOnly(widget: AbstractWidget) = host.addRenderableOnly(widget)
        }
        viewController = rootView.attachTo(host.getUiOffset()) { view, bounds ->
            host.getViewPlacerRegistry().place(context, view, bounds)
        }
        isInitialized = true
    }

    /** 子の相対位置・寸法・登録を維持して移動するのだ～🌱 */
    fun setOffset(offset: IntPoint) {
        val controller = checkNotNull(viewController) { "ViewAdapter has not been initialized or has been removed" }
        controller.setOffset(offset)
    }

    /** 配置状態を無効化してから、イベント登録などの後始末を行うのだ～🌱 */
    fun removed() {
        isInitialized = false
        val controller = viewController
        viewController = null
        if (controller != null) controller.dispose()
    }

    fun removeWidget(listener: GuiEventListener) {
        val screen = host.getScreen() ?: return
        if (screen.focused === listener && screen.children().none { it === listener }) {
            screen.focused = null
            screen.isDragging = false
        }
    }

    fun mouseClicked() {
        val screen = host.getScreen() ?: return
        val focused = screen.focused
        if (focused != null && screen.children().none { it === focused }) {
            screen.focused = null
            screen.isDragging = false
        }
    }

    /**
     * Fabric 以外ではフォーカスされた子へドラッグを中継するのだ～🌱
     * Fabric では Fabric API が [net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.mouseDragged] 内で子へ転送するため、手動では中継しないのだ～🌱
     */
    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!isInitialized) return false
        val screen = host.getScreen() ?: return false
        if (Platform.isFabric()) return false
        val focused = screen.focused
        if (focused == null) return false
        if (button == 0 && screen.isDragging) {
            return focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        }
        return false
    }

    // Fabric 以外では、子へのドラッグ終了の通知も中継するのだ～🌱
    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isInitialized) return false
        val screen = host.getScreen() ?: return false
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
        val listener = focused.findFocusedLeaf { it !is AbstractWidget || it.isActive } ?: return false
        if (listener is EditBox || listener is MultiLineEditBox) {
            if (focused.keyPressed(keyCode, scanCode, modifiers)) return true
            // 通常の文字入力は charTyped で処理されるため、keyPressed で未処理でもコンテナーのショートカットへ渡さないのだ～🌱
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                if (listener is EditBox && listener.canConsumeInput()) return true
                if (listener is MultiLineEditBox && listener.isActive && listener.isFocused) return true
            }
        }
        return false
    }

}
