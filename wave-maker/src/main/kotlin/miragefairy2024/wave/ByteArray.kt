package miragefairy2024.wave

import java.io.File

fun ByteArray.writeTo(file: File) = file.writeBytes(this)
