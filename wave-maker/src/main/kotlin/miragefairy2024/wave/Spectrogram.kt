package miragefairy2024.wave

import mirrg.kotlin.hydrogen.blueOfRgb
import mirrg.kotlin.hydrogen.greenOfRgb
import mirrg.kotlin.hydrogen.redOfRgb
import mirrg.kotlin.hydrogen.rgbOf
import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.log
import kotlin.math.pow

class Spectrogram(val bufferedImage: BufferedImage)

private val logger = getLogger(Spectrogram::class.java)

fun Waveform.toSpectrogram(bits: Int, m: Double): Spectrogram {
    check(bits >= 4)
    val windowSize = 1 shl bits
    logger.info("Window Size: $windowSize")
    val length = this.doubleArray.size
    logger.info("Input Waveform Length: $length")
    val width = length + windowSize - 1
    val height = windowSize / 2 + 1

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    repeat(width) { x ->

        // windowSize = 4
        // length = 10
        //
        //        0
        //        ##########
        // x=0 ***#        |      <- range = -3, -2, -1, 0
        // x=1  **##       |               = -3 until 1
        // x=2   *###      |               = -3 until -3 + 4
        // x=3    ####     |               = -(windowSize - 1) until -(windowSize - 1) + windowSize
        // x=9          ####               = -(windowSize - 1) until -windowSize + 1 + windowSize
        // x=10          ###*              = -(windowSize - 1) until 1
        // x=11           ##**             = -(windowSize - 1) .. 0
        // x=12            #***            = x - (windowSize - 1) .. x
        //
        // 開始が負の場合、その分だけcountを減らし、destPosを増やす
        // 終了が余る場合、その分だけcountを減らす
        // xが1増えるごとに、開始と終了が1増える
        val subWaveform = DoubleArray(windowSize)
        run {
            var start = x - (windowSize - 1)
            var end = x
            var count = windowSize
            var dest = 0
            if (start < 0) {
                val amount = -start
                start = 0
                count -= amount
                dest += amount
            } else if (end >= length) {
                // length = 10; end = 10; amount = 1 = 10 - 10 + 1
                // length = 10; end = 11; amount = 2 = 11 - 10 + 1
                // length = 11; end = 10; amount = 0 = 10 - 11 + 1 = end - length + 1
                val amount = end - length + 1
                end = length - 1
                count -= amount
            }
            System.arraycopy(this.doubleArray, start, subWaveform, dest, end - start + 1)
        }

        val complexWindowedSubWaveform = subWaveform.mapIndexed { i, it ->
            val w = hanningWindow(i.toDouble(), windowSize.toDouble())
            Complex(it * w, 0.0)
        }.toTypedArray()
        val outputSpectrum = complexWindowedSubWaveform.fft()

        repeat(height) { y ->
            val r = outputSpectrum[y].re * m
            val g = outputSpectrum[y].abs() * m
            val b = outputSpectrum[y].im * m
            val rgb = rgbOf(
                r.toInt().coerceIn(-128, 127) + 128,
                g.toInt().coerceIn(0, 255),
                b.toInt().coerceIn(-128, 127) + 128,
            )
            image.setRGB(x, height - 1 - y, rgb)
        }

    }

    return Spectrogram(image)
}

fun Spectrogram.toWaveform(bits: Int, m: Double): Waveform {
    check(bits >= 4) // 8
    val windowSize = 1 shl bits // 256
    check(this.bufferedImage.width >= windowSize)
    check(this.bufferedImage.height == windowSize / 2 + 1) // 129

    val doubleArrays = mutableListOf<DoubleArray>()
    val zero = Complex(0.0, 0.0)
    repeat(this.bufferedImage.width) { x -> // 0 .. width - 1

        val spectrum = Array(windowSize) { zero } // Complex[256]
        repeat(this.bufferedImage.height) { y -> // 0 .. 128
            val rgb = this.bufferedImage.getRGB(x, this.bufferedImage.height - 1 - y) // (y = 128 .. 0)
            val r = (rgb.redOfRgb - 128).toDouble() / m
            //val g = (rgb.greenOfRgb - 128).toDouble() / m
            val b = (rgb.blueOfRgb - 128).toDouble() / m

            spectrum[y] = Complex(r, b) // [index = 0 .. 128]
            if (y != 0 && y != this.bufferedImage.height - 1) spectrum[windowSize - y] = Complex(r, -b) // [index = 256 .. 128]
            // y = 0 の画素は直流分を表すため、1か所にしかマッピングされない
            // 虚軸はインデックスが逆になると符号が入れ替わり、実軸は同じ値になる
            // y = 0, 128 において、必ず b = 0 になる性質がある
            // y = 128 のとき、普通には complexes[128] に2度代入することになるが、 b = 0 であるため等価となる
        }

        doubleArrays += spectrum.ifft().map { it.re }.toDoubleArray()
    }

    val length = doubleArrays.size - windowSize + 1 // width - 255
    val doubleArray = DoubleArray(length)
    repeat(length) { x ->
        var value = 0.0
        repeat(windowSize) { i -> // 0 .. 255
            val w = hanningWindow(i.toDouble(), windowSize.toDouble())
            value += doubleArrays[x + i][windowSize - 1 - i] * w
        }
        doubleArray[x] = value / windowSize * 2
    }

    return Waveform(doubleArray)
}

fun Spectrogram.resizeHorizontal(imageHeight: Int): Spectrogram {
    val image = BufferedImage(this.bufferedImage.width, imageHeight, BufferedImage.TYPE_INT_RGB)
    val rate = this.bufferedImage.height / imageHeight.toDouble() // 3 / 4 = 0.75
    repeat(this.bufferedImage.width) { x ->
        repeat(imageHeight) { imageY0 -> // 1

            /*
             *             thisY0      thisY1
             * thisY0f         thisY1f
             * 0               1               2               3  thisY
             * |           ====|========       |               |  this    height=3
             * |           |===========|           |           |  image   height=4
             * 0          [1]          2           3           4  imageY
             *             imageY0     imageY1
             * 0           0.75        1.5         2.25        3  thisY
             *
             * thisY = imageY * 0.75 = imageY * (this.height / image.height) = imageY * rate
             */
            val imageY1 = imageY0 + 1 // 2
            val thisY0 = imageY0 * rate // 0.75
            val thisY1 = imageY1 * rate // 1.5
            val thisY0f = thisY0.toInt() // 0
            val thisY1f = thisY1.toInt() // 1

            var count = 0.0
            var sumR = 0.0
            var sumG = 0.0
            var sumB = 0.0

            fun add(c: Double, thisY: Int) {
                val inputRgb = this.bufferedImage.getRGB(x, thisY)
                count += c
                sumR += inputRgb.redOfRgb * c
                sumG += inputRgb.greenOfRgb * c
                sumB += inputRgb.blueOfRgb * c
            }

            if (thisY0f == thisY1f) {
                // 解像度が上がるためimage画素のすべての範囲が単一のthis画素にマッピングされる場合
                /*
                 *                         thisY0      thisY1
                 *                     thisY0f
                 *                     thisY1f
                 * 0                   1                   2                   3  thisY
                 * |                   |   =============   |                   |  this    height=3
                 * |           |           |===========|           |           |  image   height=5
                 * 0           1          [2]          3           4           5  imageY
                 *                         imageY0     imageY1
                 */
                add(thisY1 - thisY0, thisY0f)
            } else {
                // 対応するthisの画素が始端・0ピクセル以上の中間・終端に分かれる場合
                /*
                 *                     thisY0              thisY1
                 *             thisY0f                 thisY1f
                 * 0           1           2           3           4           5  thisY
                 * |           |       ====|===========|====       |           |  this    height=5
                 * |                   |===================|                   |  image   height=3
                 * 0                  [1]                  2                   3  imageY
                 *                     imageY0             imageY1
                 */
                add((thisY0f + 1) - thisY0, thisY0f)
                @Suppress("ReplaceRangeToWithRangeUntil")
                (thisY0f + 1..thisY1f - 1).forEach { thisYf ->
                    add(1.0, thisYf)
                }
                if (thisY1f < this.bufferedImage.height) add(thisY1 - thisY1f, thisY1f)
            }

            val r = sumR / count
            val g = sumG / count
            val b = sumB / count
            val outputRgb = rgbOf(
                r.toInt().coerceIn(0, 255),
                g.toInt().coerceIn(0, 255),
                b.toInt().coerceIn(0, 255),
            )

            image.setRGB(x, imageY0, outputRgb)
        }
    }
    return Spectrogram(image)
}

fun Spectrogram.resizeVertical(imageWidth: Int): Spectrogram {
    val image = BufferedImage(imageWidth, this.bufferedImage.height, BufferedImage.TYPE_INT_RGB)
    val rate = this.bufferedImage.width / imageWidth.toDouble() // 3 / 4 = 0.75
    repeat(this.bufferedImage.height) { y ->
        repeat(imageWidth) { imageX0 -> // 1

            /*
             *             thisX0      thisX1
             * thisX0f         thisX1f
             * 0               1               2               3  thisX
             * |           ====|========       |               |  this    height=3
             * |           |===========|           |           |  image   height=4
             * 0          [1]          2           3           4  imageX
             *             imageX0     imageX1
             * 0           0.75        1.5         2.25        3  thisX
             *
             * thisX = imageX * 0.75 = imageX * (this.height / image.height) = imageX * rate
             */
            val imageX1 = imageX0 + 1 // 2
            val thisX0 = imageX0 * rate // 0.75
            val thisX1 = imageX1 * rate // 1.5
            val thisX0f = thisX0.toInt() // 0
            val thisX1f = thisX1.toInt() // 1

            var count = 0.0
            var sumR = 0.0
            var sumG = 0.0
            var sumB = 0.0

            fun add(c: Double, thisX: Int) {
                val inputRgb = this.bufferedImage.getRGB(thisX, y)
                count += c
                sumR += inputRgb.redOfRgb * c
                sumG += inputRgb.greenOfRgb * c
                sumB += inputRgb.blueOfRgb * c
            }

            if (thisX0f == thisX1f) {
                // 解像度が上がるためimage画素のすべての範囲が単一のthis画素にマッピングされる場合
                /*
                 *                         thisX0      thisX1
                 *                     thisX0f
                 *                     thisX1f
                 * 0                   1                   2                   3  thisX
                 * |                   |   =============   |                   |  this    height=3
                 * |           |           |===========|           |           |  image   height=5
                 * 0           1          [2]          3           4           5  imageX
                 *                         imageX0     imageX1
                 */
                add(thisX1 - thisX0, thisX0f)
            } else {
                // 対応するthisの画素が始端・0ピクセル以上の中間・終端に分かれる場合
                /*
                 *                     thisX0              thisX1
                 *             thisX0f                 thisX1f
                 * 0           1           2           3           4           5  thisX
                 * |           |       ====|===========|====       |           |  this    height=5
                 * |                   |===================|                   |  image   height=3
                 * 0                  [1]                  2                   3  imageX
                 *                     imageX0             imageX1
                 */
                add((thisX0f + 1) - thisX0, thisX0f)
                @Suppress("ReplaceRangeToWithRangeUntil")
                (thisX0f + 1..thisX1f - 1).forEach { thisXf ->
                    add(1.0, thisXf)
                }
                if (thisX1f < this.bufferedImage.width) add(thisX1 - thisX1f, thisX1f)
            }

            val r = sumR / count
            val g = sumG / count
            val b = sumB / count
            val outputRgb = rgbOf(
                r.toInt().coerceIn(0, 255),
                g.toInt().coerceIn(0, 255),
                b.toInt().coerceIn(0, 255),
            )

            image.setRGB(imageX0, y, outputRgb)
        }
    }
    return Spectrogram(image)
}

fun Spectrogram.resize(imageWidth: Int, imageHeight: Int) = this.resizeHorizontal(imageHeight).resizeVertical(imageWidth)

fun Spectrogram.fromLogScale(): Spectrogram {
    val image = BufferedImage(this.bufferedImage.width, this.bufferedImage.height, BufferedImage.TYPE_INT_RGB)
    repeat(this.bufferedImage.width) { x ->
        repeat(this.bufferedImage.height) { y ->
            // 元が255だったときに255
            // 125下がるごとに0.1倍でいく
            // 1で約0.01倍になる
            // 元が0だった場合は0
            fun f(x: Double): Double {
                if (x == 0.0) return 0.0
                return 255 * 0.1.pow((255 - x) / 125)
            }

            val rgb = this.bufferedImage.getRGB(x, y)
            val r = f(rgb.redOfRgb.toDouble()).toInt().coerceIn(0, 255)
            val g = f(rgb.greenOfRgb.toDouble()).toInt().coerceIn(0, 255)
            val b = f(rgb.blueOfRgb.toDouble()).toInt().coerceIn(0, 255)
            image.setRGB(x, y, rgbOf(r, g, b))
        }
    }
    return Spectrogram(image)
}

fun Spectrogram.toLogScale(): Spectrogram {
    val image = BufferedImage(this.bufferedImage.width, this.bufferedImage.height, BufferedImage.TYPE_INT_RGB)
    repeat(this.bufferedImage.width) { x ->
        repeat(this.bufferedImage.height) { y ->
            // 元が255だったときに255
            // 0.1倍になるごとに125下がる
            // 元が0だった場合は0
            fun f(x: Double): Double {
                if (x == 0.0) return 0.0
                return 255 - log(x / 255.0, 0.1) * 125.0
            }

            val rgb = this.bufferedImage.getRGB(x, y)
            val r = f(rgb.redOfRgb.toDouble()).toInt().coerceIn(0, 255)
            val g = f(rgb.greenOfRgb.toDouble()).toInt().coerceIn(0, 255)
            val b = f(rgb.blueOfRgb.toDouble()).toInt().coerceIn(0, 255)
            image.setRGB(x, y, rgbOf(r, g, b))
        }
    }
    return Spectrogram(image)
}

fun File.readSpectrogram(): Spectrogram = Spectrogram(ImageIO.read(this))

fun File.writeSpectrogram(spectrogram: Spectrogram) {
    ImageIO.write(spectrogram.bufferedImage, "png", this)
}

fun Spectrogram.writeTo(file: File) = file.writeSpectrogram(this)
