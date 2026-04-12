package tools

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun normalizeJson(element: JsonElement): JsonElement = when {
    element.isJsonObject -> {
        val sorted = JsonObject()
        element.asJsonObject.entrySet().sortedBy { it.key }.forEach { (key, value) ->
            sorted.add(key, normalizeJson(value))
        }
        sorted
    }

    element.isJsonArray -> {
        val array = JsonArray()
        element.asJsonArray.forEach { array.add(normalizeJson(it)) }
        array
    }

    else -> element
}
