package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import mirrg.kotlin.helium.unit
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

/** 中身をViewのツリーで組むメニューの画面の土台なのだ～🌱 */
class ViewContainerScreen<T>(menu: T, playerInventory: Inventory, title: Component) : AbstractContainerScreen<T>(menu, playerInventory, title) where T : AbstractContainerMenu, T : ViewProviderMenu {

    private val viewAdapter = ViewAdapter(object : ViewAdapterHost {

        override fun getRenderingProxy() = renderingProxy
        override fun getViewPlacerRegistry() = VANILLA_VIEW_PLACER_REGISTRY

        override fun getScreen() = this@ViewContainerScreen
        override fun getRegionSizeX() = this@ViewContainerScreen.width
        override fun getRegionSizeY() = this@ViewContainerScreen.height

        override fun createRootView() = menu.createView(title, playerInventoryTitle)

        override fun getLeftPos() = this@ViewContainerScreen.leftPos
        override fun getTopPos() = this@ViewContainerScreen.topPos
        override fun setImageWidth(imageWidth: Int) = run { this@ViewContainerScreen.imageWidth = imageWidth }
        override fun setImageHeight(imageHeight: Int) = run { this@ViewContainerScreen.imageHeight = imageHeight }

        override fun init() = super@ViewContainerScreen.init()

        override fun addRenderableWidget(widget: AbstractWidget) = unit { this@ViewContainerScreen.addRenderableWidget(widget) }
        override fun addRenderableOnly(widget: AbstractWidget) = unit { this@ViewContainerScreen.addRenderableOnly(widget) }
        override fun removeWidget(listener: GuiEventListener) = this@ViewContainerScreen.removeWidget(listener)

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

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (viewAdapter.keyPressed(keyCode, scanCode, modifiers)) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    /** 枠の絵はViewのツリーが描くのだ～🌱 */
    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) = Unit

    /** 題名とインベントリの名前もViewのツリーが描くのだ～🌱 */
    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) = Unit

}
