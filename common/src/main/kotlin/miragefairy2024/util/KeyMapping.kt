package miragefairy2024.util

fun interface KeyMapping {
    fun matches(keyCode: Int, scanCode: Int): Boolean
}
