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
import org.lwjgl.glfw.GLFW

interface ViewAdapterHost {

    fun createRootView(): View

    fun getViewController(): ViewController?
    fun setViewController(viewController: ViewController?)

    fun getWidth(): Int
    fun getHeight(): Int
    fun getLeftPos(): Int
    fun getTopPos(): Int
    fun setImageWidth(imageWidth: Int)
    fun setImageHeight(imageHeight: Int)

    fun getChildren(): List<GuiEventListener>
    fun getFocused(): GuiEventListener?
    fun setFocused(focused: GuiEventListener?)
    fun getIsDragging(): Boolean
    fun setIsDragging(isDragging: Boolean)

    fun init()

    fun addRenderableWidget(widget: AbstractWidget)
    fun addRenderableOnly(widget: AbstractWidget)
    fun removeWidget(listener: GuiEventListener)

}

class ViewAdapter(val host: ViewAdapterHost) {

    fun init() {
        removed()
        val rootView = host.createRootView()
        rootView.initSizeCalculation(renderingProxy, { host.getWidth() }, { host.getHeight() })
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
        host.setViewController(rootView.attachTo(IntPoint(host.getLeftPos(), host.getTopPos())) { view, bounds ->
            VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds)
        })
    }

    fun removed() {
        val controller = host.getViewController()
        host.setViewController(null)
        if (controller != null) controller.dispose()
    }

    fun removeWidget(listener: GuiEventListener) {
        if (host.getFocused() === listener && host.getChildren().none { it === listener }) {
            host.setFocused(null)
            host.setIsDragging(false)
        }
    }

    fun mouseClicked() {
        val focused = host.getFocused()
        if (focused != null && host.getChildren().none { it === focused }) {
            host.setFocused(null)
            host.setIsDragging(false)
        }
    }

    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!Platform.isFabric()) {
            val focused = host.getFocused()
            if (focused != null) {
                if (host.getIsDragging()) {
                    if (button == 0) {
                        if (focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true
                    }
                }
            }
        }
        return false
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val focused = host.getFocused()
        if (focused is EditBox) {
            if (focused.keyPressed(keyCode, scanCode, modifiers)) return true
            // 通常の文字入力は charTyped で処理されるため、keyPressed で未処理でもコンテナーのショートカットへ渡さないのだ～🌱
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && focused.canConsumeInput()) return true
        }
        return false
    }

}
