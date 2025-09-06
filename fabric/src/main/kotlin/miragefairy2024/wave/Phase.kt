package miragefairy2024.wave

import java.awt.image.BufferedImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

fun Spectrogram.removePhase(): Spectrogram {
    val image = BufferedImage(this.bufferedImage.width, this.bufferedImage.height, BufferedImage.TYPE_INT_RGB)
    repeat(this.bufferedImage.width) { x ->
        repeat(this.bufferedImage.height) { y ->
            image.setRGB(x, y, this.bufferedImage.getRGB(x, y) and 0x00FF00)
        }
    }
    return Spectrogram(image)
}

fun Spectrogram.generatePhaseSimple(): Spectrogram {
    val windowSize = (this.bufferedImage.height - 1) * 2 // 256

    val image = BufferedImage(this.bufferedImage.width, this.bufferedImage.height, BufferedImage.TYPE_INT_RGB)
    repeat(this.bufferedImage.width) { x ->
        repeat(this.bufferedImage.height) { imageY -> // height = 129, imageY = 0 .. 128
            val y = this.bufferedImage.height - 1 - imageY // 128 .. 0
            val w = 2.0 * PI / windowSize * y

            val inputRgb = this.bufferedImage.getRGB(x, imageY)
            val g = (inputRgb shr 8 and 0xFF).toDouble()

            val r = g * cos(w * x)
            val b = g * sin(w * x)
            val outputRgb = (r.roundToInt().coerceIn(-128, 127) + 128 and 0xFF shl 16) or
                (g.toInt().coerceIn(0, 255) and 0xFF shl 8) or
                (b.roundToInt().coerceIn(-128, 127) + 128 and 0xFF shl 0)

            image.setRGB(x, imageY, outputRgb)
        }
    }
    return Spectrogram(image)
}

fun Spectrogram.generatePhaseLegacy(): Spectrogram {
    val windowSize = (this.bufferedImage.height - 1) * 2 // 256

    val random = Random(0)

    // 位相の周期性によりウィンドウサイズを波長とするブザー音が発生するのを防ぐための位相の動的攪乱
    val oldPhases = (0 until this.bufferedImage.height).map { 2 * PI * random.nextDouble() }.toMutableList() // <0 ~ 2PI>[129]
    val newPhases = (0 until this.bufferedImage.height).map { 2 * PI * random.nextDouble() }.toMutableList() // <0 ~ 2PI>[129]

    // 「位相の動的攪乱をリセットする」処理の走るタイミング
    // 位相の攪乱のリセットは、ウィンドウサイズごとに行われる
    val phaseGradientResetOffsets = (0 until this.bufferedImage.height).map { random.nextInt(windowSize) } // <0 .. 255>[129]

    val image = BufferedImage(this.bufferedImage.width, this.bufferedImage.height, BufferedImage.TYPE_INT_RGB)
    repeat(this.bufferedImage.width) { x ->
        repeat(this.bufferedImage.height) { imageY -> // height = 129, imageY = 0 .. 128
            val y = this.bufferedImage.height - 1 - imageY // 128 .. 0

            // y = 0 のとき、 w = 0
            // y = 1 のとき、 w = 2PI / 256
            // y = 32 のとき、 w = 2PI / 8
            // y = 64 のとき、 w = 2PI / 4
            // y = 128 のとき、 w = 2PI / 2
            // w = 2PI / 256 * y
            val w = 2.0 * PI / windowSize * y

            val inputRgb = this.bufferedImage.getRGB(x, imageY)
            val g = (inputRgb shr 8 and 0xFF).toDouble()

            // 位相の動的攪乱の変化の度合い（サンプル位置）
            val phaseGradientResetPhase = (x + phaseGradientResetOffsets[y]) % windowSize // 0 .. 255

            // 位相の動的攪乱の変化の度合い（0～1）
            val phaseGradient = phaseGradientResetPhase / windowSize.toDouble() // 0 ~ 1

            // 位相
            val phase = oldPhases[y] * (1 - phaseGradient) + newPhases[y] * phaseGradient // 0 ~ 2PI

            val r = g * cos(phase + w * x)
            val b = g * sin(phase + w * x)
            val outputRgb = (r.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 16) or
                (g.toInt().coerceIn(0, 255) and 0xFF shl 8) or
                (b.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 0)

            // 位相の動的攪乱のリセットのタイミングである場合、リセット
            if (phaseGradientResetPhase == 0) {
                oldPhases[y] = newPhases[y]
                newPhases[y] = 2 * PI * random.nextDouble()
            }

            image.setRGB(x, imageY, outputRgb)
        }
    }
    return Spectrogram(image)
}

fun Spectrogram.generatePhaseGriffinLim(times: Int, toWaveform: (Spectrogram) -> Waveform, toSpectrogram: (Waveform) -> Spectrogram): Spectrogram {
    val width = this.bufferedImage.width
    val height = this.bufferedImage.height

    // 軽量化のために一旦正しい絶対値のテーブルを生成しておく
    val correctGTable = Array(height) { IntArray(width) }
    val doubleCorrectGTable = Array(height) { DoubleArray(width) }
    repeat(width) { x ->
        repeat(height) { y ->
            val thisRgb = this.bufferedImage.getRGB(x, y)
            val thisG = thisRgb shr 8 and 0xFF
            correctGTable[y][x] = thisG
            doubleCorrectGTable[y][x] = thisG.toDouble()
        }
    }

    var spectrogram = this
    repeat(times) {

        // 波形にして、スペクトログラムに戻す
        spectrogram = toSpectrogram(toWaveform(spectrogram))

        // そのスペクトログラムにおけるすべての画素について、絶対値を元のスペクトログラムの対応する画素と同じにする
        repeat(width) { x ->
            repeat(height) { y ->
                val rgb = spectrogram.bufferedImage.getRGB(x, y)

                val r = ((rgb shr 16 and 0xFF) - 128).toDouble()
                val g = (rgb shr 8 and 0xFF).toDouble()
                val b = ((rgb shr 0 and 0xFF) - 128).toDouble()

                val rate = if (g == 0.0) 10000.0 else doubleCorrectGTable[y][x] / g

                val trueR = r * rate
                val trueB = b * rate

                val trueRgb = (trueR.roundToInt().coerceIn(-128, 127) + 128 and 0xFF shl 16) or
                    (correctGTable[y][x].coerceIn(0, 255) and 0xFF shl 8) or
                    (trueB.roundToInt().coerceIn(-128, 127) + 128 and 0xFF shl 0)

                spectrogram.bufferedImage.setRGB(x, y, trueRgb)
            }
        }

    }
    return spectrogram
}
