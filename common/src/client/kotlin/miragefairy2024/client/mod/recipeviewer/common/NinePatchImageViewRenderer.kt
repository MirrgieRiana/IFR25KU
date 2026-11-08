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

        if (view.repeat) {

            // 上下
            run {
                var dx = dx1
                while (true) {
                    if (dx >= dx2) break
                    val dw = sw2 atMost dx2 - dx

                    graphics.blit(view.textureId, dx, dy0, sx1.toFloat(), sy0.toFloat(), dw, sh1, sw0, sh0)
                    graphics.blit(view.textureId, dx, dy2, sx1.toFloat(), sy2.toFloat(), dw, sh3, sw0, sh0)

                    dx += dw
                }
            }

            // 左右
            run {
                var dy = dy1
                while (true) {
                    if (dy >= dy2) break
                    val dh = sh2 atMost dy2 - dy

                    graphics.blit(view.textureId, dx0, dy, sx0.toFloat(), sy1.toFloat(), sw1, dh, sw0, sh0)
                    graphics.blit(view.textureId, dx2, dy, sx2.toFloat(), sy1.toFloat(), sw3, dh, sw0, sh0)

                    dy += dh
                }
            }

            // 中央
            run {
                var dx = dx1
                while (true) {
                    if (dx >= dx2) break
                    val dw = sw2 atMost dx2 - dx

                    var dy = dy1
                    while (true) {
                        if (dy >= dy2) break
                        val dh = sh2 atMost dy2 - dy

                        graphics.blit(view.textureId, dx, dy, sx1.toFloat(), sy1.toFloat(), dw, dh, sw0, sh0)

                        dy += dh
                    }

                    dx += dw
                }
            }

        } else {

            // 上下
            if (dx2 > dx1) {
                graphics.blit(view.textureId, dx1, dy0, dx2 - dx1, sh1, sx1.toFloat(), sy0.toFloat(), sw2, sh1, sw0, sh0)
                graphics.blit(view.textureId, dx1, dy2, dx2 - dx1, sh3, sx1.toFloat(), sy2.toFloat(), sw2, sh3, sw0, sh0)
            }

            // 左右
            if (dy2 > dy1) {
                graphics.blit(view.textureId, dx0, dy1, sw1, dy2 - dy1, sx0.toFloat(), sy1.toFloat(), sw1, sh2, sw0, sh0)
                graphics.blit(view.textureId, dx2, dy1, sw3, dy2 - dy1, sx2.toFloat(), sy1.toFloat(), sw3, sh2, sw0, sh0)
            }

            // 中央
            if (dx2 > dx1 && dy2 > dy1) {
                graphics.blit(view.textureId, dx1, dy1, dx2 - dx1, dy2 - dy1, sx1.toFloat(), sy1.toFloat(), sw2, sh2, sw0, sh0)
            }

        }

    }
}
