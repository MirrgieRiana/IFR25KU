package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome

val COMMON_MOTIF_RECIPES = mutableListOf<CommonMotifRecipe>()

sealed class CommonMotifRecipe(val motif: Motif) {
    companion object {
        val CODEC: Codec<CommonMotifRecipe> = Codec.STRING.dispatch(
            "Type",
            { recipe: CommonMotifRecipe ->
                when (recipe) {
                    is AlwaysCommonMotifRecipe -> "always"
                    is BiomeCommonMotifRecipe -> "biome"
                    is BiomeTagCommonMotifRecipe -> "biome_tag"
                }
            },
            { type: String ->
                when (type) {
                    "always" -> AlwaysCommonMotifRecipe.CODEC
                    "biome" -> BiomeCommonMotifRecipe.CODEC
                    "biome_tag" -> BiomeTagCommonMotifRecipe.CODEC
                    else -> throw IllegalArgumentException("Unknown CommonMotifRecipe type: $type")
                }
            }
        )
    }
}

class AlwaysCommonMotifRecipe(motif: Motif) : CommonMotifRecipe(motif) {
    companion object {
        val CODEC: MapCodec<AlwaysCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
            ).apply(instance, ::AlwaysCommonMotifRecipe)
        }
    }
}

class BiomeCommonMotifRecipe(motif: Motif, val biome: ResourceKey<Biome>) : CommonMotifRecipe(motif) {
    companion object {
        val CODEC: MapCodec<BiomeCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                ResourceKey.codec(Registries.BIOME).fieldOf("Biome").forGetter { it.biome }
            ).apply(instance, ::BiomeCommonMotifRecipe)
        }
    }
}

class BiomeTagCommonMotifRecipe(motif: Motif, val biomeTag: TagKey<Biome>) : CommonMotifRecipe(motif) {
    companion object {
        val CODEC: MapCodec<BiomeTagCommonMotifRecipe> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                TagKey.codec(Registries.BIOME).fieldOf("BiomeTag").forGetter { it.biomeTag }
            ).apply(instance, ::BiomeTagCommonMotifRecipe)
        }
    }
}
