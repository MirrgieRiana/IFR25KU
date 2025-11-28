package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.with
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.placement.PlacedFeature

abstract class BiomeCard(
    path: String,
    name: EnJa,
    advancementCreator: (BiomeCard.() -> AdvancementCard)? = null,
    vararg val tags: TagKey<Biome>,
) {
    abstract fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome

    val identifier = MirageFairy2024.identifier(path)
    val key = Registries.BIOME with identifier
    val translation = Translation({ identifier.toLanguageKey("biome") }, name)
    val advancement = advancementCreator?.invoke(this)

    context(ModContext)
    open fun init() {

        registerDynamicGeneration(key) {
            createBiome(lookup(Registries.PLACED_FEATURE), lookup(Registries.CONFIGURED_CARVER))
        }

        translation.enJa()

        tags.forEach { tag ->
            tag.generator.registerChild(identifier)
        }

        advancement?.init()

    }
}

context(ModContext, BiomeCard)
fun registerOverworldBiomeOverride(biome: ResourceKey<Biome>) {
    OVERWORLD_BIOME_OVERRIDES[biome] = key
}
