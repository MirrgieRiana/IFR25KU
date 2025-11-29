package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.HolderGetter
import net.minecraft.data.worldgen.BiomeDefaultFeatures
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.tags.BiomeTags
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object RetrospectiveCityBiomeCard : BiomeCard(
    "retrospective_city", EnJa("Retrospective City", "過去を見つめる都市"),
    advancementCreator = {
        AdvancementCard(
            identifier = identifier,
            context = AdvancementCard.Sub { FairyForestBiomeCard.advancement!!.await() },
            icon = { BlockMaterialCard.AURA_RESISTANT_CERAMIC_STAIRS.item().createItemStack() },
            name = EnJa("Graveyard of Civilization", "文明の墓場"),
            description = EnJa("Travel the overworld and discover the Retrospective City", "地上を旅して過去を見つめる都市を探す"),
            criterion = AdvancementCard.visit(key),
            type = AdvancementCardType.TOAST_ONLY,
        )
    },
    BiomeTags.IS_OVERWORLD, ConventionalBiomeTags.IS_COLD_OVERWORLD, ConventionalBiomeTags.IS_DRY_OVERWORLD,
) {
    override fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.4F)
            .downfall(0.4F)
            .specialEffects(
                BiomeSpecialEffects.Builder()
                    .waterColor(0x5D7C8C)
                    .waterFogColor(0x1E2123)
                    .fogColor(0xC0D8FF)
                    .skyColor(0x78A7FF)
                    .grassColorOverride(0xCCA672)
                    .foliageColorOverride(0x9E6045)
                    .build()
            )
            .mobSpawnSettings(MobSpawnSettings.Builder().also { spawnSettings ->
                BiomeDefaultFeatures.plainsSpawns(spawnSettings)
                spawnSettings.addSpawn(MobCategory.MONSTER, MobSpawnSettings.SpawnerData(ChaosCubeCard.entityType(), 2, 1, 4))
            }.build())
            .generationSettings(BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup).also { lookupBackedBuilder ->

                // BasicFeatures
                BiomeDefaultFeatures.addDefaultCarversAndLakes(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultCrystalFormations(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultMonsterRoom(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultUndergroundVariety(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSprings(lookupBackedBuilder)
                BiomeDefaultFeatures.addSurfaceFreezing(lookupBackedBuilder)

                BiomeDefaultFeatures.addPlainGrass(lookupBackedBuilder)
                BiomeDefaultFeatures.addSavannaGrass(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultOres(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                BiomeDefaultFeatures.addPlainVegetation(lookupBackedBuilder)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH)
                BiomeDefaultFeatures.addDefaultMushrooms(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultExtraVegetation(lookupBackedBuilder)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiragidianLampFeatureCard.placedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, RetrospectiveCitySmallRuinFeatureCard.placedFeatureKey)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        super.init()

        registerOverworldSurfaceRules(MirageFairy2024.MOD_ID) {
            SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.ifTrue(
                        SurfaceRules.waterBlockCheck(-1, 0),
                        SurfaceRules.ifTrue(
                            SurfaceRules.isBiome(key),
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SURFACE, 3.5 / 8.25, Double.MAX_VALUE),
                                    SurfaceRules.state(BlockMaterialCard.AURA_RESISTANT_CERAMIC_TILES.block().defaultBlockState())
                                ),
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SURFACE, 3.3 / 8.25, Double.MAX_VALUE),
                                    SurfaceRules.state(BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC.block().defaultBlockState())
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        registerOverworldBiomeOverride(Biomes.SAVANNA)
        registerOverworldBiomeOverride(Biomes.SAVANNA_PLATEAU)
        registerOverworldBiomeOverride(Biomes.WINDSWEPT_SAVANNA)
        registerOverworldBiomeOverride(Biomes.BADLANDS)
        registerOverworldBiomeOverride(Biomes.ERODED_BADLANDS)
        registerOverworldBiomeOverride(Biomes.WOODED_BADLANDS)

    }
}
