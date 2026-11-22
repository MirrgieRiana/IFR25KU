package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.Slot
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

object NinePatchImageViewRenderer : ViewRenderer<NinePatchImageView, Unit> {
    override fun createState(view: NinePatchImageView) = Unit
    override fun render(view: NinePatchImageView, state: Slot<Unit>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        renderNinePatchImage(
            graphics,
            bounds,
            view.textureId,
            view.textureWidth,
            view.xStartSize,
            view.xMiddleSize,
            view.xEndSize,
            view.textureHeight,
            view.yStartSize,
            view.yMiddleSize,
            view.yEndSize,
            view.repeatX,
            view.repeatY,
        )
    }
}

fun renderNinePatchImage(
    graphics: GuiGraphics,
    bounds: IntRectangle,
    textureId: ResourceLocation,
    textureWidth: Int,
    xStartSize: Int,
    xMiddleSize: Int,
    xEndSize: Int,
    textureHeight: Int,
    yStartSize: Int,
    yMiddleSize: Int,
    yEndSize: Int,
    repeatX: Boolean,
    repeatY: Boolean,
) {

    val sw0 = textureWidth
    val sw1 = xStartSize
    val sw2 = xMiddleSize
    val sw3 = xEndSize
    val sh0 = textureHeight
    val sh1 = yStartSize
    val sh2 = yMiddleSize
    val sh3 = yEndSize
    val sx0 = 0
    val sx1 = sw1
    val sx2 = sw1 + sw2
    val sy0 = 0
    val sy1 = sh1
    val sy2 = sh1 + sh2
    val dx0 = bounds.x
    val dx1 = bounds.x + sw1
    val dx2 = bounds.x + bounds.sizeX - sw3
    // val dx3 = bounds.x + bounds.sizeX
    val dy0 = bounds.y
    val dy1 = bounds.y + sh1
    val dy2 = bounds.y + bounds.sizeY - sh3
    // val dy3 = bounds.y + bounds.sizeY

    // 角
    graphics.blit(textureId, dx0, dy0, sx0.toFloat(), sy0.toFloat(), sw1, sh1, sw0, sh0)
    graphics.blit(textureId, dx2, dy0, sx2.toFloat(), sy0.toFloat(), sw3, sh1, sw0, sh0)
    graphics.blit(textureId, dx0, dy2, sx0.toFloat(), sy2.toFloat(), sw1, sh3, sw0, sh0)
    graphics.blit(textureId, dx2, dy2, sx2.toFloat(), sy2.toFloat(), sw3, sh3, sw0, sh0)

    // 上下
    run {
        var dx = dx1
        while (true) {
            if (dx >= dx2) break
            val dw = if (repeatX) sw2 atMost dx2 - dx else dx2 - dx
            val sw = if (repeatX) dw else sw2

            graphics.blit(textureId, dx, dy0, dw, sh1, sx1.toFloat(), sy0.toFloat(), sw, sh1, sw0, sh0)
            graphics.blit(textureId, dx, dy2, dw, sh3, sx1.toFloat(), sy2.toFloat(), sw, sh3, sw0, sh0)

            dx += dw
        }
    }

    // 左右
    run {
        var dy = dy1
        while (true) {
            if (dy >= dy2) break
            val dh = if (repeatY) sh2 atMost dy2 - dy else dy2 - dy
            val sh = if (repeatY) dh else sh2

            graphics.blit(textureId, dx0, dy, sw1, dh, sx0.toFloat(), sy1.toFloat(), sw1, sh, sw0, sh0)
            graphics.blit(textureId, dx2, dy, sw3, dh, sx2.toFloat(), sy1.toFloat(), sw3, sh, sw0, sh0)

            dy += dh
        }
    }

    // 中央
    run {
        var dx = dx1
        while (true) {
            if (dx >= dx2) break
            val dw = if (repeatX) sw2 atMost dx2 - dx else dx2 - dx
            val sw = if (repeatX) dw else sw2

            var dy = dy1
            while (true) {
                if (dy >= dy2) break
                val dh = if (repeatY) sh2 atMost dy2 - dy else dy2 - dy
                val sh = if (repeatY) dh else sh2

                graphics.blit(textureId, dx, dy, dw, dh, sx1.toFloat(), sy1.toFloat(), sw, sh, sw0, sh0)

                dy += dh
            }

            dx += dw
        }
    }

}
