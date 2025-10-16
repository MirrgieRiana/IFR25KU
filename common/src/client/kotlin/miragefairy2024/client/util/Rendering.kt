package miragefairy2024.client.util

import com.mojang.blaze3d.vertex.PoseStack
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
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
    this.matrixStack.last().pose().transformPosition(input0, output0)
    val input1 = Vector3f(maxX.toFloat(), (y + font.lineHeight).toFloat(), 0F)
    val output1 = Vector3f()
    this.matrixStack.last().pose().transformPosition(input1, output1)
    this.enableScissor(output0.x.roundToInt(), output0.y.roundToInt(), output1.x.roundToInt(), output1.y.roundToInt())
    try {
        this.drawString(font, text, x.roundToInt(), y, color, dropShadow)
    } finally {
        this.disableScissor()
    }
}
