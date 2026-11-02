package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

/** 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱 */
class ViewContainerScreen<T>(menu: T, playerInventory: Inventory, title: Component) : AbstractViewContainerScreen<T>(menu, playerInventory, title) where T : AbstractContainerMenu, T : ViewProviderMenu {
    override fun createRootView() = menu.createView(title, playerInventoryTitle)
}

/** View のツリーを画面側で構築するコンテナー画面の基底クラスなのだ～🌱 */
abstract class AbstractViewContainerScreen<T : AbstractContainerMenu>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) {

    protected abstract fun createRootView(): View

    private val viewAdapter = ViewAdapter(object : ViewAdapterHost {

        override fun getRenderingProxy() = renderingProxy
        override fun getViewPlacerRegistry() = VANILLA_VIEW_PLACER_REGISTRY

        override fun getScreen() = this@AbstractViewContainerScreen
        override fun getRegionSizeX() = this@AbstractViewContainerScreen.width
        override fun getRegionSizeY() = this@AbstractViewContainerScreen.height

        override fun createRootView() = this@AbstractViewContainerScreen.createRootView()

        override fun getUiOffset() = IntPoint(this@AbstractViewContainerScreen.leftPos, this@AbstractViewContainerScreen.topPos)
        override fun setUiSize(uiSize: IntPoint) {
            this@AbstractViewContainerScreen.imageWidth = uiSize.x
            this@AbstractViewContainerScreen.imageHeight = uiSize.y
        }

        override fun init() = super@AbstractViewContainerScreen.init()

        override fun addRenderableWidget(widget: AbstractWidget): Remover {
            this@AbstractViewContainerScreen.addRenderableWidget(widget)
            return Remover { this@AbstractViewContainerScreen.removeWidget(widget) }
        }

        override fun addRenderableOnly(widget: AbstractWidget): Remover {
            this@AbstractViewContainerScreen.addRenderableOnly(widget)
            return Remover { this@AbstractViewContainerScreen.removeWidget(widget) }
        }

    })

    override fun init() = viewAdapter.init()

    override fun removed() {
        viewAdapter.removed()
        super.removed()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun removeWidget(listener: GuiEventListener) {
        super.removeWidget(listener)
        viewAdapter.removeWidget(listener)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val handled = super.mouseClicked(mouseX, mouseY, button)
        viewAdapter.mouseClicked()
        return handled
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (viewAdapter.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (viewAdapter.mouseReleased(mouseX, mouseY, button)) return true
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (viewAdapter.keyPressed(keyCode, scanCode, modifiers)) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名とインベントリの名前もViewのツリーが描くのだ～🌱 */
    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

}
