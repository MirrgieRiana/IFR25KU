package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getSurfaceNoiseThreshold
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
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
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object OldGrowthAmberForestBiomeCard : BiomeCard(
    "old_growth_amber_forest", EnJa("Old Growth Amber Forest", "琥珀色の原生林"),
    advancementCreator = {
        AdvancementCard(
            identifier = identifier,
            context = AdvancementCard.Sub { FairyForestBiomeCard.advancement!!.await() },
            icon = { MaterialCard.FAIRY_PLASTIC.item().createItemStack() }, // TODO →プラノキの苗木
            name = EnJa("Land Abloom with Nectar", "蜜の咲き誇る地"), // TODO 蜜から産まれた雑草
            description = EnJa("Travel the overworld and discover the Old Growth Amber Forest", "地上を旅して琥珀色の原生林を探す"),
            criterion = AdvancementCard.visit(key),
            type = AdvancementCardType.TOAST_ONLY,
        )
    },
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, BiomeTags.INCREASED_FIRE_BURNOUT, ConventionalBiomeTags.IS_WET_OVERWORLD,
) {
    override fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.4F)
            .downfall(0.9F)
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
                BiomeDefaultFeatures.addForestFlowers(lookupBackedBuilder)
                BiomeDefaultFeatures.addFerns(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultOres(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, ElevatedSpawnerFeatureCard.placedFeatureKey)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_TAIGA)
                BiomeDefaultFeatures.addDefaultFlowers(lookupBackedBuilder)
                BiomeDefaultFeatures.addTaigaGrass(lookupBackedBuilder)
                BiomeDefaultFeatures.addGiantTaigaVegetation(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultExtraVegetation(lookupBackedBuilder)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        super.init()

        registerOverworldSurfaceRules(MirageFairy2024.MOD_ID) {
            SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(
                    SurfaceRules.waterBlockCheck(-1, 0),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(key),
                        SurfaceRules.sequence(
                            // 数千万年分の樹脂が堆積した土地なので、地表だけでなく、メサのテラコッタの層と同じくらい深くまで置き換えるのだぁ🌱
                            SurfaceRules.ifTrue(
                                SurfaceRules.VERY_DEEP_UNDER_FLOOR,
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SURFACE_SECONDARY, getSurfaceNoiseThreshold(Noises.SURFACE_SECONDARY, 0.61), Double.MAX_VALUE),
                                    SurfaceRules.state(BlockMaterialCard.RESIN_CEMENTED_DIRT.block().defaultBlockState())
                                ),
                            ),
                            SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                        SurfaceRules.noiseCondition(Noises.SURFACE, getSurfaceNoiseThreshold(Noises.SURFACE, 0.25), Double.MAX_VALUE),
                                        SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState())
                                    ),
                                    SurfaceRules.ifTrue(
                                        SurfaceRules.noiseCondition(Noises.SURFACE, getSurfaceNoiseThreshold(Noises.SURFACE, 0.64), Double.MAX_VALUE),
                                        SurfaceRules.state(Blocks.PODZOL.defaultBlockState())
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }

        registerOverworldBiomeOverride(Biomes.JUNGLE)
        registerOverworldBiomeOverride(Biomes.SPARSE_JUNGLE)
        registerOverworldBiomeOverride(Biomes.BAMBOO_JUNGLE)

    }
}
