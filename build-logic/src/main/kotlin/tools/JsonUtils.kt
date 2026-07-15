package tools

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.File
import java.security.MessageDigest

fun JsonElement.normalizeJson(): JsonElement = when {
    isJsonObject -> {
        val sorted = JsonObject()
        asJsonObject.entrySet().sortedBy { it.key }.forEach { (key, value) ->
            sorted.add(key, value.normalizeJson())
        }
        sorted
    }

    isJsonArray -> {
        val array = JsonArray()
        asJsonArray.forEach { array.add(it.normalizeJson()) }
        array
    }

    else -> this
}

fun File.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(readBytes())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
