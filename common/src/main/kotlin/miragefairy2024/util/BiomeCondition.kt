package miragefairy2024.util

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome

sealed class BiomeCondition {
    companion object {
        val CODEC: Codec<BiomeCondition> = Codec.STRING.dispatch(
            "Type",
            { it.type },
            { type ->
                when (type) {
                    "always" -> Always.CODEC
                    "biome" -> BiomeKey.CODEC
                    "biome_tag" -> BiomeTag.CODEC
                    else -> throw IllegalArgumentException("Unknown BiomeCondition type: $type")
                }
            }
        )
    }

    abstract val type: String
    abstract fun test(biome: Holder<Biome>): Boolean
    abstract fun getDisplayName(): Component
    abstract fun getTooltip(): List<Component>
    abstract val sortKey: String

    data object Always : BiomeCondition() {
        override val type = "always"
        val TRANSLATION = Translation({ "gui.${MirageFairy2024.identifier("common_motif_recipe").toLanguageKey()}.always" }, "Always", "常時")
        val CODEC: MapCodec<Always> = MapCodec.unit(Always)
        override fun test(biome: Holder<Biome>) = true
        override fun getDisplayName() = text { translate(TRANSLATION.keyGetter()) }
        override fun getTooltip() = emptyList<Component>()
        override val sortKey = "1_always"
    }

    class BiomeKey(val biome: ResourceKey<Biome>) : BiomeCondition() {
        override val type = "biome"
        companion object {
            val CODEC: MapCodec<BiomeKey> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    ResourceKey.codec(Registries.BIOME).fieldOf("Biome").forGetter { it.biome },
                ).apply(instance, ::BiomeKey)
            }
        }
        override fun test(biome: Holder<Biome>) = biome isIn this.biome
        override fun getDisplayName() = text { translate(this@BiomeKey.biome.location().toLanguageKey("biome")) }
        override fun getTooltip() = emptyList<Component>()
        override val sortKey get() = "2_biome/" + biome.location().pathString
    }

    class BiomeTag(val biomeTag: TagKey<Biome>) : BiomeCondition() {
        override val type = "biome_tag"
        companion object {
            val CODEC: MapCodec<BiomeTag> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    TagKey.codec(Registries.BIOME).fieldOf("BiomeTag").forGetter { it.biomeTag },
                ).apply(instance, ::BiomeTag)
            }
        }
        override fun test(biome: Holder<Biome>) = biome isIn this.biomeTag
        override fun getDisplayName() = text { biomeTag.location().path() }
        override fun getTooltip() = listOf(text { biomeTag.location().string() })
        override val sortKey get() = "3_biome_tag/" + biomeTag.location().pathString
    }
}
