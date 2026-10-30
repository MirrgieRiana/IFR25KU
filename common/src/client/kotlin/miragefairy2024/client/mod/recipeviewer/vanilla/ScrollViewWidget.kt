package miragefairy2024.client.mod.recipeviewer.vanilla

import kotlin.math.max
import kotlin.math.sin
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractScrollWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

/**
 * [miragefairy2024.mod.recipeviewer.views.ScrollView] の子を、バニラの画面のスクロール領域として描くのだ～🌱
 * 親クラスが前提にしている枠と内側の余白は、このスクロール領域が持たないため、取り除くのだ～🌱
 * 親クラスはスクロールバーを自身の右隣へ描くため、スクロールバーの幅だけ狭い幅を渡して、[miragefairy2024.mod.recipeviewer.views.ScrollView] の右端へ収めるのだ～🌱
 */
class ScrollViewWidget(val view: ScrollView, viewBounds: IntRectangle) :
    AbstractScrollWidget(viewBounds.x, viewBounds.y, viewBounds.sizeX - view.scrollbarThickness, viewBounds.sizeY, Component.empty()) {

    companion object {
        /** スクロールバーが完全に透明になるまでにかける時間なのだ～🌱 */
        private const val FADE_DURATION = 750L

        /** スクロールバーをホバーしたときやドラッグしたときに、不透明なまま留まる時間なのだ～🌱 */
        private const val HOVER_DURATION = 1500L

        /** スクロールしたときに、不透明なまま留まる時間なのだ～🌱 */
        private const val SCROLL_DURATION = 1250L

    }

    /** 子の View を置いた結果のウィジェットなのだ～🌱 座標はスクロール量 0 の状態なのだ～🌱 */
    private class WidgetSlot(val widget: AbstractWidget)

    private val widgetSlots = mutableListOf<WidgetSlot>()

    fun addWidget(widget: AbstractWidget): Remover {
        val widgetSlot = WidgetSlot(widget)
        widgetSlots += widgetSlot
        return Remover { widgetSlots -= widgetSlot }
    }

    /** 中身のクリッピング範囲なのだ～🌱 枠を描かないため、枠用の余白は設けないのだ～🌱 */
    val contentScissorArea get() = IntRectangle(x, y, width, height)

    override fun getInnerHeight() = view.childView.evaluateActualSizeY()

    override fun scrollRate() = view.scrollStep

    override fun setScrollAmount(scrollAmount: Double) {
        super.setScrollAmount(scrollAmount)
        view.scrollAmount = scrollAmount().toInt()
    }

    override fun scrollbarWidth() = view.scrollbarThickness

    /** 親クラスの式は内側の余白 4px を前提にしているため、余白を持たない式へ置き換えるのだ～🌱 */
    override fun getMaxScrollAmount() = max(0, innerHeight - height)

    override fun innerPadding() = 0

    override fun renderBackground(guiGraphics: GuiGraphics) = Unit

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val contentsMousePosition = if (withinContentAreaPoint(mouseX.toDouble(), mouseY.toDouble())) toContentsPosition(mouseX.toDouble(), mouseY.toDouble()) else IntPoint(-1, -1)
        widgetSlots.forEach {
            it.widget.render(guiGraphics, contentsMousePosition.x, contentsMousePosition.y, partialTick)
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
            widgetSlots.forEach {
                if (it.widget.mouseClicked(contentsMousePosition.x.toDouble(), contentsMousePosition.y.toDouble(), button)) return true
            }
        }
        return false
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double) = visible && (withinContentAreaPoint(mouseX, mouseY) || isInScrollbar(mouseX, mouseY))

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        widgetSlots.forEach {
            if (it.widget.keyPressed(keyCode, scanCode, modifiers)) return true
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
        setScrollAmount(view.scrollAmount.toDouble())
        guiGraphics.enableScissor(x, y, x + width, y + height)
        guiGraphics.pose().pushPose()
        try {
            guiGraphics.pose().translate(0.0, -scrollAmount(), 0.0)
            renderContents(guiGraphics, mouseX, mouseY, partialTick)
        } finally {
            guiGraphics.pose().popPose()
            guiGraphics.disableScissor()
        }
        renderDecorations(guiGraphics)
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

    /** 1 が不透明で、0 が透明なのだ～🌱 */
    private fun getFadeProgress(): Float {
        val remainingTime = Mth.clamp(fadeEndTime - System.currentTimeMillis(), 0L, FADE_DURATION)
        val ratio = remainingTime / FADE_DURATION.toDouble()
        // サインの曲線を 0 から 1 の範囲へ正規化したものなのだ～🌱
        return (sin(ratio * Math.PI - Math.PI / 2) * 0.5 + 0.5).toFloat()
    }

    private fun renderFlatScrollbar(guiGraphics: GuiGraphics, argb: Int) {
        if (!scrollbarVisible()) return
        val alpha = ((argb ushr 24) * getFadeProgress()).toInt()
        if (alpha <= 0) return
        // 親クラスがスクロールバーの寸法を公開していないため、同じ式で再現しているのだ～🌱
        val scrollbarHeight = Mth.clamp(height * height / innerHeight.toFloat(), 32.0F, height.toFloat()).toInt()
        val scrollbarX = x + width
        val scrollbarY = max(y, scrollAmount().toInt() * (height - scrollbarHeight) / max(1, maxScrollAmount) + y)
        guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth(), scrollbarY + scrollbarHeight, alpha shl 24 or (argb and 0xFFFFFF))
    }

}

interface ScrollViewPlacerContext : VanillaViewPlacerContext {
    val scrollViewWidget: ScrollViewWidget
}
