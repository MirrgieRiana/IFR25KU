package miragefairy2024.wave

import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.io.File
import kotlin.math.roundToInt

object GenerateV2 {
    private val logger = getLogger(GenerateV2::class.java)

    private val samplesPerSecond = 48000
    private val pixelsPerSecond = 128
    private val bits = 8
    private val amplifier = 1600.0

    private val imageHeight = (1 shl bits) / 2 + 1
    private val bias = (1 shl bits) - 1

    // 内部画像の幅のうち、255は固定の部分に使われる
    // 内部画像の幅から-255した部分の長さが実際のサンプル数に相当する
    // 画像の幅が+128される度にサンプル数が+48000になってほしい
    // generateしてdegenerateしたときに元の幅を維持してほしい
    // 画像の幅が変わっても1画素あたりの波形の長さが変わらないようにしたい
    // 画像幅が128のとき、サンプル数は48000であってほしい

    // サンプル数 = 内部画像幅 - 255 = (画像幅 / 128 * 48000 + a) - 255
    // 48000 = (128 / 128 * 48000 + a) - 255
    // ⇒a = 48000 + 255 - 128 / 128  * 48000
    //     = 255
    // このとき、
    // (128 / 128 * 48000 + 255) - 255 = 48000
    // (256 / 128 * 48000 + 255) - 255 = 96000

    // 内部画像幅 = 画像幅 / 128 * 48000 + a
    // 画像幅 = (内部画像幅 - a) / 48000 * 128

    fun generate(inputFile: File, outputFile: File, dumpWav: Boolean = false) {
        inputFile
            .readSpectrogram()
            .also { logger.info("Input Image Size: ${it.bufferedImage.width} x ${it.bufferedImage.height}") } // 128 x 129

            .fromLogScale()

            .let { it.resize((it.bufferedImage.width.toDouble() / pixelsPerSecond.toDouble() * samplesPerSecond.toDouble() + bias).roundToInt(), imageHeight) }
            .also { logger.info("Internal Image Size: ${it.bufferedImage.width} x ${it.bufferedImage.height}") } // 48255 x 129

            .generatePhaseLegacy()
            .generatePhaseGriffinLim(5, { it.toWaveform(bits, 1.0) }, { it.toSpectrogram(bits, 1.0) })

            .toWaveform(bits, 1 / amplifier)
            .also { logger.info("Output Waveform Length: ${it.doubleArray.size} samples") } // 48000

            .toWavByteArray()
            .also {
                if (dumpWav) it.writeTo(File("${outputFile.path}.wav").also { it.mkdirsParentOrThrow() })
            }
            .toOggAsWav() // ここでサンプル数が増えていることに注意
            .writeTo(outputFile.also { it.mkdirsParentOrThrow() })
    }

    fun degenerate(inputFile: File, outputFile: File) {
        inputFile
            .readBytes()
            .toWavAsOgg()
            .toWaveformAsWav()
            .also {
                if (it.doubleArray.size > samplesPerSecond * 10) throw RuntimeException("too long: ${inputFile.name} (${it.doubleArray.size.toDouble() / samplesPerSecond.toDouble()}s)")
            }
            .also { logger.info("Input Waveform Length: ${it.doubleArray.size} samples") } // 48000

            .toSpectrogram(bits, 1 / amplifier)
            .also { logger.info("Internal Image Size: ${it.bufferedImage.width} x ${it.bufferedImage.height}") } // 48255 x 129

            .removePhase()

            .let { it.resize(((it.bufferedImage.width.toDouble() - bias) / samplesPerSecond.toDouble() * pixelsPerSecond.toDouble()).roundToInt(), imageHeight) }
            .also { logger.info("Output Image Size: ${it.bufferedImage.width} x ${it.bufferedImage.height}") } // 128 x 129

            .toLogScale()

            .writeTo(outputFile.also { it.mkdirsParentOrThrow() })
    }
}
