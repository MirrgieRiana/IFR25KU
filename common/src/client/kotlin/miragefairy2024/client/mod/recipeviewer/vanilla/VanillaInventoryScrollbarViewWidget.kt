package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import miragefairy2024.MirageFairy2024
import miragefairy2024.client.util.drawNinePatchImage
import miragefairy2024.client.util.stack
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.InventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.XInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

private val BACKGROUND_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_background.png")
private val SCROLLER_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_scroller.png")
private val DISABLED_SCROLLER_TEXTURE = MirageFairy2024.identifier("textures/gui/inventory_scrollbar_scroller_disabled.png")

abstract class VanillaInventoryScrollbarViewWidget(view: InventoryScrollbarView, bounds: IntRectangle) : VanillaScrollbarViewWidget<InventoryScrollbarView>(view, bounds) {

    protected fun renderVerticalScrollbar(guiGraphics: GuiGraphics, x: Int, y: Int) {
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

}

class VanillaXInventoryScrollbarViewWidget(view: XInventoryScrollbarView, bounds: IntRectangle) : VanillaInventoryScrollbarViewWidget(view, bounds) {

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
        val pose: PoseStack = guiGraphics.pose()
        pose.stack {
            pose.translate(x.toFloat(), (y + height).toFloat(), 0F)
            pose.mulPose(Axis.ZN.rotationDegrees(90F))
            renderVerticalScrollbar(guiGraphics, 0, 0)
        }
    }

}

class VanillaYInventoryScrollbarViewWidget(view: YInventoryScrollbarView, bounds: IntRectangle) : VanillaInventoryScrollbarViewWidget(view, bounds) {

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
        renderVerticalScrollbar(guiGraphics, x, y)
    }

}
