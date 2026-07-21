package miragefairy2024.mod.biome

import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import net.minecraft.core.HolderGetter
import net.minecraft.data.worldgen.BiomeDefaultFeatures
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.tags.BiomeTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object OldGrowthAmberForestBiomeCard : BiomeCard(
    "old_growth_amber_forest", EnJa("Old Growth Amber Forest", "琥珀色の原生林"),
    advancementCreator = null,
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_TAIGA,
) {
    override fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.25F)
            .downfall(0.8F)
            .specialEffects(
                BiomeSpecialEffects.Builder()
                    .waterColor(0x5B2A8A)
                    .waterFogColor(0x37175A)
                    .fogColor(0xE0C088)
                    .skyColor(0x93A6E6)
                    .grassColorOverride(0xE0A628)
                    .foliageColorOverride(0xC98F1C)
                    .build()
            )
            .mobSpawnSettings(MobSpawnSettings.Builder().also { spawnSettings ->

                BiomeDefaultFeatures.farmAnimals(spawnSettings)
                spawnSettings.addSpawn(MobCategory.CREATURE, MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))
                spawnSettings.addSpawn(MobCategory.CREATURE, MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
                spawnSettings.addSpawn(MobCategory.CREATURE, MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4))
                BiomeDefaultFeatures.commonSpawns(spawnSettings)

            }.build())
            .generationSettings(BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup).also { lookupBackedBuilder ->

                // BasicFeatures
                BiomeDefaultFeatures.addDefaultCarversAndLakes(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultCrystalFormations(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultMonsterRoom(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultUndergroundVariety(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSprings(lookupBackedBuilder)
                BiomeDefaultFeatures.addSurfaceFreezing(lookupBackedBuilder)

                BiomeDefaultFeatures.addMossyStoneBlock(lookupBackedBuilder)
                BiomeDefaultFeatures.addFerns(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultOres(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                // TREES_OLD_GROWTH_SPRUCE_TAIGA を TREES_TAIGA に差し替え（トウヒの巨木の撤去）
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_TAIGA)
                BiomeDefaultFeatures.addDefaultFlowers(lookupBackedBuilder)
                BiomeDefaultFeatures.addGiantTaigaVegetation(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultMushrooms(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultExtraVegetation(lookupBackedBuilder)
                BiomeDefaultFeatures.addCommonBerryBushes(lookupBackedBuilder)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        super.init()

        registerOverworldBiomeOverride(Biomes.JUNGLE)
        registerOverworldBiomeOverride(Biomes.SPARSE_JUNGLE)
        registerOverworldBiomeOverride(Biomes.BAMBOO_JUNGLE)

    }
}
