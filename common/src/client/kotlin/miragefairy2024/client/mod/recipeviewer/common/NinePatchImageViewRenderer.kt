package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics

object NinePatchImageViewRenderer : ViewRenderer<NinePatchImageView> {
    override fun render(view: NinePatchImageView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {

        val w1 = view.xStartSize
        val w2 = view.xMiddleSize
        val w3 = view.xEndSize
        val h1 = view.yStartSize
        val h2 = view.yMiddleSize
        val h3 = view.yEndSize
        val u0 = 0
        val u1 = w1
        val u2 = w1 + w2
        val u3 = w1 + w2 + w3
        val v0 = 0
        val v1 = h1
        val v2 = h1 + h2
        val v3 = h1 + h2 + h3
        val x0 = bounds.x
        val x1 = bounds.x + w1
        val x2 = bounds.x + bounds.sizeX - w3
        // val x3 = bounds.x + bounds.sizeX
        val y0 = bounds.y
        val y1 = bounds.y + h1
        val y2 = bounds.y + bounds.sizeY - h3
        // val y3 = bounds.y + bounds.sizeY

        // 角
        graphics.blit(view.textureId, x0, y0, u0.toFloat(), v0.toFloat(), w1, h1, u3, v3)
        graphics.blit(view.textureId, x2, y0, u2.toFloat(), v0.toFloat(), w3, h1, u3, v3)
        graphics.blit(view.textureId, x0, y2, u0.toFloat(), v2.toFloat(), w1, h3, u3, v3)
        graphics.blit(view.textureId, x2, y2, u2.toFloat(), v2.toFloat(), w3, h3, u3, v3)

        // 上下
        run {
            var x = x1
            while (true) {
                if (x >= x2) break
                val w = w2 atMost x2 - x

                graphics.blit(view.textureId, x, y0, u1.toFloat(), v0.toFloat(), w, h1, u3, v3)
                graphics.blit(view.textureId, x, y2, u1.toFloat(), v2.toFloat(), w, h3, u3, v3)

                x += w
            }
        }

        // 左右
        run {
            var y = y1
            while (true) {
                if (y >= y2) break
                val h = h2 atMost y2 - y

                graphics.blit(view.textureId, x0, y, u0.toFloat(), v1.toFloat(), w1, h, u3, v3)
                graphics.blit(view.textureId, x2, y, u2.toFloat(), v1.toFloat(), w3, h, u3, v3)

                y += h
            }
        }

        // 中央
        run {
            var x = x1
            while (true) {
                if (x >= x2) break
                val w = w2 atMost x2 - x

                var y = y1
                while (true) {
                    if (y >= y2) break
                    val h = h2 atMost y2 - y

                    graphics.blit(view.textureId, x, y, u1.toFloat(), v1.toFloat(), w, h, u3, v3)

                    y += h
                }

                x += w
            }
        }

    }
}
