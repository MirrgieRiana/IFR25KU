package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.clientProxy
import net.minecraft.network.chat.Component

fun interface KeyMapping {
    fun matches(keyCode: Int, scanCode: Int): Boolean
}

/** @param category この文字列は識別と翻訳キーの両方に使われます。 */
class KeyMappingCard(
    val nameTranslationKey: String,
    private val defaultKeyCode: Int,
    private val category: String,
    private val listener: () -> Unit,
) {
    lateinit var keyMapping: KeyMapping

    context(ModContext)
    fun init() = ModEvents.onClientInit {
        keyMapping = clientProxy!!.registerKeyMapping(nameTranslationKey, defaultKeyCode, category, listener)
    }
}

context(TextScope)
operator fun KeyMappingCard.invoke(): Component = Component.keybind(this@invoke.nameTranslationKey)
