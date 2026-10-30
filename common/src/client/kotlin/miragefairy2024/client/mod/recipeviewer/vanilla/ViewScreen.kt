package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/** 中身を View のツリーで構築する画面なのだ～🌱 */
abstract class ViewScreen(title: Component) : Screen(title) {

    protected abstract fun createRootView(): View

    private val viewAdapter = ViewAdapter(object : ViewAdapterHost {

        override fun getRenderingProxy() = renderingProxy
        override fun getViewPlacerRegistry() = VANILLA_VIEW_PLACER_REGISTRY

        override fun getScreen() = this@ViewScreen
        override fun getRegionSizeX() = this@ViewScreen.width
        override fun getRegionSizeY() = this@ViewScreen.height

        override fun createRootView() = this@ViewScreen.createRootView()

        override fun getUiOffset() = IntPoint.ZERO
        override fun setUiSize(uiSize: IntPoint) = Unit

        override fun init() = super@ViewScreen.init()

        override fun addRenderableWidget(widget: AbstractWidget): Remover {
            this@ViewScreen.addRenderableWidget(widget)
            return Remover { this@ViewScreen.removeWidget(widget) }
        }

        override fun addRenderableOnly(widget: AbstractWidget): Remover {
            this@ViewScreen.addRenderableOnly(widget)
            return Remover { this@ViewScreen.removeWidget(widget) }
        }

    })

    override fun init() = viewAdapter.init()

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = renderTransparentBackground(guiGraphics)

    override fun removed() {
        viewAdapter.removed()
        super.removed()
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

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (viewAdapter.keyPressed(keyCode, scanCode, modifiers)) return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

}
