package miragefairy2024.wave

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

/** 注意：wav→oggの変換ではサンプル数が増える場合がある。 */
fun ByteArray.toOggAsWav(): ByteArray {
    val processBuilder = ProcessBuilder("bash", "-c", "ffmpeg -i - -f ogg -")
    val process = processBuilder.start()
    return runBlocking {
        launch(Dispatchers.IO) {
            process.outputStream.use { output ->
                output.write(this@toOggAsWav)
            }
        }
        val err = async(Dispatchers.IO) {
            process.errorStream.use { input ->
                input.readBytes()
            }
        }
        val result = async(Dispatchers.IO) {
            process.inputStream.use { input ->
                input.readBytes()
            }
        }
        val returnCode = process.waitFor()
        if (returnCode != 0) throw IOException("Process exit: $returnCode\n${err.await().toString(Charsets.UTF_8).replace("""\n+\Z""".toRegex(), "")}")
        result.await()
    }
}

fun ByteArray.toWavAsOgg(): ByteArray {
    val processBuilder = ProcessBuilder("bash", "-c", "ffmpeg -i - -f wav -")
    val process = processBuilder.start()
    return runBlocking {
        launch(Dispatchers.IO) {
            process.outputStream.use { output ->
                output.write(this@toWavAsOgg)
            }
        }
        val err = async(Dispatchers.IO) {
            process.errorStream.use { input ->
                input.readBytes()
            }
        }
        val result = async(Dispatchers.IO) {
            process.inputStream.use { input ->
                input.readBytes()
            }
        }
        val returnCode = process.waitFor()
        if (returnCode != 0) throw IOException("Process exit: $returnCode\n${err.await().toString(Charsets.UTF_8).replace("""\n+\Z""".toRegex(), "")}")
        result.await()
    }
}
