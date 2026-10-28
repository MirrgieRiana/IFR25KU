package miragefairy2024.client.util

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.client.mod.recipeviewer.vanilla.VANILLA_VIEW_PLACER_REGISTRY
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapter
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewAdapterHost
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.Remover
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent

/** View を Screen なしで表示し、非表示への切り替え時にイベント購読を解除するトーストなのだ～🌱 */
class ViewToast(rootView: View) : Toast {

    private class WidgetSlot(val widget: AbstractWidget)

    private val widgetSlots = mutableListOf<WidgetSlot>()
    private var size = IntPoint.ZERO

    private val viewAdapter = ViewAdapter(object : ViewAdapterHost {

        override fun getRenderingProxy() = renderingProxy
        override fun getViewPlacerRegistry() = VANILLA_VIEW_PLACER_REGISTRY

        override fun getScreen() = null
        override fun getRegionSizeX() = 1000
        override fun getRegionSizeY() = 1000

        override fun createRootView() = rootView

        override fun getUiOffset() = IntPoint.ZERO
        override fun setUiSize(uiSize: IntPoint) = run { size = uiSize }

        override fun init() {
            widgetSlots.clear()
        }

        override fun addRenderableWidget(widget: AbstractWidget): Remover {
            val widgetSlot = WidgetSlot(widget)
            widgetSlots += widgetSlot
            return Remover { widgetSlots -= widgetSlot }
        }

        override fun addRenderableOnly(widget: AbstractWidget) = addRenderableWidget(widget)

    })

    init {
        viewAdapter.init()
    }

    override fun width() = size.x
    override fun height() = size.y

    override fun render(guiGraphics: GuiGraphics, toastComponent: ToastComponent, timeSinceLastVisible: Long): Toast.Visibility {
        val partialTick = Minecraft.getInstance().timer.getGameTimeDeltaPartialTick(true)
        widgetSlots.forEach {
            it.widget.render(guiGraphics, -1000, -1000, partialTick)
        }
        if (timeSinceLastVisible >= 5000.0 * toastComponent.notificationDisplayTimeMultiplier) {
            // 非表示アニメーション中にも描画されるため、購読を解除しても描画要素は保持するのだ～🌱
            viewAdapter.removed()
            return Toast.Visibility.HIDE
        }
        return Toast.Visibility.SHOW
    }

}
