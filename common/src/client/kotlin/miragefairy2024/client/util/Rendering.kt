package miragefairy2024.client.util

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import mirrg.kotlin.helium.max
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

inline fun <T> PoseStack.stack(block: () -> T): T {
    this.pushPose()
    try {
        return block()
    } finally {
        this.popPose()
    }
}

/** 単一行の本文が幅を超える場合、省略記号の実測幅を確保して文字単位で切り詰めるのだ～🌱 */
fun Font.ellipsize(text: FormattedText, maxWidth: Int, ellipsis: FormattedText): FormattedCharSequence {
    if (maxWidth <= 0) return FormattedCharSequence.EMPTY
    // componentStyleAtWidth は初めて幅を超えたときの Style を返すから、null の場合は幅に収まってるのだ～🌱
    if (this.splitter.componentStyleAtWidth(text, maxWidth) == null) return Language.getInstance().getVisualOrder(text)
    val ellipsisWidth = this.width(ellipsis)
    val result = if (ellipsisWidth > maxWidth) {
        this.substrByWidth(ellipsis, maxWidth)
    } else {
        FormattedText.composite(this.substrByWidth(text, maxWidth - ellipsisWidth), ellipsis)
    }
    return Language.getInstance().getVisualOrder(result)
}

fun GuiGraphics.drawRightText(textRenderer: Font, text: String, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawString(textRenderer, text, rightX - textRenderer.width(text), y, color, shadow)
}

fun GuiGraphics.drawRightText(textRenderer: Font, text: Component, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    val orderedText = text.visualOrderText
    this.drawString(textRenderer, orderedText, rightX - textRenderer.width(orderedText), y, color, shadow)
}

fun GuiGraphics.drawRightText(textRenderer: Font, text: FormattedCharSequence, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawString(textRenderer, text, rightX - textRenderer.width(text), y, color, shadow)
}

// net.minecraft.client.gui.components.AbstractWidget#renderScrollingString
fun GuiGraphics.drawScrollingString(font: Font, text: FormattedCharSequence, centerX: Int, minX: Int, minY: Int, maxX: Int, maxY: Int, color: Int) {
    val textWidth = font.width(text)
    val y = (minY + maxY - 9) / 2 + 1
    val areaWidth = maxX - minX
    if (textWidth > areaWidth) {
        val overflowX = textWidth - areaWidth
        val seconds = System.nanoTime().toDouble() / 1_000_000_000.0
        val speed = overflowX.toDouble() * 0.5 max 3.0
        val delta = sin((PI / 2) * cos((PI * 2) * seconds / speed)) / 2.0 + 0.5
        val leftOffset = Mth.lerp(delta, 0.0, overflowX.toDouble())
        this.enableScissor(minX, minY, maxX, maxY)
        this.drawString(font, text, minX - leftOffset.toInt(), y, color)
        this.disableScissor()
    } else {
        val x = Mth.clamp(centerX, minX + textWidth / 2, maxX - textWidth / 2)
        this.drawCenteredString(font, text, x, y, color)
    }
}

// net.minecraft.client.gui.components.AbstractWidget#renderScrollingString
fun GuiGraphics.drawScrollingString(
    font: Font,
    text: FormattedCharSequence,
    minX: Int,
    maxX: Int,
    y: Int,
    startNanoTime: Long,
    maxSpeed: Double,
    minPeriod: Double,
    color: Int,
    dropShadow: Boolean,
) {
    val areaWidth = maxX - minX
    val textWidth = font.width(text)
    val overflowX = textWidth - areaWidth
    val x = if (overflowX > 0) {
        val seconds = (System.nanoTime() - startNanoTime).toDouble() / 1_000_000_000.0

        //       x  = overflowX * (0.5 - cos(2 * PI * (seconds / period)) * 0.5)
        //       x' = overflowX * (0   + sin(2 * PI * (seconds / period)) * 0.5) * (2 * PI * (1 / period))
        // maxSpeed = overflowX * (0   + 1                                * 0.5) * (2 * PI * (1 / period))
        // maxSpeed = overflowX * (                                         0.5) * (2 * PI      / period )
        // maxSpeed = overflowX *                                           0.5  *  2 * PI      / period
        // maxSpeed = overflowX *                                                       PI      / period
        //   period = overflowX *                                                       PI      / maxSpeed
        val period = overflowX.toDouble() * PI / maxSpeed atLeast minPeriod
        val delta = overflowX.toDouble() * (0.5 - cos(2 * PI * (seconds / period)) * 0.5)

        minX.toDouble() - delta
    } else {
        minX.toDouble()
    }
    val input0 = Vector3f(minX.toFloat(), y.toFloat(), 0F)
    val output0 = Vector3f()
    this.pose().last().pose().transformPosition(input0, output0)
    val input1 = Vector3f(maxX.toFloat(), (y + font.lineHeight).toFloat(), 0F)
    val output1 = Vector3f()
    this.pose().last().pose().transformPosition(input1, output1)
    this.enableScissor(output0.x.roundToInt(), output0.y.roundToInt(), output1.x.roundToInt(), output1.y.roundToInt())
    try {
        this.drawString(font, text, x.roundToInt(), y, color, dropShadow)
    } finally {
        this.disableScissor()
    }
}

fun GuiGraphics.drawNinePatchImage(bounds: IntRectangle, ninePatchTextureCard: NinePatchTextureCard) {

    val textureId = ninePatchTextureCard.texture
    val repeatX = ninePatchTextureCard.repeatX
    val repeatY = ninePatchTextureCard.repeatY
    val sw0 = ninePatchTextureCard.textureWidth.toFloat()
    val sw1 = ninePatchTextureCard.xStartSize
    val sw2 = ninePatchTextureCard.xMiddleSize
    val sw3 = ninePatchTextureCard.xEndSize
    val sh0 = ninePatchTextureCard.textureHeight.toFloat()
    val sh1 = ninePatchTextureCard.yStartSize
    val sh2 = ninePatchTextureCard.yMiddleSize
    val sh3 = ninePatchTextureCard.yEndSize
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

    RenderSystem.setShaderTexture(0, textureId)
    RenderSystem.setShader(GameRenderer::getPositionTexShader)
    val matrix: Matrix4f = this.pose().last().pose()
    val bufferBuilder: BufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)

    /** 9 分割画像のパッチ 1 枚を描くための 4 頂点をバッファに追加するのだ～🌱 */
    fun addQuad(x: Int, y: Int, width: Int, height: Int, u: Int, v: Int, uWidth: Int, vHeight: Int) {
        val minU = u.toFloat() / sw0
        val maxU = (u.toFloat() + uWidth) / sw0
        val minV = v.toFloat() / sh0
        val maxV = (v.toFloat() + vHeight) / sh0
        bufferBuilder.addVertex(matrix, x.toFloat(), y.toFloat(), 0.0F).setUv(minU, minV)
        bufferBuilder.addVertex(matrix, x.toFloat(), (y + height).toFloat(), 0.0F).setUv(minU, maxV)
        bufferBuilder.addVertex(matrix, (x + width).toFloat(), (y + height).toFloat(), 0.0F).setUv(maxU, maxV)
        bufferBuilder.addVertex(matrix, (x + width).toFloat(), y.toFloat(), 0.0F).setUv(maxU, minV)
    }

    // 角
    addQuad(dx0, dy0, sw1, sh1, sx0, sy0, sw1, sh1)
    addQuad(dx2, dy0, sw3, sh1, sx2, sy0, sw3, sh1)
    addQuad(dx0, dy2, sw1, sh3, sx0, sy2, sw1, sh3)
    addQuad(dx2, dy2, sw3, sh3, sx2, sy2, sw3, sh3)

    // 上下
    run {
        var dx = dx1
        while (true) {
            if (dx >= dx2) break
            val dw = if (repeatX) sw2 atMost dx2 - dx else dx2 - dx
            val sw = if (repeatX) dw else sw2

            addQuad(dx, dy0, dw, sh1, sx1, sy0, sw, sh1)
            addQuad(dx, dy2, dw, sh3, sx1, sy2, sw, sh3)

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

            addQuad(dx0, dy, sw1, dh, sx0, sy1, sw1, sh)
            addQuad(dx2, dy, sw3, dh, sx2, sy1, sw3, sh)

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

                addQuad(dx, dy, dw, dh, sx1, sy1, sw, sh)

                dy += dh
            }

            dx += dw
        }
    }

    BufferUploader.drawWithShader(bufferBuilder.buildOrThrow())

}
