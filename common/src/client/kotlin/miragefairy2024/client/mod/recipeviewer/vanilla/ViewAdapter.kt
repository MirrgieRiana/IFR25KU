package miragefairy2024.client.mod.recipeviewer.vanilla

import dev.architectury.platform.Platform
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.Remover
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.glfw.GLFW

/** [miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter] から画面の機能を呼び出すためのインターフェースなのだ～🌱 */
interface ViewAdapterHost {

    fun getRenderingProxy(): RenderingProxy
    fun getViewPlacerRegistry(): ViewPlacerRegistry<VanillaViewPlacerContext>

    fun getScreen(): Screen

    fun createRootView(): View

    fun getLeftPos(): Int
    fun getTopPos(): Int
    fun setImageWidth(imageWidth: Int)
    fun setImageHeight(imageHeight: Int)

    fun init()

    fun addRenderableWidget(widget: AbstractWidget)
    fun addRenderableOnly(widget: AbstractWidget)
    fun removeWidget(listener: GuiEventListener)

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
        rootView.initSizeCalculation(host.getRenderingProxy(), { host.getScreen().width }, { host.getScreen().height })
        host.setImageWidth(rootView.evaluateActualSizeX())
        host.setImageHeight(rootView.evaluateActualSizeY())
        host.init()

        fun register(widget: AbstractWidget, add: () -> Unit): Remover {
            add()
            var registered = true
            return Remover {
                if (registered) {
                    registered = false
                    host.removeWidget(widget)
                }
            }
        }

        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(host.getLeftPos(), host.getTopPos())
            override fun getClipBounds(): IntRectangle? = null

            override fun addRenderableWidget(widget: AbstractWidget) = register(widget) { host.addRenderableWidget(widget) }
            override fun addRenderableOnly(widget: AbstractWidget) = register(widget) { host.addRenderableOnly(widget) }
        }
        viewController = rootView.attachTo(IntPoint(host.getLeftPos(), host.getTopPos())) { view, bounds ->
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
        if (host.getScreen().focused === listener && host.getScreen().children().none { it === listener }) {
            host.getScreen().focused = null
            host.getScreen().isDragging = false
        }
    }

    fun mouseClicked() {
        val focused = host.getScreen().focused
        if (focused != null && host.getScreen().children().none { it === focused }) {
            host.getScreen().focused = null
            host.getScreen().isDragging = false
        }
    }

    /**
     * Fabric 以外ではフォーカスされた子へドラッグを中継するのだ～🌱
     * Fabric では Fabric API が [net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.mouseDragged] 内で子へ転送するため、手動では中継しないのだ～🌱
     */
    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!isInitialized) return false
        if (Platform.isFabric()) return false
        val focused = host.getScreen().focused
        if (focused == null) return false
        if (button == 0 && host.getScreen().isDragging) {
            return focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        }
        return false
    }

    // Fabric 以外では、子へのドラッグ終了の通知も中継するのだ～🌱
    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isInitialized) return false
        if (Platform.isFabric()) return false
        // isDragging は左クリックでのみオンになるのだ～🌱
        if (button == 0 && host.getScreen().isDragging) {
            host.getScreen().isDragging = false
            val focused = host.getScreen().focused
            if (focused != null) return focused.mouseReleased(mouseX, mouseY, button)
        }
        val listener = host.getScreen().getChildAt(mouseX, mouseY).orNull ?: return false
        return listener.mouseReleased(mouseX, mouseY, button)
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isInitialized) return false
        val focused = host.getScreen().focused
        if (focused is EditBox) {
            if (focused.keyPressed(keyCode, scanCode, modifiers)) return true
            // 通常の文字入力は charTyped で処理されるため、keyPressed で未処理でもコンテナーのショートカットへ渡さないのだ～🌱
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && focused.canConsumeInput()) return true
        }
        return false
    }

}
