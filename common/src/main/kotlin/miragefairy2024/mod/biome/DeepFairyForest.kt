package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import net.minecraft.core.HolderGetter
import net.minecraft.data.worldgen.BiomeDefaultFeatures
import net.minecraft.data.worldgen.placement.AquaticPlacements
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.tags.BiomeTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object DeepFairyForestBiomeCard : BiomeCard(
    "deep_fairy_forest", EnJa("Deep Fairy Forest", "妖精の樹海"),
    advancementCreator = {
        AdvancementCard(
            identifier = identifier,
            context = AdvancementCard.Sub { FairyForestBiomeCard.advancement!!.await() },
            icon = { HaimeviskaBlockCard.SAPLING.item().createItemStack() },
            name = EnJa("The Forest of Memories", "記憶の森"),
            description = EnJa("Travel the overworld and discover the Deep Fairy Forest", "地上を旅して妖精の樹海を探す"),
            criterion = AdvancementCard.visit(key),
            type = AdvancementCardType.TOAST_ONLY,
        )
    },
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, FAIRY_BIOME_TAG,
) {
    override fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.4F)
            .downfall(0.6F)
            .specialEffects(
                BiomeSpecialEffects.Builder()
                    .waterColor(0xD1FCFF)
                    .waterFogColor(0xD1FCFF)
                    .fogColor(0xB7C9FF)
                    .skyColor(0x87A9FF)
                    .grassColorOverride(0x31EDCD)
                    .foliageColorOverride(0xB2A8FF)
                    .build()
            )
            .mobSpawnSettings(MobSpawnSettings.Builder().also { spawnSettings ->

                BiomeDefaultFeatures.commonSpawns(spawnSettings)
                spawnSettings.addSpawn(MobCategory.MONSTER, MobSpawnSettings.SpawnerData(EntityType.WITCH, 100, 1, 4))

                spawnSettings.addSpawn(MobCategory.CREATURE, MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))

                // River Mobs
                spawnSettings.addSpawn(MobCategory.WATER_CREATURE, MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4))
                spawnSettings.addSpawn(MobCategory.WATER_AMBIENT, MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5))

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
                BiomeDefaultFeatures.addForestFlowers(lookupBackedBuilder)
                BiomeDefaultFeatures.addFerns(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultOres(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY)

                BiomeDefaultFeatures.addTaigaGrass(lookupBackedBuilder)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH)
                BiomeDefaultFeatures.addDefaultMushrooms(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        super.init()
        registerOverworldBiomeOverride(Biomes.TAIGA)
        registerOverworldBiomeOverride(Biomes.OLD_GROWTH_PINE_TAIGA)
        registerOverworldBiomeOverride(Biomes.OLD_GROWTH_SPRUCE_TAIGA)
        registerOverworldBiomeOverride(Biomes.SNOWY_TAIGA)
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
                                    SurfaceRules.noiseCondition(Noises.SURFACE, 1.75 / 8.25, Double.MAX_VALUE),
                                    SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState())
                                ),
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SURFACE, -0.95 / 8.25, Double.MAX_VALUE),
                                    SurfaceRules.state(Blocks.PODZOL.defaultBlockState())
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
