package miragefairy2024.client.mod.recipeviewer.common

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import mirrg.kotlin.helium.atMost
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import org.joml.Matrix4f

object NinePatchImageViewRenderer : ViewRenderer<NinePatchImageView> {
    override fun render(view: NinePatchImageView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {

        val w0 = view.textureWidth
        val w1 = view.xStartSize
        val w2 = view.xMiddleSize
        val w3 = view.xEndSize
        val h0 = view.textureHeight
        val h1 = view.yStartSize
        val h2 = view.yMiddleSize
        val h3 = view.yEndSize
        val u0 = 0
        val u1 = w1
        val u2 = w1 + w2
        val v0 = 0
        val v1 = h1
        val v2 = h1 + h2
        val x0 = bounds.x
        val x1 = bounds.x + w1
        val x2 = bounds.x + bounds.sizeX - w3
        // val x3 = bounds.x + bounds.sizeX
        val y0 = bounds.y
        val y1 = bounds.y + h1
        val y2 = bounds.y + bounds.sizeY - h3
        // val y3 = bounds.y + bounds.sizeY

        RenderSystem.setShaderTexture(0, view.textureId)
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        val matrix: Matrix4f = graphics.pose().last().pose()
        val bufferBuilder: BufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)

        fun blit(x: Int, y: Int, width: Int, height: Int, u: Int, v: Int, uWidth: Int, vHeight: Int) {
            val minU = u.toFloat() / w0
            val maxU = (u + uWidth).toFloat() / w0
            val minV = v.toFloat() / h0
            val maxV = (v + vHeight).toFloat() / h0
            bufferBuilder.addVertex(matrix, x.toFloat(), y.toFloat(), 0.0F).setUv(minU, minV)
            bufferBuilder.addVertex(matrix, x.toFloat(), (y + height).toFloat(), 0.0F).setUv(minU, maxV)
            bufferBuilder.addVertex(matrix, (x + width).toFloat(), (y + height).toFloat(), 0.0F).setUv(maxU, maxV)
            bufferBuilder.addVertex(matrix, (x + width).toFloat(), y.toFloat(), 0.0F).setUv(maxU, minV)
        }

        // 角は元のサイズで描画するのだ～🌱
        blit(x0, y0, w1, h1, u0, v0, w1, h1)
        blit(x2, y0, w3, h1, u2, v0, w3, h1)
        blit(x0, y2, w1, h3, u0, v2, w1, h3)
        blit(x2, y2, w3, h3, u2, v2, w3, h3)

        if (view.repeat) {

            // 上下を反復描画するのだ～🌱
            run {
                var x = x1
                while (true) {
                    if (x >= x2) break
                    val w = w2 atMost x2 - x

                    blit(x, y0, w, h1, u1, v0, w, h1)
                    blit(x, y2, w, h3, u1, v2, w, h3)

                    x += w
                }
            }

            // 左右を反復描画するのだ～🌱
            run {
                var y = y1
                while (true) {
                    if (y >= y2) break
                    val h = h2 atMost y2 - y

                    blit(x0, y, w1, h, u0, v1, w1, h)
                    blit(x2, y, w3, h, u2, v1, w3, h)

                    y += h
                }
            }

            // 中央を反復描画するのだ～🌱
            run {
                var x = x1
                while (true) {
                    if (x >= x2) break
                    val w = w2 atMost x2 - x

                    var y = y1
                    while (true) {
                        if (y >= y2) break
                        val h = h2 atMost y2 - y

                        blit(x, y, w, h, u1, v1, w, h)

                        y += h
                    }

                    x += w
                }
            }
        } else {

            // 辺と中央を伸長描画するのだ～🌱
            if (x2 > x1) {
                blit(x1, y0, x2 - x1, h1, u1, v0, w2, h1)
                blit(x1, y2, x2 - x1, h3, u1, v2, w2, h3)
            }
            if (y2 > y1) {
                blit(x0, y1, w1, y2 - y1, u0, v1, w1, h2)
                blit(x2, y1, w3, y2 - y1, u2, v1, w3, h2)
            }
            if (x2 > x1 && y2 > y1) {
                blit(x1, y1, x2 - x1, y2 - y1, u1, v1, w2, h2)
            }

        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow())

    }
}
