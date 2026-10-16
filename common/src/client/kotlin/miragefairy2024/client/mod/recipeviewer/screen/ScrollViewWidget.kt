package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractScrollWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import kotlin.math.max
import kotlin.math.sin

/**
 * [ScrollView]の子を、バニラの画面のスクロール領域として描くのだ～🌱
 * 親クラスが前提にしている枠と内側の余白は、このスクロール領域が持たないため、取り除くのだ～🌱
 */
class ScrollViewWidget(private val view: ScrollView, private val viewBounds: IntRectangle) :
    AbstractScrollWidget(viewBounds.x, viewBounds.y, viewBounds.sizeX, viewBounds.sizeY, Component.empty()) {

    companion object {
        /** スクロールバーが完全に透明になるまでにかける時間なのだ～🌱 */
        private const val FADE_DURATION = 750L

        /** スクロールバーをホバーしたときやドラッグしたときに、不透明なまま留まる時間なのだ～🌱 */
        private const val HOVER_DURATION = 1500L

        /** スクロールしたときに、不透明なまま留まる時間なのだ～🌱 */
        private const val SCROLL_DURATION = 1250L

        /** キーの上下やマウスホイール1目盛りで動く量なのだ～🌱 */
        private const val SCROLL_RATE = 9.0
    }

    /** 子のViewを置いた結果のウィジェットなのだ～🌱 座標はスクロール量0の状態なのだ～🌱 */
    val childWidgets = mutableListOf<AbstractWidget>()

    override fun getInnerHeight() = view.childView.actualSize.y

    override fun scrollRate() = SCROLL_RATE

    /** 親クラスの式は内側の余白4pxを前提にしているため、余白を持たない式へ置き換えるのだ～🌱 */
    override fun getMaxScrollAmount() = max(0, innerHeight - height)

    override fun innerPadding() = 0

    override fun renderBackground(guiGraphics: GuiGraphics) = Unit

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val contentsMousePosition = toContentsPosition(mouseX.toDouble(), mouseY.toDouble())
        childWidgets.forEach {
            it.render(guiGraphics, contentsMousePosition.x, contentsMousePosition.y, partialTick)
        }
    }

    /** 画面上の座標を、スクロールする前の子の座標へ移すのだ～🌱 */
    private fun toContentsPosition(mouseX: Double, mouseY: Double) = IntPoint(mouseX.toInt(), (mouseY + scrollAmount()).toInt())

    private fun isInScrollbar(mouseX: Double, mouseY: Double): Boolean {
        if (!scrollbarVisible()) return false
        if (mouseX < (x + width).toDouble()) return false
        if (mouseX > (x + width + scrollbarWidth()).toDouble()) return false
        if (mouseY < y.toDouble()) return false
        if (mouseY >= (y + height).toDouble()) return false
        return true
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && isInScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true
            return super.mouseClicked(mouseX, mouseY, button)
        }
        if (withinContentAreaPoint(mouseX, mouseY)) {
            val contentsMousePosition = toContentsPosition(mouseX, mouseY)
            childWidgets.forEach {
                if (it.mouseClicked(contentsMousePosition.x.toDouble(), contentsMousePosition.y.toDouble(), button)) return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        childWidgets.forEach {
            if (it.keyPressed(keyCode, scanCode, modifiers)) return true
        }
        val result = super.keyPressed(keyCode, scanCode, modifiers)
        if (result) delayFade(SCROLL_DURATION)
        return result
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

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (draggingScrollbar || isInScrollbar(mouseX.toDouble(), mouseY.toDouble())) delayFade(HOVER_DURATION)
        view.scrollAmount = scrollAmount().toInt()
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun renderDecorations(guiGraphics: GuiGraphics) {
        when (val scrollbarStyle = view.scrollbarStyle) {
            is ScrollbarStyle.Flat -> renderFlatScrollbar(guiGraphics, scrollbarStyle.argb)
            ScrollbarStyle.Vanilla -> super.renderDecorations(guiGraphics)
        }
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit


    /** スクロールバーが完全に透明になる時刻なのだ～🌱 */
    private var fadeEndTime = 0L

    private var draggingScrollbar = false

    private fun delayFade(duration: Long) {
        fadeEndTime = System.currentTimeMillis() + duration
    }

    /** 1が不透明で、0が透明なのだ～🌱 */
    private fun getFadeProgress(): Float {
        val remainingTime = Mth.clamp(fadeEndTime - System.currentTimeMillis(), 0L, FADE_DURATION)
        val ratio = remainingTime / FADE_DURATION.toDouble()
        // サインの曲線を0から1の範囲へ正規化したものなのだ～🌱
        return (sin(ratio * Math.PI - Math.PI / 2) * 0.5 + 0.5).toFloat()
    }

    private fun renderFlatScrollbar(guiGraphics: GuiGraphics, argb: Int) {
        if (!scrollbarVisible()) return
        val alpha = ((argb ushr 24) * getFadeProgress()).toInt()
        if (alpha <= 0) return
        // 親クラスがスクロールバーの寸法を公開していないため、同じ式で再現しているのだ～🌱
        val scrollbarHeight = Mth.clamp(height * height / innerHeight.toFloat(), 32.0F, height.toFloat()).toInt()
        val scrollbarX = x + width - view.scrollbarThickness
        val scrollbarY = max(y, scrollAmount().toInt() * (height - scrollbarHeight) / max(1, maxScrollAmount) + y)
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + view.scrollbarThickness, scrollbarY + scrollbarHeight, alpha shl 24 or (argb and 0xFFFFFF))
    }

}
