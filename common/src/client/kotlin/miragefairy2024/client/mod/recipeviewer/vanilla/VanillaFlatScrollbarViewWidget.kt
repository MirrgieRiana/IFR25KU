package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.FlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.XFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import mirrg.kotlin.hydrogen.alphaOfArgb
import mirrg.kotlin.hydrogen.argbOf
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

abstract class VanillaFlatScrollbarViewWidget(view: FlatScrollbarView, bounds: IntRectangle) : VanillaScrollbarViewWidget<FlatScrollbarView>(view, bounds) {

    private var fadeEndTime: Long? = null

    protected fun resetFadeTimer(now: Long) {
        fadeEndTime = now + 1500L
    }

    protected fun getScrollbarColor(now: Long): Int {
        val color = view.scrollbarColor
        val endTime = fadeEndTime
        val alpha = if (endTime == null) {
            0
        } else {
            // owo と同じく、表示期限までの残り 750 ms で透明にするのだ～🌱
            val remainingTime = (endTime - now) atLeast 0L atMost 750L
            val progress = remainingTime / 750.0F
            val opacity = (sin(progress * PI - PI / 2) * 0.5 + 0.5).toFloat()
            (opacity * color.alphaOfArgb).toInt()
        }
        return argbOf(alpha, color)
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val range = evaluateScrollRange()
        if (range.start < range.endInclusive && scrollbarThickness > 0 && scrollbarLength > 0) {
            val now = Util.getMillis()
            // ホバー中か、領域外でドラッグを保持している間、フェードアウトをリセットし続けるのだ～🌱
            if (isMouseOver(mouseX.toDouble(), mouseY.toDouble()) || hasMouseCapture(0)) {
                resetFadeTimer(now)
            }
            val start = evaluateThumbPosition().roundToInt()
            val length = evaluateThumbLength()
            val color = getScrollbarColor(now)
            renderImpl(guiGraphics, start, length, color)
        }
    }

    protected abstract fun renderImpl(guiGraphics: GuiGraphics, start: Int, length: Int, color: Int)

}

class VanillaXFlatScrollbarViewWidget(
    view: XFlatScrollbarView,
    bounds: IntRectangle,
) : VanillaFlatScrollbarViewWidget(
    view,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = width
    override val scrollbarThickness get() = height


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x
    override fun getDragAmount(dragX: Double, dragY: Double) = dragX

    // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    override fun renderImpl(guiGraphics: GuiGraphics, start: Int, length: Int, color: Int) {
        guiGraphics.fill(x + start, y, x + start + length, y + height, color)
    }

}

class VanillaYFlatScrollbarViewWidget(
    view: YFlatScrollbarView,
    bounds: IntRectangle,
) : VanillaFlatScrollbarViewWidget(
    view,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = height
    override val scrollbarThickness get() = width


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y
    override fun getDragAmount(dragX: Double, dragY: Double) = dragY
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    override fun renderImpl(guiGraphics: GuiGraphics, start: Int, length: Int, color: Int) {
        guiGraphics.fill(x, y + start, x + width, y + start + length, color)
    }

}
