package miragefairy2024.util

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.mod.guiAlwaysTranslation
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
            {
                when (it) {
                    is Always -> "always"
                    is BiomeKey -> "biome"
                    is BiomeTag -> "tag"
                }
            },
            { type ->
                when (type) {
                    "always" -> Always.CODEC
                    "biome" -> BiomeKey.CODEC
                    "tag" -> BiomeTag.CODEC
                    else -> throw IllegalArgumentException("Unknown BiomeCondition type: $type")
                }
            }
        )
    }

    abstract fun test(biome: Holder<Biome>): Boolean
    abstract fun getDisplayName(): Component

    data object Always : BiomeCondition() {
        val CODEC: MapCodec<Always> = MapCodec.unit(Always)
        override fun test(biome: Holder<Biome>) = true
        override fun getDisplayName() = text { translate(guiAlwaysTranslation.keyGetter()) }
    }

    class BiomeKey(val biomeKey: ResourceKey<Biome>) : BiomeCondition() {
        companion object {
            val CODEC: MapCodec<BiomeKey> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    ResourceKey.codec(Registries.BIOME).fieldOf("Biome").forGetter { it.biomeKey },
                ).apply(instance, ::BiomeKey)
            }
        }
        override fun test(biome: Holder<Biome>) = biome isIn biomeKey
        override fun getDisplayName() = text { translate(biomeKey.location().toLanguageKey("biome")) }
    }

    class BiomeTag(val biomeTag: TagKey<Biome>) : BiomeCondition() {
        companion object {
            val CODEC: MapCodec<BiomeTag> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    TagKey.codec(Registries.BIOME).fieldOf("BiomeTag").forGetter { it.biomeTag },
                ).apply(instance, ::BiomeTag)
            }
        }
        override fun test(biome: Holder<Biome>) = biome isIn biomeTag
        override fun getDisplayName() = text { biomeTag.location().path() }
    }
}
