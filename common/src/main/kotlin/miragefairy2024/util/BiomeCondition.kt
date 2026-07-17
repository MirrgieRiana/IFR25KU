package miragefairy2024.util

import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome

sealed class BiomeCondition {
    class BiomeKey(val biome: ResourceKey<Biome>) : BiomeCondition()
    class BiomeTag(val biomeTag: TagKey<Biome>) : BiomeCondition()
}
