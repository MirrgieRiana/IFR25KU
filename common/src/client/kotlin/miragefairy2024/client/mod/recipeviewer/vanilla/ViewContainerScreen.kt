package miragefairy2024.client.mod.recipeviewer.vanilla

import dev.architectury.platform.Platform
import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.util.Remover
import mirrg.kotlin.helium.unit
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import org.lwjgl.glfw.GLFW

/** 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱 */
class ViewContainerScreen<T>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) where T : AbstractContainerMenu, T : ViewProviderMenu {

    private val viewAdapter = ViewAdapter(object: ViewAdapterHost {

        override fun createRootView() = menu.createView(title, playerInventoryTitle)

        override fun getViewController() = this@ViewContainerScreen.viewController
        override fun setViewController(viewController: ViewController?) = run { this@ViewContainerScreen.viewController = viewController }

        override fun getWidth() = this@ViewContainerScreen.width
        override fun getHeight() = this@ViewContainerScreen.height
        override fun getLeftPos() = this@ViewContainerScreen.leftPos
        override fun getTopPos() = this@ViewContainerScreen.topPos
        override fun setImageWidth(imageWidth: Int) = run { this@ViewContainerScreen.imageWidth = imageWidth }
        override fun setImageHeight(imageHeight: Int) = run { this@ViewContainerScreen.imageHeight = imageHeight }

        override fun getChildren() = this@ViewContainerScreen.children()
        override fun getFocused() = this@ViewContainerScreen.focused
        override fun setFocused(focused: GuiEventListener?) = run { this@ViewContainerScreen.focused = focused }
        override fun getIsDragging() = this@ViewContainerScreen.isDragging
        override fun setIsDragging(isDragging: Boolean) = run { this@ViewContainerScreen.isDragging = isDragging }

        override fun init() = super@ViewContainerScreen.init()

        override fun addRenderableWidget(widget: AbstractWidget) = unit { this@ViewContainerScreen.addRenderableWidget(widget) }
        override fun addRenderableOnly(widget: AbstractWidget) = unit { this@ViewContainerScreen.addRenderableOnly(widget) }
        override fun removeWidget(listener: GuiEventListener) = this@ViewContainerScreen.removeWidget(listener)

    })

    private var viewController: ViewController? = null

    /**
     * View のツリーを再構築して、各ウィジェットを画面へ直接登録するのだ～🌱
     * 親クラスは、画面の寸法が変わる度に、ウィジェットを捨ててからこれを呼ぶのだ～🌱
     */
    override fun init() = viewAdapter.init2()

    override fun removed() {
        viewAdapter.removed2()
        super.removed()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun removeWidget(listener: GuiEventListener) {
        super.removeWidget(listener)
        viewAdapter.removeWidget2(listener)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val handled = super.mouseClicked(mouseX, mouseY, button)
        viewAdapter.mouseClicked2()
        return handled
    }

    /**
     * Fabric 以外ではフォーカスされた子へドラッグを中継してから、スロットの操作へ渡すのだ～🌱
     * Fabric では Fabric API が [net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.mouseDragged] 内で子へ転送するため、手動では中継しないのだ～🌱
     */
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (viewAdapter.mouseDragged2(mouseX, mouseY, button, dragX, dragY)) return true
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (viewAdapter.keyPressed2(keyCode, scanCode, modifiers)) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名とインベントリの名前もViewのツリーが描くのだ～🌱 */
    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

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

    fun init2() {
        removed2()
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

    fun removed2() {
        val controller = host.getViewController()
        host.setViewController(null)
        if (controller != null) controller.dispose()
    }

    fun removeWidget2(listener: GuiEventListener) {
        if (host.getFocused() === listener && host.getChildren().none { it === listener }) {
            host.setFocused(null)
            host.setIsDragging(false)
        }
    }

    fun mouseClicked2() {
        val focused = host.getFocused()
        if (focused != null && host.getChildren().none { it === focused }) {
            host.setFocused(null)
            host.setIsDragging(false)
        }
    }

    fun mouseDragged2(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
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

     fun keyPressed2(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val focused = host.getFocused()
        if (focused is EditBox) {
            if (focused.keyPressed(keyCode, scanCode, modifiers)) return true
            // 通常の文字入力は charTyped で処理されるため、keyPressed で未処理でもコンテナーのショートカットへ渡さないのだ～🌱
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && focused.canConsumeInput()) return true
        }
        return false
    }

    }

}
