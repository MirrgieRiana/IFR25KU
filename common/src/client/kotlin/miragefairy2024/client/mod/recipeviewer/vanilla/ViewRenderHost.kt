package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener

/** Screen を生成せず、View の描画要素を所有するホストなのだ～🌱 */
class ViewRenderHost(private val rootView: View, private val regionSize: IntPoint, private val renderingProxy: RenderingProxy, private val viewPlacerRegistry: ViewPlacerRegistry<VanillaViewPlacerContext>) : ViewAdapterHost, Renderable {
    private var widgets = listOf<AbstractWidget>()
    var size = IntPoint.ZERO
        private set
    val viewAdapter = ViewAdapter(this)

    init {
        viewAdapter.init()
    }

    override fun getRenderingProxy() = renderingProxy
    override fun getViewPlacerRegistry() = viewPlacerRegistry
    override fun getScreen() = null
    override fun getRegionSizeX() = regionSize.x
    override fun getRegionSizeY() = regionSize.y
    override fun createRootView() = rootView
    override fun getLeftPos() = 0
    override fun getTopPos() = 0
    override fun setImageWidth(imageWidth: Int) {
        size = IntPoint(imageWidth, size.y)
    }
    override fun setImageHeight(imageHeight: Int) {
        size = IntPoint(size.x, imageHeight)
    }

    override fun init() {
        widgets = emptyList()
    }

    override fun addRenderableWidget(widget: AbstractWidget) {
        widgets = widgets + widget
    }

    override fun addRenderableOnly(widget: AbstractWidget) = addRenderableWidget(widget)

    override fun removeWidget(listener: GuiEventListener) {
        widgets = widgets.filterNot { it === listener }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        widgets.forEach {
            it.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }
}
