package miragefairy2024.wave

import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.io.File

object GenerateV1 {
    private val logger = getLogger(GenerateV1::class.java)

    private val samplesPerSecond = 48000

    fun generate(inputFile: File) {
        logger.info("inputFile        = $inputFile")

        when (inputFile.extension) {
            "wav" -> {
                inputFile
                    .readBytes()
                    .toWaveformAsWav()
                    .toSpectrogram(8, 1 / 4000.0)
                    .writeTo(inputFile.resolveSibling("${inputFile.nameWithoutExtension}.png"))
            }

            "png" -> {
                val m = """(.*)\.(\d+)b\.(\d+)ms\.(\d+)a\.scr\.png""".toRegex().matchEntire(inputFile.name)!!


                val baseName = m.groups[1]?.value!!
                logger.info("baseName         = $baseName")
                val outputFile = inputFile.resolveSibling("$baseName.ogg")
                logger.info("outputFile       = $outputFile")

                val bits = m.groups[2]?.value!!.toInt()
                logger.info("bits             = $bits")
                val windowSize = 1 shl bits
                logger.info("windowSize       = $windowSize")
                val imageHeight = windowSize / 2 + 1
                logger.info("imageHeight      = $imageHeight")

                logger.info("samplesPerSecond = $samplesPerSecond")

                val duration = m.groups[3]?.value!!.toDouble() * 0.001
                logger.info("duration         = $duration")
                val imageWidth = (samplesPerSecond.toDouble() * duration + (windowSize - 1)).toInt()
                logger.info("imageWidth       = $imageWidth")

                //val amplifier = 4000.0
                val amplifier = m.groups[4]?.value!!.toDouble()
                logger.info("amplifier        = $amplifier")

                inputFile
                    .readSpectrogram()
                    .resize(imageWidth, imageHeight)
                    .generatePhaseSimple()
                    .generatePhaseGriffinLim(20, { it.toWaveform(bits, 1.0) }, { it.toSpectrogram(bits, 1.0) })
                    .also { it.writeTo(inputFile.resolveSibling("dump.png")) }
                    .toWaveform(bits, 1 / amplifier)
                    .toWavByteArray()
                    .also { it.writeTo(inputFile.resolveSibling("dump.wav")) }
                    .toOggAsWav()
                    .writeTo(outputFile)
            }
        }
    }
}
