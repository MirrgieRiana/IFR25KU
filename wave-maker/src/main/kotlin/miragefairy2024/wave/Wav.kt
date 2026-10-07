package miragefairy2024.wave

import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

private object Wav

private val logger = getLogger(Wav::class.java)

fun ByteArray.toWaveformAsWav(): Waveform {
    AudioSystem.getAudioInputStream(ByteArrayInputStream(this)).use { input ->
        logger.info("${input.format}")
        logger.info("${input.format.sampleRate}")
        logger.info("${input.format.frameSize}")

        val inputBytes = input.readAllBytes()
        val length = inputBytes.size / input.format.frameSize
        val outputDoubles = DoubleArray(length)
        repeat(length) { i ->

            val a = i * input.format.frameSize
            var value = 0.0
            repeat(input.format.channels) { channel ->
                value += when (input.format.sampleSizeInBits) {
                    8 -> inputBytes[a + 1 * channel + 0].toInt().toDouble()

                    16 -> {
                        val b0 = inputBytes[a + 2 * channel + 0]
                        val b1 = inputBytes[a + 2 * channel + 1]
                        ((b0.toInt() and 0xFF) or (b1.toInt() shl 8)).toDouble()
                    }

                    else -> throw RuntimeException("Bits not supported: ${input.format.sampleSizeInBits}")
                }
            }
            value /= input.format.channels

            outputDoubles[i] = value
        }

        return Waveform(outputDoubles)
    }
}

fun Waveform.toWavByteArray(): ByteArray {

    val bytes = ByteArray(this.doubleArray.size * 2)
    repeat(this.doubleArray.size) { i ->
        val int = this.doubleArray[i].toInt().coerceIn(-32768 until 32768)
        bytes[2 * i + 0] = (int shr 0 and 0xFF).toByte()
        bytes[2 * i + 1] = (int shr 8 and 0xFF).toByte()
    }

    val format = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        48000.0F,
        16,
        1,
        2,
        48000.0F,
        false,
    )
    val output = ByteArrayOutputStream()
    AudioSystem.write(AudioInputStream(ByteArrayInputStream(bytes), format, this.doubleArray.size.toLong()), AudioFileFormat.Type.WAVE, output)
    return output.toByteArray()
}
