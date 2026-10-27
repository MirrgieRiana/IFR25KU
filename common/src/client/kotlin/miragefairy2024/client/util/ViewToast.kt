package miragefairy2024.client.util

import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.client.mod.recipeviewer.vanilla.VANILLA_VIEW_PLACER_REGISTRY
import miragefairy2024.client.mod.recipeviewer.vanilla.ViewRenderHost
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent

/** View を Screen なしで表示し、非表示への切り替え時にイベント購読を解除するトーストなのだ～🌱 */
class ViewToast(rootView: View) : Toast {
    private val host = ViewRenderHost(rootView, IntPoint(1000, 1000), renderingProxy, VANILLA_VIEW_PLACER_REGISTRY)

    override fun width() = host.size.x
    override fun height() = host.size.y

    override fun render(guiGraphics: GuiGraphics, toastComponent: ToastComponent, timeSinceLastVisible: Long): Toast.Visibility {
        host.render(guiGraphics, -1000, -1000, Minecraft.getInstance().timer.getGameTimeDeltaPartialTick(true))
        if (timeSinceLastVisible >= 5000.0 * toastComponent.notificationDisplayTimeMultiplier) {
            // 非表示アニメーション中にも描画されるため、購読を解除しても描画要素は保持するのだ～🌱
            host.viewAdapter.removed()
            return Toast.Visibility.HIDE
        }
        return Toast.Visibility.SHOW
    }
}
