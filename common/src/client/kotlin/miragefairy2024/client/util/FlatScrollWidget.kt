package miragefairy2024.client.util

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractScrollWidget
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import kotlin.math.max
import kotlin.math.sin

/**
 * スクロールバーを単色の矩形で描き、操作が止まると徐々に透明にしていくスクロール領域なのだ～🌱
 * スクロールバーの描画以外の振る舞いは、[net.minecraft.client.gui.components.AbstractScrollWidget] のままなのだ～🌱
 */
abstract class FlatScrollWidget(x: Int, y: Int, width: Int, height: Int, message: Component, private val scrollbarColor: Int) : AbstractScrollWidget(x, y, width, height, message) {
    companion object {
        /** スクロールバーが完全に透明になるまでにかける時間なのだ～🌱 */
        private const val FADE_DURATION = 750L

        /** スクロールバーをホバーしたときやドラッグしたときに、不透明なまま留まる時間なのだ～🌱 */
        private const val HOVER_DURATION = 1500L

        /** スクロールしたときに、不透明なまま留まる時間なのだ～🌱 */
        private const val SCROLL_DURATION = 1250L
    }

    /** スクロールバーが完全に透明になる時刻なのだ～🌱 */
    private var fadeEndTime = 0L

    private var draggingScrollbar = false

    private fun delayFade(duration: Long) {
        fadeEndTime = System.currentTimeMillis() + duration
    }

    private fun isInScrollbar(mouseX: Double, mouseY: Double): Boolean {
        if (!scrollbarVisible()) return false
        if (mouseX < (x + width).toDouble()) return false
        if (mouseX > (x + width + scrollbarWidth()).toDouble()) return false
        if (mouseY < y.toDouble()) return false
        if (mouseY >= (y + height).toDouble()) return false
        return true
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && isInScrollbar(mouseX, mouseY)) draggingScrollbar = true
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) draggingScrollbar = false
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val result = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        if (result) delayFade(SCROLL_DURATION)
        return result
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val result = super.keyPressed(keyCode, scanCode, modifiers)
        if (result) delayFade(SCROLL_DURATION)
        return result
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (draggingScrollbar || isInScrollbar(mouseX.toDouble(), mouseY.toDouble())) delayFade(HOVER_DURATION)
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun renderDecorations(guiGraphics: GuiGraphics) {
        if (!scrollbarVisible()) return
        val alpha = ((scrollbarColor ushr 24) * getFadeProgress()).toInt()
        if (alpha <= 0) return
        // 親クラスがスクロールバーの寸法を公開していないため、同じ式で再現しているのだ～🌱
        val contentHeight = getInnerHeight() + 4
        val scrollbarHeight = Mth.clamp(height * height / contentHeight.toFloat(), 32.0F, height.toFloat()).toInt()
        val scrollbarX = x + width
        val scrollbarY = max(y, scrollAmount().toInt() * (height - scrollbarHeight) / maxScrollAmount + y)
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth(), scrollbarY + scrollbarHeight, alpha shl 24 or (scrollbarColor and 0xFFFFFF))
    }

    /** 1 が不透明で、0 が透明なのだ～🌱 */
    private fun getFadeProgress(): Float {
        val remainingTime = Mth.clamp(fadeEndTime - System.currentTimeMillis(), 0L, FADE_DURATION)
        val ratio = remainingTime / FADE_DURATION.toDouble()
        // サインの曲線を 0 から 1 の範囲へ収め直したものなのだ～🌱
        return (sin(ratio * Math.PI - Math.PI / 2) * 0.5 + 0.5).toFloat()
    }
}
