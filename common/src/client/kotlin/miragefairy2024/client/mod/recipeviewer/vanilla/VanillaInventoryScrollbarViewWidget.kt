package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import miragefairy2024.client.util.stack
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.InventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.XInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

private val CREATIVE_INVENTORY_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png")
private val SCROLLER_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/scroller.png")
private val DISABLED_SCROLLER_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/scroller_disabled.png")

abstract class VanillaInventoryScrollbarViewWidget(view: InventoryScrollbarView, bounds: IntRectangle) : VanillaScrollbarViewWidget<InventoryScrollbarView>(view, bounds) {

    protected fun renderVerticalScrollbar(guiGraphics: GuiGraphics, x: Int, y: Int) {
        val range = evaluateScrollRange()
        val scrollable = range.start < range.endInclusive
        val start = evaluateThumbPosition().roundToInt()
        val length = evaluateThumbLength()

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

}

class VanillaXInventoryScrollbarViewWidget(
    view: XInventoryScrollbarView,
    bounds: IntRectangle,
) : VanillaInventoryScrollbarViewWidget(
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

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (scrollbarThickness < 2 || scrollbarLength < 2) return
        RenderSystem.enableBlend()
        try {
            val pose: PoseStack = guiGraphics.pose()
            pose.stack {
                pose.translate(x.toFloat(), (y + height).toFloat(), 0F)
                pose.mulPose(Axis.ZN.rotationDegrees(90F))
                renderVerticalScrollbar(guiGraphics, 0, 0)
            }
        } finally {
            RenderSystem.disableBlend()
        }
    }

}

class VanillaYInventoryScrollbarViewWidget(
    view: YInventoryScrollbarView,
    bounds: IntRectangle,
) : VanillaInventoryScrollbarViewWidget(
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

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (scrollbarThickness < 2 || scrollbarLength < 2) return
        RenderSystem.enableBlend()
        try {
            renderVerticalScrollbar(guiGraphics, x, y)
        } finally {
            RenderSystem.disableBlend()
        }
    }

}
