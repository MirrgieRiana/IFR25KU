package miragefairy2024.client.util

import miragefairy2024.ModContext
import miragefairy2024.mixin.client.api.inputEventsHandlers
import miragefairy2024.util.TextScope
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper as KeyMappingHelper // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.network.chat.Component

/** @param category この文字列は識別と翻訳キーの両方に使われます。 */
class KeyMappingCard(
    val nameTranslationKey: String,
    private val defaultKeyCode: Int,
    private val category: String,
    private val listener: () -> Unit,
) {
    lateinit var keyMapping: KeyMapping

    context(_: ModContext)
    fun init() {
        keyMapping = KeyMapping(nameTranslationKey, defaultKeyCode, category)
        inputEventsHandlers += {
            while (keyMapping.consumeClick()) {
                listener()
            }
        }
        KeyMappingHelper.registerKeyBinding(keyMapping)
    }
}

context(_: TextScope)
operator fun KeyMappingCard.invoke(): Component = Component.keybind(this@invoke.nameTranslationKey)
