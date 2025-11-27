package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.with
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object BiomeCards {
    val entries = listOf(
        FairyForestBiomeCard,
        DeepFairyForestBiomeCard,
        RetrospectiveCityBiomeCard,
    )
}

abstract class BiomeCard(
    path: String,
    name: EnJa,
    vararg val tags: TagKey<Biome>,
) {
    abstract fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome

    val identifier = MirageFairy2024.identifier(path)
    val key = Registries.BIOME with identifier
    val translation = Translation({ identifier.toLanguageKey("biome") }, name)

    context(ModContext)
    open fun init() = Unit
}
