package miragefairy2024.client.mod.recipeviewer.vanilla

import dev.architectury.platform.Platform
import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.glfw.GLFW

interface ViewAdapterHost {

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

class ViewAdapter(val host: ViewAdapterHost) {

    private var viewController: ViewController? = null

    fun init() {
        removed()
        val rootView = host.createRootView()
        rootView.initSizeCalculation(renderingProxy, { host.getScreen().width }, { host.getScreen().height })
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

            override fun addRenderableWidget(widget: AbstractWidget) = register(widget) { host.addRenderableWidget(widget) }
            override fun addRenderableOnly(widget: AbstractWidget) = register(widget) { host.addRenderableOnly(widget) }
        }
        viewController = rootView.attachTo(IntPoint(host.getLeftPos(), host.getTopPos())) { view, bounds ->
            VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds)
        }
    }

    fun removed() {
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

    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!Platform.isFabric()) {
            val focused = host.getScreen().focused
            if (focused != null) {
                if (host.getScreen().isDragging) {
                    if (button == 0) {
                        if (focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true
                    }
                }
            }
        }
        return false
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val focused = host.getScreen().focused
        if (focused is EditBox) {
            if (focused.keyPressed(keyCode, scanCode, modifiers)) return true
            // 通常の文字入力は charTyped で処理されるため、keyPressed で未処理でもコンテナーのショートカットへ渡さないのだ～🌱
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && focused.canConsumeInput()) return true
        }
        return false
    }

}
