package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent

/** トーストが消えるまでの時間なのだ～🌱 */
private const val DISPLAY_DURATION_MILLIS = 5000.0

/** 入力を受け取らないため、カーソルは枠の外に居ることにするのだ～🌱 */
private const val MOUSE_POSITION_OUTSIDE = -1000

/**
 * Viewのツリーを、バニラのトーストとして見せるのだ～🌱
 * 親クラスは枠の左上を原点として呼ぶため、Viewのツリーも原点を左上として組むのだ～🌱
 */
fun createViewToast(size: IntPoint, rootView: View): Toast = object : Toast {

    private val widgets = ViewScreenAdapter(rootView).also { it.layout(IntPoint.ZERO, size) }.widgets

    override fun render(guiGraphics: GuiGraphics, toastComponent: ToastComponent, timeSinceLastVisible: Long): Toast.Visibility {
        val partialTick = Minecraft.getInstance().timer.getGameTimeDeltaPartialTick(true)
        widgets.forEach {
            it.render(guiGraphics, MOUSE_POSITION_OUTSIDE, MOUSE_POSITION_OUTSIDE, partialTick)
        }

        return if (timeSinceLastVisible.toDouble() >= DISPLAY_DURATION_MILLIS * toastComponent.notificationDisplayTimeMultiplier) Toast.Visibility.HIDE else Toast.Visibility.SHOW
    }

    override fun width() = size.x

    override fun height() = size.y

}
