package miragefairy2024.mod.magicplant

import com.mojang.brigadier.arguments.StringArgumentType
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.common.CommandEvents
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.executesThrowable
import miragefairy2024.util.failure
import miragefairy2024.util.invoke
import miragefairy2024.util.string
import miragefairy2024.util.success
import miragefairy2024.util.text
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation

object NegativeTraitBitsRegistry {
    private val map: MutableMap<Trait, Int?> = mutableMapOf()

    fun get(trait: Trait): Int? = map.getOrDefault(trait, 0)

    fun set(trait: Trait, mask: Int?) {
        if (mask == 0) {
            map -= trait
        } else {
            map[trait] = mask
        }
    }
}

private val IDENTIFIER = MirageFairy2024.identifier("negative_trait_bits")
private val UNKNOWN_TRAIT_TRANSLATION = Translation({ IDENTIFIER.toLanguageKey("commands", "unknown_trait") }, "Unknown trait: %s", "不明な特性: %s")
private val INVALID_BITS_FORMAT_TRANSLATION = Translation({ IDENTIFIER.toLanguageKey("commands", "invalid_bits_format") }, "Invalid bits format: %s", "ビットの形式が無効です: %s")
private val SUCCESS_TRANSLATION = Translation({ IDENTIFIER.toLanguageKey("commands", "success") }, "Set negative trait bits for %s = %s", "特性 %s のネガティブビットを %s に設定しました")

context(ModContext)
fun initNegativeTraitBits() {
    UNKNOWN_TRAIT_TRANSLATION.enJa()
    INVALID_BITS_FORMAT_TRANSLATION.enJa()
    SUCCESS_TRANSLATION.enJa()

    CommandEvents.onRegisterClientSubCommand { builder ->
        builder
            .then(
                ClientCommandRegistrationEvent.literal("trait")
                    .then(
                        ClientCommandRegistrationEvent.literal("negative")
                            .then(
                                ClientCommandRegistrationEvent.argument("trait", ResourceLocationArgument.id())
                                    .suggests { _, builder ->
                                        traitRegistry.keySet().forEach {
                                            builder.suggest(it.string)
                                        }
                                        builder.buildFuture()
                                    }
                                    .executesThrowable { context ->
                                        val id = context.getArgument("trait", ResourceLocation::class.java)
                                        val trait = traitRegistry.get(id) ?: failure(text { UNKNOWN_TRAIT_TRANSLATION(id.string) })
                                        NegativeTraitBitsRegistry.set(trait, null)
                                        clientProxy!!.getClientPlayer()!!.sendSystemMessage(text { SUCCESS_TRANSLATION(trait.getName(), "ALL") })
                                        success()
                                    }
                                    .then(
                                        ClientCommandRegistrationEvent.argument("bits", StringArgumentType.word())
                                            .suggests { _, builder ->
                                                listOf("0", "101000").forEach {
                                                    builder.suggest(it)
                                                }
                                                builder.buildFuture()
                                            }
                                            .executesThrowable { context ->
                                                val id = context.getArgument("trait", ResourceLocation::class.java)
                                                val trait = traitRegistry.get(id) ?: failure(text { UNKNOWN_TRAIT_TRANSLATION(id.string) })
                                                val rawBits = context.getArgument("bits", String::class.java)
                                                if (!rawBits.matches("""^[01_]{1,32}$""".toRegex())) failure(text { INVALID_BITS_FORMAT_TRANSLATION("$rawBits") })
                                                val mask = rawBits.replace("_", "").fold(0) { acc, c -> (acc shl 1) or (c - '0') }
                                                NegativeTraitBitsRegistry.set(trait, mask)
                                                clientProxy!!.getClientPlayer()!!.sendSystemMessage(text { SUCCESS_TRANSLATION(trait.getName(), "$rawBits") })
                                                success()
                                            }
                                    )
                            )
                    )
            )
    }
}
