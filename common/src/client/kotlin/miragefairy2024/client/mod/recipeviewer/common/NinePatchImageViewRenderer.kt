package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics

object NinePatchImageViewRenderer : ViewRenderer<NinePatchImageView> {
    override fun render(view: NinePatchImageView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {

        val sw0 = view.textureWidth
        val sw1 = view.xStartSize
        val sw2 = view.xMiddleSize
        val sw3 = view.xEndSize
        val sh0 = view.textureHeight
        val sh1 = view.yStartSize
        val sh2 = view.yMiddleSize
        val sh3 = view.yEndSize
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
        graphics.blit(view.textureId, dx0, dy0, sx0.toFloat(), sy0.toFloat(), sw1, sh1, sw0, sh0)
        graphics.blit(view.textureId, dx2, dy0, sx2.toFloat(), sy0.toFloat(), sw3, sh1, sw0, sh0)
        graphics.blit(view.textureId, dx0, dy2, sx0.toFloat(), sy2.toFloat(), sw1, sh3, sw0, sh0)
        graphics.blit(view.textureId, dx2, dy2, sx2.toFloat(), sy2.toFloat(), sw3, sh3, sw0, sh0)

        // 上下
        run {
            var dx = dx1
            while (true) {
                if (dx >= dx2) break
                val dw = if (view.repeatX) sw2 atMost dx2 - dx else dx2 - dx
                val sw = if (view.repeatX) dw else sw2

                graphics.blit(view.textureId, dx, dy0, dw, sh1, sx1.toFloat(), sy0.toFloat(), sw, sh1, sw0, sh0)
                graphics.blit(view.textureId, dx, dy2, dw, sh3, sx1.toFloat(), sy2.toFloat(), sw, sh3, sw0, sh0)

                dx += dw
            }
        }

        // 左右
        run {
            var dy = dy1
            while (true) {
                if (dy >= dy2) break
                val dh = if (view.repeatY) sh2 atMost dy2 - dy else dy2 - dy
                val sh = if (view.repeatY) dh else sh2

                graphics.blit(view.textureId, dx0, dy, sw1, dh, sx0.toFloat(), sy1.toFloat(), sw1, sh, sw0, sh0)
                graphics.blit(view.textureId, dx2, dy, sw3, dh, sx2.toFloat(), sy1.toFloat(), sw3, sh, sw0, sh0)

                dy += dh
            }
        }

        // 中央
        run {
            var dx = dx1
            while (true) {
                if (dx >= dx2) break
                val dw = if (view.repeatX) sw2 atMost dx2 - dx else dx2 - dx
                val sw = if (view.repeatX) dw else sw2

                var dy = dy1
                while (true) {
                    if (dy >= dy2) break
                    val dh = if (view.repeatY) sh2 atMost dy2 - dy else dy2 - dy
                    val sh = if (view.repeatY) dh else sh2

                    graphics.blit(view.textureId, dx, dy, dw, dh, sx1.toFloat(), sy1.toFloat(), sw, sh, sw0, sh0)

                    dy += dh
                }

                dx += dw
            }
        }

    }
}
