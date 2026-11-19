package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import miragefairy2024.client.util.stack
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.VanillaScrollbarView
import miragefairy2024.mod.recipeviewer.views.XVanillaScrollbarView
import miragefairy2024.mod.recipeviewer.views.YVanillaScrollbarView
import miragefairy2024.util.ObservableValue
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

private val CREATIVE_INVENTORY_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png")
private val SCROLLER_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/scroller.png")
private val DISABLED_SCROLLER_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/scroller_disabled.png")

abstract class VanillaVanillaScrollbarViewWidget(
    view: VanillaScrollbarView,
    scrollOffsetValue: ObservableValue<Double>,
    bounds: IntRectangle,
    horizontal: Boolean,
) : AbstractVanillaScrollbarViewWidget(view, scrollOffsetValue, bounds, horizontal) {

    override fun shouldRenderScrollbar(scrollable: Boolean) = scrollbarThickness >= 2 && scrollbarLength >= 2

    override fun renderScrollbar(guiGraphics: GuiGraphics, start: Int, length: Int, scrollable: Boolean) {
        RenderSystem.enableBlend()
        try {
            if (horizontal) {
                val pose: PoseStack = guiGraphics.pose()
                pose.stack {
                    pose.translate(x.toFloat(), (y + height).toFloat(), 0F)
                    pose.mulPose(Axis.ZN.rotationDegrees(90F))
                    renderVerticalScrollbar(guiGraphics, 0, 0, start, length, scrollable)
                }
            } else {
                renderVerticalScrollbar(guiGraphics, x, y, start, length, scrollable)
            }
        } finally {
            RenderSystem.disableBlend()
        }
    }

    private fun renderVerticalScrollbar(guiGraphics: GuiGraphics, x: Int, y: Int, start: Int, length: Int, scrollable: Boolean) {

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

class VanillaXVanillaScrollbarViewWidget(
    view: XVanillaScrollbarView,
    bounds: IntRectangle,
) : VanillaVanillaScrollbarViewWidget(view, view.scrollOffsetX, bounds, horizontal = true)

class VanillaYVanillaScrollbarViewWidget(
    view: YVanillaScrollbarView,
    bounds: IntRectangle,
) : VanillaVanillaScrollbarViewWidget(view, view.scrollOffsetY, bounds, horizontal = false)
