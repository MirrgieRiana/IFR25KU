package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import miragefairy2024.MirageFairy2024
import miragefairy2024.client.util.drawNinePatchImage
import miragefairy2024.client.util.stack
import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.ScrollbarView
import miragefairy2024.util.Remover
import miragefairy2024.util.empty
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import mirrg.kotlin.hydrogen.alphaOfArgb
import mirrg.kotlin.hydrogen.argbOf
import net.minecraft.Util
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.lwjgl.glfw.GLFW
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private val BACKGROUND_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_background.png")
private val SCROLLER_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_scroller.png")
private val DISABLED_SCROLLER_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_scroller_disabled.png")

class VanillaScrollbarViewWidget(
    private val view: ScrollbarView,
    bounds: IntRectangle,
) :
    AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }), MouseCaptureContainer {


    // ライフサイクルなのだ～🌱

    private var disposed = false

    private var fadeEndTime: Long? = null
    private val scrollInputRemover = if (view.style is ScrollbarStyle.Flat) {
        view.onScrollInput.register {
            fadeEndTime = Util.getMillis() + 1250L
        }
    } else {
        Remover.NONE
    }

    fun dispose() {
        scrollInputRemover.remove()
        disposed = true
        draggingScrollbar = false
        focusedSelf = false
    }


    // サイズと位置なのだ～🌱

    private val scrollbarLength get() = if (view.horizontal) width else height
    private val scrollbarThickness get() = if (view.horizontal) height else width

    // 範囲が狭まっても、現在の位置から操作できるようにするのだ～🌱
    private fun evaluateScrollRange(): ClosedFloatingPointRange<Double> {
        val normalMaxScrollOffset = (view.contentLength - view.viewportLength atLeast 0).toDouble()
        return minOf(0.0, view.scrollOffset.value)..maxOf(normalMaxScrollOffset, view.scrollOffset.value)
    }

    private fun evaluateThumbLength(): Int {
        if (scrollbarLength <= 0) return 0
        val range = evaluateScrollRange()
        val scrollLength = range.endInclusive - range.start
        if (scrollLength == 0.0) return scrollbarLength
        val visibleRatio = view.viewportLength / (view.viewportLength + scrollLength)
        val rawThumbLength = (scrollbarLength.toDouble() * visibleRatio).roundToInt()
        val minThumbLength = scrollbarThickness.coerceIn(1, scrollbarLength)
        return rawThumbLength.coerceIn(minThumbLength, scrollbarLength)
    }

    private fun evaluateThumbPosition(): Double {
        val range = evaluateScrollRange()
        return if (range.start == range.endInclusive) {
            0.0
        } else {
            val maxThumbPosition = scrollbarLength - evaluateThumbLength()
            val scrollRatio = (view.scrollOffset.value - range.start) / (range.endInclusive - range.start)
            maxThumbPosition.toDouble() * scrollRatio
        }
    }


    // スクロール操作なのだ～🌱

    private fun updateScroll(offset: Double): Boolean {
        val old = view.scrollOffset.value
        val range = evaluateScrollRange()
        view.scrollOffset.value = offset.coerceIn(range.start, range.endInclusive)
        return view.scrollOffset.value != old
    }


    // マウス入力なのだ～🌱

    private var draggingScrollbar = false

    override fun isActive() = super.isActive() && !disposed

    override fun isMouseOver(mouseX: Double, mouseY: Double) = isActive && super.isMouseOver(mouseX, mouseY)

    override fun hasMouseCapture(button: Int) = !disposed && button == 0 && draggingScrollbar

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) draggingScrollbar = false
        if (!isActive) return false

        if (super.isMouseOver(mouseX, mouseY)) {
            // スクロールバーのドラッグ開始なのだ～🌱
            if (button != 0) return false
            val range = evaluateScrollRange()
            if (range.start == range.endInclusive) return false
            val thumbLength = evaluateThumbLength()
            val remainingThumbLength = scrollbarLength - thumbLength
            if (remainingThumbLength <= 0) return false
            draggingScrollbar = true
            val mousePosition = if (view.horizontal) mouseX - x else mouseY - y
            val thumbPosition = evaluateThumbPosition()
            if (mousePosition < thumbPosition || mousePosition >= thumbPosition + thumbLength) {
                val scrollRatio = ((mousePosition - thumbLength / 2.0) / remainingThumbLength).coerceIn(0.0, 1.0)
                updateScroll(range.start + (range.endInclusive - range.start) * scrollRatio)
            }
            return true
        } else {
            return false
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (button == 0 && draggingScrollbar) {
            val remainingThumbLength = scrollbarLength - evaluateThumbLength()
            if (remainingThumbLength > 0) {
                val range = evaluateScrollRange()
                val dragAmount = if (view.horizontal) dragX else dragY
                val scrollDelta = dragAmount * (range.endInclusive - range.start) / remainingThumbLength
                updateScroll(view.scrollOffset.value + scrollDelta)
            }
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false
            return true
        }
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!isMouseOver(mouseX, mouseY)) return false
        // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
        val amount = if (view.horizontal && scrollX != 0.0) scrollX else scrollY
        if (amount == 0.0) return false
        val handled = updateScroll(view.scrollOffset.value - amount * view.scrollStep)
        view.onScrollInput.fire()
        return handled
    }


    // フォーカスとキーボード入力なのだ～🌱

    private var focusedSelf = false

    override fun isFocused() = focusedSelf

    override fun setFocused(focused: Boolean) {
        focusedSelf = focused
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {

        if (!isActive || !view.keyboardFocusable) return null

        val range = evaluateScrollRange()
        if (range.start < range.endInclusive && !isFocused) {
            return object : ComponentPath {
                override fun component() = this@VanillaScrollbarViewWidget

                override fun applyFocus(focused: Boolean) {
                    this@VanillaScrollbarViewWidget.isFocused = focused
                }
            }
        }
        return null
    }

    override fun getCurrentFocusPath(): ComponentPath? {
        if (isFocused) return ComponentPath.leaf(this)
        return null
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isActive) return false
        if (!isFocused) return false
        val backwardKey = if (view.horizontal) GLFW.GLFW_KEY_LEFT else GLFW.GLFW_KEY_UP
        val forwardKey = if (view.horizontal) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_DOWN
        val offset = when (keyCode) {
            backwardKey -> view.scrollOffset.value - view.scrollStep
            forwardKey -> view.scrollOffset.value + view.scrollStep
            GLFW.GLFW_KEY_PAGE_UP -> view.scrollOffset.value - view.viewportLength * 0.8
            GLFW.GLFW_KEY_PAGE_DOWN -> view.scrollOffset.value + view.viewportLength * 0.8
            GLFW.GLFW_KEY_HOME -> evaluateScrollRange().start
            GLFW.GLFW_KEY_END -> evaluateScrollRange().endInclusive
            else -> return false
        }
        val handled = updateScroll(offset)
        view.onScrollInput.fire()
        return handled
    }


    // 読み上げなのだ～🌱

    override fun narrationPriority(): NarratableEntry.NarrationPriority {
        if (!isActive) return NarratableEntry.NarrationPriority.NONE
        if (isFocused) return NarratableEntry.NarrationPriority.FOCUSED
        return NarratableEntry.NarrationPriority.NONE
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        if (isFocused) {
            narrationElementOutput.add(NarratedElementType.TITLE, text { SCROLL_AREA_TRANSLATION() })
            return
        }
    }


    // 描画なのだ～🌱

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        when (val style = view.style) {
            is ScrollbarStyle.Flat -> renderFlatScrollbar(guiGraphics, style.color)
            ScrollbarStyle.Inventory -> renderInventoryScrollbar(guiGraphics)
        }
    }

    private fun renderFlatScrollbar(guiGraphics: GuiGraphics, scrollbarColor: Int) {
        fun getScrollbarColor(now: Long): Int {
            val endTime = fadeEndTime
            val alpha = if (endTime == null) {
                0
            } else {
                // owo と同じく、表示期限までの残り 750 ms で透明にするのだ～🌱
                val remainingTime = (endTime - now) atLeast 0L atMost 750L
                val progress = remainingTime / 750.0F
                val opacity = (sin(progress * PI - PI / 2) * 0.5 + 0.5).toFloat()
                (opacity * scrollbarColor.alphaOfArgb).toInt()
            }
            return argbOf(alpha, scrollbarColor)
        }

        val range = evaluateScrollRange()
        if (range.start < range.endInclusive && scrollbarThickness > 0 && scrollbarLength > 0) {
            val now = Util.getMillis()
            // 親のクリップで隠れた部分へのホバーを除外し、範囲外に出たドラッグは引き続き扱うのだ～🌱
            if ((isActive && isHovered) || hasMouseCapture(0)) {
                fadeEndTime = now + 1500L
            }
            val start = evaluateThumbPosition().roundToInt()
            val length = evaluateThumbLength()
            val color = getScrollbarColor(now)
            if (view.horizontal) {
                guiGraphics.fill(x + start, y, x + start + length, y + height, color)
            } else {
                guiGraphics.fill(x, y + start, x + width, y + start + length, color)
            }
        }
    }

    private fun renderInventoryScrollbar(guiGraphics: GuiGraphics) {
        if (scrollbarThickness < 2 || scrollbarLength < 2) return

        fun renderVerticalScrollbar(x: Int, y: Int) {
            val range = evaluateScrollRange()
            val scrollable = range.start < range.endInclusive
            val start = evaluateThumbPosition().roundToInt()
            val length = evaluateThumbLength()

            // 背景の描画
            guiGraphics.drawNinePatchImage(
                IntRectangle(x, y, scrollbarThickness, scrollbarLength),
                BACKGROUND_TEXTURE,
                16, 1, 13, 1,
                16, 1, 13, 1,
                false, false,
            )

            // つまみの描画
            val texture = if (scrollable) SCROLLER_TEXTURE else DISABLED_SCROLLER_TEXTURE
            val thumbX = x + 1
            val thumbY = y + start + 1
            val thumbWidth = scrollbarThickness - 2
            val thumbHeight = length - 2
            // 狭すぎる場合はつまみを描画しないのだ～🌱
            if (thumbWidth >= 4 && thumbHeight >= 4) {
                guiGraphics.drawNinePatchImage(
                    IntRectangle(thumbX, thumbY, thumbWidth, thumbHeight),
                    texture,
                    16, 2, 11, 2,
                    16, 2, 11, 2,
                    false, true,
                )
            }
        }

        if (view.horizontal) {
            val pose: PoseStack = guiGraphics.pose()
            pose.stack {
                pose.translate(x.toFloat(), (y + height).toFloat(), 0F)
                pose.mulPose(Axis.ZN.rotationDegrees(90F))
                renderVerticalScrollbar(0, 0)
            }
        } else {
            renderVerticalScrollbar(x, y)
        }
    }

}
