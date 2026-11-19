package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import miragefairy2024.client.util.stack
import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.VanillaScrollbarView
import miragefairy2024.mod.recipeviewer.views.XVanillaScrollbarView
import miragefairy2024.mod.recipeviewer.views.YVanillaScrollbarView
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.empty
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

private val CREATIVE_INVENTORY_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png")
private val SCROLLER_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/scroller.png")
private val DISABLED_SCROLLER_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/scroller_disabled.png")

abstract class VanillaVanillaScrollbarViewWidget(
    protected val view: VanillaScrollbarView,
    private val scrollOffsetValue: ObservableValue<Double>,
    bounds: IntRectangle,
) :
    AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }), MouseCaptureContainer {


    // ライフサイクルなのだ～🌱

    private var disposed = false

    fun dispose() {
        disposed = true
        draggingScrollbar = false
        focusedSelf = false
    }


    // サイズと位置なのだ～🌱

    protected abstract val scrollbarLength: Int
    protected abstract val scrollbarThickness: Int
    protected abstract fun evaluateContentLength(): Int

    // 範囲が狭まっても、現在の位置から操作できるようにするのだ～🌱
    private fun evaluateScrollRange(): ClosedFloatingPointRange<Double> {
        val normalMaxScrollOffset = (evaluateContentLength() - view.viewportLength atLeast 0).toDouble()
        return minOf(0.0, scrollOffsetValue.value)..maxOf(normalMaxScrollOffset, scrollOffsetValue.value)
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
            val scrollRatio = (scrollOffsetValue.value - range.start) / (range.endInclusive - range.start)
            maxThumbPosition.toDouble() * scrollRatio
        }
    }

    private fun updateScroll(offset: Double): Boolean {
        val old = scrollOffsetValue.value
        val range = evaluateScrollRange()
        scrollOffsetValue.value = offset.coerceIn(range.start, range.endInclusive)
        return scrollOffsetValue.value != old
    }


    // 描画とマウス入力なのだ～🌱

    private var draggingScrollbar = false

    override fun isActive() = super.isActive() && !disposed

    override fun isMouseOver(mouseX: Double, mouseY: Double) = isActive && super.isMouseOver(mouseX, mouseY)

    protected abstract fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int, scrollable: Boolean)

    protected fun renderVerticalScrollbar(guiGraphics: GuiGraphics, x: Int, y: Int, start: Int, length: Int, scrollable: Boolean) {

        // クリエイティブインベントリの背景に含まれる溝は、外周の 1 px を保ったまま伸長するのだ～🌱
        fun blitTrackRow(dy: Int, height: Int, v: Float) {
            guiGraphics.blit(CREATIVE_INVENTORY_TEXTURE, x, dy, 1, height, 174F, v, 1, 1, 256, 256)
            if (scrollbarThickness > 2) guiGraphics.blit(CREATIVE_INVENTORY_TEXTURE, x + 1, dy, scrollbarThickness - 2, height, 175F, v, 12, 1, 256, 256)
            guiGraphics.blit(CREATIVE_INVENTORY_TEXTURE, x + scrollbarThickness - 1, dy, 1, height, 187F, v, 1, 1, 256, 256)
        }
        blitTrackRow(y, 1, 17F)
        if (scrollbarLength > 2) blitTrackRow(y + 1, scrollbarLength - 2, 18F)
        blitTrackRow(y + scrollbarLength - 1, 1, 128F)

        val texture = if (scrollable) SCROLLER_TEXTURE else DISABLED_SCROLLER_TEXTURE
        val thumbX = x + 1
        val thumbY = y + start + 1
        val thumbWidth = scrollbarThickness - 2
        val thumbHeight = length - 2
        if (thumbWidth <= 0 || thumbHeight <= 0) return
        if (thumbWidth < 4 || thumbHeight < 4) {
            guiGraphics.blit(texture, thumbX, thumbY, thumbWidth, thumbHeight, 0F, 0F, 12, 15, 12, 15)
            return
        }

        fun blitThumbRow(dy: Int, height: Int, v: Int) {
            guiGraphics.blit(texture, thumbX, dy, 2, height, 0F, v.toFloat(), 2, height, 12, 15)
            var remainingWidth = thumbWidth - 4
            while (remainingWidth > 0) {
                val width = remainingWidth atMost 8
                guiGraphics.blit(texture, thumbX + 2 + remainingWidth - width, dy, width, height, (10 - width).toFloat(), v.toFloat(), width, height, 12, 15)
                remainingWidth -= width
            }
            guiGraphics.blit(texture, thumbX + thumbWidth - 2, dy, 2, height, 10F, v.toFloat(), 2, height, 12, 15)
        }

        blitThumbRow(thumbY, 2, 0)
        // 縞の端が下側の枠につながるよう、中央の 12 px を下から反復するのだ～🌱
        var remainingHeight = thumbHeight - 4
        while (remainingHeight > 0) {
            val height = remainingHeight atMost 12
            blitThumbRow(thumbY + 2 + remainingHeight - height, height, 13 - height)
            remainingHeight -= height
        }
        blitThumbRow(thumbY + thumbHeight - 2, 2, 13)

    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (scrollbarThickness < 2 || scrollbarLength < 2) return
        val range = evaluateScrollRange()
        RenderSystem.enableBlend()
        try {
            renderScrollbar(guiGraphics, evaluateThumbPosition().roundToInt(), evaluateThumbLength(), range.start < range.endInclusive)
        } finally {
            RenderSystem.disableBlend()
        }
    }

    protected abstract fun getMousePosition(mouseX: Double, mouseY: Double): Double

    protected abstract fun getDragAmount(dragX: Double, dragY: Double): Double

    protected abstract fun getScrollAmount(scrollX: Double, scrollY: Double): Double

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
            val mousePosition = getMousePosition(mouseX, mouseY)
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
                val scrollDelta = getDragAmount(dragX, dragY) * (range.endInclusive - range.start) / remainingThumbLength
                updateScroll(scrollOffsetValue.value + scrollDelta)
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
        return updateScroll(scrollOffsetValue.value - getScrollAmount(scrollX, scrollY) * view.scrollStep)
    }


    // フォーカスとキーボード入力なのだ～🌱

    private var focusedSelf = false

    override fun isFocused() = focusedSelf

    override fun setFocused(focused: Boolean) {
        focusedSelf = focused
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? {

        if (!isActive) return null

        val range = evaluateScrollRange()
        if (range.start < range.endInclusive && !isFocused) {
            return object : ComponentPath {
                override fun component() = this@VanillaVanillaScrollbarViewWidget

                override fun applyFocus(focused: Boolean) {
                    this@VanillaVanillaScrollbarViewWidget.isFocused = focused
                }
            }
        }
        return null
    }

    override fun getCurrentFocusPath(): ComponentPath? {
        if (isFocused) return ComponentPath.leaf(this)
        return null
    }

    protected abstract val backwardKey: Int
    protected abstract val forwardKey: Int

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isActive) return false
        if (!isFocused) return false
        val offset = when (keyCode) {
            backwardKey -> scrollOffsetValue.value - view.scrollStep
            forwardKey -> scrollOffsetValue.value + view.scrollStep
            GLFW.GLFW_KEY_PAGE_UP -> scrollOffsetValue.value - view.viewportLength * 0.8
            GLFW.GLFW_KEY_PAGE_DOWN -> scrollOffsetValue.value + view.viewportLength * 0.8
            GLFW.GLFW_KEY_HOME -> evaluateScrollRange().start
            GLFW.GLFW_KEY_END -> evaluateScrollRange().endInclusive
            else -> return false
        }
        return updateScroll(offset)
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

}

class VanillaXVanillaScrollbarViewWidget(
    view: XVanillaScrollbarView,
    bounds: IntRectangle,
) : VanillaVanillaScrollbarViewWidget(
    view,
    view.scrollOffsetX,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = width
    override val scrollbarThickness get() = height
    override fun evaluateContentLength() = view.contentLength


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_LEFT
    override val forwardKey get() = GLFW.GLFW_KEY_RIGHT

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseX - x
    override fun getDragAmount(dragX: Double, dragY: Double) = dragX

    // 横方向の入力がない場合は、縦方向のホイール入力で横スクロールするのだ～🌱
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = if (scrollX != 0.0) scrollX else scrollY

    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int, scrollable: Boolean) {
        val pose: PoseStack = guiGraphics.pose()
        pose.stack {
            pose.translate(x.toFloat(), (y + height).toFloat(), 0F)
            pose.mulPose(Axis.ZN.rotationDegrees(90F))
            renderVerticalScrollbar(guiGraphics, 0, 0, start, length, scrollable)
        }
    }

}

class VanillaYVanillaScrollbarViewWidget(
    view: YVanillaScrollbarView,
    bounds: IntRectangle,
) : VanillaVanillaScrollbarViewWidget(
    view,
    view.scrollOffsetY,
    bounds,
) {

    // 領域とサイズなのだ～🌱

    override val scrollbarLength get() = height
    override val scrollbarThickness get() = width
    override fun evaluateContentLength() = view.contentLength


    // スクロール操作なのだ～🌱

    override val backwardKey get() = GLFW.GLFW_KEY_UP
    override val forwardKey get() = GLFW.GLFW_KEY_DOWN

    override fun getMousePosition(mouseX: Double, mouseY: Double) = mouseY - y
    override fun getDragAmount(dragX: Double, dragY: Double) = dragY
    override fun getScrollAmount(scrollX: Double, scrollY: Double) = scrollY

    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int, scrollable: Boolean) {
        renderVerticalScrollbar(guiGraphics, x, y, start, length, scrollable)
    }

}
