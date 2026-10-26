package miragefairy2024.client.mod.recipeviewer.vanilla

import dev.architectury.platform.Platform
import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.util.Remover
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

    private var viewController: ViewController? = null

    /**
     * View のツリーを再構築して、各ウィジェットを画面へ直接登録するのだ～🌱
     * 親クラスは、画面の寸法が変わる度に、ウィジェットを捨ててからこれを呼ぶのだ～🌱
     */
    override fun init() = init2()

    override fun removed() {
        removed2()
        super.removed()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun removeWidget(listener: GuiEventListener) {
        super.removeWidget(listener)
        removeWidget2(listener)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val handled = super.mouseClicked(mouseX, mouseY, button)
        mouseClicked2()
        return handled
    }

    /**
     * Fabric 以外ではフォーカスされた子へドラッグを中継してから、スロットの操作へ渡すのだ～🌱
     * Fabric では Fabric API が [net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.mouseDragged] 内で子へ転送するため、手動では中継しないのだ～🌱
     */
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (mouseDragged2(mouseX, mouseY, button, dragX, dragY)) return true
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyPressed2(keyCode, scanCode, modifiers)) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名とインベントリの名前もViewのツリーが描くのだ～🌱 */
    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

    private fun init2() {
        removed2()
        val rootView = menu.createView(title, playerInventoryTitle)
        rootView.initSizeCalculation(renderingProxy, { width }, { height })
        imageWidth = rootView.evaluateActualSizeX()
        imageHeight = rootView.evaluateActualSizeY()
        super.init()

        fun register(widget: AbstractWidget, add: () -> Unit): Remover {
            add()
            var registered = true
            return Remover {
                if (registered) {
                    registered = false
                    removeWidget(widget)
                }
            }
        }

        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(leftPos, topPos)

            override fun addRenderableWidget(widget: AbstractWidget) = register(widget) { this@ViewContainerScreen.addRenderableWidget(widget) }
            override fun addRenderableOnly(widget: AbstractWidget) = register(widget) { this@ViewContainerScreen.addRenderableOnly(widget) }
        }
        viewController = rootView.attachTo(IntPoint(leftPos, topPos)) { view, bounds ->
            VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds)
        }
    }

    private fun removed2() {
        val controller = viewController
        viewController = null
        if (controller != null) controller.dispose()
    }

    private fun removeWidget2(listener: GuiEventListener) {
        if (focused === listener && children().none { it === listener }) {
            focused = null
            isDragging = false
        }
    }

    private fun mouseClicked2() {
        val focused = focused
        if (focused != null && children().none { it === focused }) {
            this.focused = null
            isDragging = false
        }
    }

    private fun mouseDragged2(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!Platform.isFabric()) {
            val focused = focused
            if (focused != null) {
                if (isDragging) {
                    if (button == 0) {
                        if (focused.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true
                    }
                }
            }
        }
        return false
    }

    private fun keyPressed2(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val focused = focused
        if (focused is EditBox) {
            if (focused.keyPressed(keyCode, scanCode, modifiers)) return true
            // 通常の文字入力は charTyped で処理されるため、keyPressed で未処理でもコンテナーのショートカットへ渡さないのだ～🌱
            if (keyCode != GLFW.GLFW_KEY_ESCAPE && focused.canConsumeInput()) return true
        }
        return false
    }

}
