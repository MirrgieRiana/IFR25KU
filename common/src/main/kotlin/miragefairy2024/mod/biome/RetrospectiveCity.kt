package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.get
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BiomeDefaultFeatures
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.tags.BiomeTags
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import terrablender.api.SurfaceRuleManager

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

    private val MIRAGIDIAN_LAMP_FEATURE = MiragidianLampFeature(NoneFeatureConfiguration.CODEC)
    private val MIRAGIDIAN_LAMP_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("miragidian_lamp")
    private val MIRAGIDIAN_LAMP_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("miragidian_lamp")

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
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MIRAGIDIAN_LAMP_PLACED_FEATURE_KEY)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        ModEvents.onTerraBlenderInitialized {
            val rule = SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.ifTrue(
                        SurfaceRules.waterBlockCheck(-1, 0),
                        SurfaceRules.ifTrue(
                            SurfaceRules.isBiome(key),
                            SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SURFACE, 3.0 / 8.25, Double.MAX_VALUE),
                                    SurfaceRules.state(BlockMaterialCard.AURA_RESISTANT_CERAMIC_TILES.block().defaultBlockState())
                                ),
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SURFACE, 2.8 / 8.25, Double.MAX_VALUE),
                                    SurfaceRules.state(BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC.block().defaultBlockState())
                                ),
                            ),
                        ),
                    ),
                ),
            )
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MirageFairy2024.MOD_ID, rule)
        }

        Registration(BuiltInRegistries.FEATURE, MirageFairy2024.identifier("miragidian_lamp")) { MIRAGIDIAN_LAMP_FEATURE }.register()
        registerDynamicGeneration(MIRAGIDIAN_LAMP_CONFIGURED_FEATURE_KEY) {
            MIRAGIDIAN_LAMP_FEATURE with NoneFeatureConfiguration.INSTANCE
        }
        registerDynamicGeneration(MIRAGIDIAN_LAMP_PLACED_FEATURE_KEY) {
            val placementModifiers = placementModifiers { per(8) + flower(square, surface) }
            Registries.CONFIGURED_FEATURE[MIRAGIDIAN_LAMP_CONFIGURED_FEATURE_KEY] with placementModifiers
        }
    }
}
