package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.plastictree.GIANT_PLASTIC_TREE_CONFIGURED_FEATURE_KEY
import miragefairy2024.mod.tree.contents.plastictree.SMALL_PLASTIC_TREE_CONFIGURED_FEATURE_KEY
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.count
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.getSurfaceNoiseThreshold
import miragefairy2024.util.per
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.tree
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.Direction
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BiomeDefaultFeatures
import net.minecraft.data.worldgen.features.FeatureUtils
import net.minecraft.data.worldgen.features.TreeFeatures
import net.minecraft.data.worldgen.placement.PlacementUtils
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
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.placement.BiomeFilter
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter
import net.minecraft.world.level.levelgen.placement.CaveSurface
import net.minecraft.world.level.levelgen.placement.InSquarePlacement
import net.minecraft.world.level.levelgen.placement.PlacedFeature

val onResinCement get() = listOf(BlockPredicateFilter.forPredicate(BlockPredicate.matchesBlocks(Direction.DOWN.normal, BlockMaterialCard.RESIN_CEMENT.block())))

object OldGrowthAmberForestBiomeCard : BiomeCard(
    "old_growth_amber_forest", EnJa("Old Growth Amber Forest", "琥珀色の原生林"),
    advancementCreator = {
        AdvancementCard(
            identifier = identifier,
            context = AdvancementCard.Sub { FairyForestBiomeCard.advancement!!.await() },
            icon = { TreeBlockCard.PLASTIC_TREE_SAPLING.item().createItemStack() },
            name = EnJa("Land Abloom with Nectar", "蜜の咲き誇る地"), // TODO 蜜から産まれた雑草
            description = EnJa("Travel the overworld and discover the Old Growth Amber Forest", "地上を旅して琥珀色の原生林を探す"),
            criterion = AdvancementCard.visit(key),
            type = AdvancementCardType.TOAST_ONLY,
        )
    },
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, BiomeTags.INCREASED_FIRE_BURNOUT, ConventionalBiomeTags.IS_HOT_OVERWORLD, ConventionalBiomeTags.IS_WET_OVERWORLD,
) {
    private val giantPlasticTreePlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_plastic_tree_old_growth_amber_forest")
    private val smallPlasticTreePlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_plastic_tree_old_growth_amber_forest")
    private val smallPlasticTreeCoarseDirtPlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_plastic_tree_old_growth_amber_forest_coarse_dirt")
    private val smallOakPlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_oak_old_growth_amber_forest")
    private val smallJunglePlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_jungle_tree_old_growth_amber_forest")
    private val megaJunglePlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("mega_jungle_tree_old_growth_amber_forest")
    private val tallGrassPlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("tall_grass_old_growth_amber_forest")
    private val deadBushPlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("dead_bush_old_growth_amber_forest")

    override fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.4F)
            .downfall(0.9F)
            .specialEffects(
                BiomeSpecialEffects.Builder()
                    .waterColor(0xFFB16D)
                    .waterFogColor(0xFFB16D)
                    .fogColor(0xFFD1B2)
                    .skyColor(0x7098FF)
                    .grassColorOverride(0x53B213)
                    .foliageColorOverride(0x368E25)
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

                BiomeDefaultFeatures.addForestFlowers(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultOres(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                // 原生林の証である倒木は、後から生える木がこれを避けるように、木よりも先に配置するのだ～🌱
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, FallenPlasticTreeLogFeatureCard.placedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, giantPlasticTreePlacedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, smallPlasticTreePlacedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, smallPlasticTreeCoarseDirtPlacedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, megaJunglePlacedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, smallOakPlacedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, smallJunglePlacedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ResinCementSapFeatureCard.placedFeatureKey)
                BiomeDefaultFeatures.addDefaultFlowers(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultGrass(lookupBackedBuilder)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, tallGrassPlacedFeatureKey)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, deadBushPlacedFeatureKey)
                BiomeDefaultFeatures.addDefaultExtraVegetation(lookupBackedBuilder)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        super.init()

        // 地形生成
        val onGrassBlock = listOf(BlockPredicateFilter.forPredicate(BlockPredicate.matchesBlocks(Direction.DOWN.normal, Blocks.GRASS_BLOCK)))
        val onCoarseDirt = listOf(BlockPredicateFilter.forPredicate(BlockPredicate.matchesBlocks(Direction.DOWN.normal, Blocks.COARSE_DIRT)))
        Feature.TREE.generator(MirageFairy2024.identifier("giant_plastic_tree_old_growth_amber_forest")) {
            GIANT_PLASTIC_TREE_CONFIGURED_FEATURE_KEY.generator {
                registerPlacedFeature(giantPlasticTreePlacedFeatureKey) { tree(TreeBlockCard.PLASTIC_TREE_SAPLING.block()) + onResinCement }
            }
        }
        Feature.TREE.generator(MirageFairy2024.identifier("small_plastic_tree_old_growth_amber_forest")) {
            SMALL_PLASTIC_TREE_CONFIGURED_FEATURE_KEY.generator {
                registerPlacedFeature(smallPlasticTreePlacedFeatureKey) { count(2) + tree(TreeBlockCard.PLASTIC_TREE_SAPLING.block()) + onResinCement }
                registerPlacedFeature(smallPlasticTreeCoarseDirtPlacedFeatureKey) { tree(TreeBlockCard.PLASTIC_TREE_SAPLING.block()) + onCoarseDirt }
            }
        }
        Feature.TREE.generator(MirageFairy2024.identifier("small_oak_old_growth_amber_forest")) {
            TreeFeatures.OAK.generator {
                registerPlacedFeature(smallOakPlacedFeatureKey) { per(2) + tree(Blocks.OAK_SAPLING) }
            }
        }
        Feature.TREE.generator(MirageFairy2024.identifier("small_jungle_tree_old_growth_amber_forest")) {
            TreeFeatures.JUNGLE_TREE.generator {
                registerPlacedFeature(smallJunglePlacedFeatureKey) { per(2) + tree(Blocks.JUNGLE_SAPLING) + onGrassBlock }
            }
        }
        Feature.TREE.generator(MirageFairy2024.identifier("mega_jungle_tree_old_growth_amber_forest")) {
            TreeFeatures.MEGA_JUNGLE_TREE.generator {
                registerPlacedFeature(megaJunglePlacedFeatureKey) { per(8) + tree(Blocks.JUNGLE_SAPLING) + onGrassBlock }
            }
        }
        Feature.RANDOM_PATCH.generator(MirageFairy2024.identifier("tall_grass_old_growth_amber_forest")) {
            registerConfiguredFeature { FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.TALL_GRASS)), listOf(Blocks.GRASS_BLOCK)) }.generator {
                registerPlacedFeature(tallGrassPlacedFeatureKey) { count(2) + flower(square, surface) }
            }
        }
        Feature.RANDOM_PATCH.generator(MirageFairy2024.identifier("dead_bush_old_growth_amber_forest")) {
            registerConfiguredFeature { FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.DEAD_BUSH)), listOf(Blocks.COARSE_DIRT), 4) }.generator {
                registerPlacedFeature(deadBushPlacedFeatureKey) { count(4) + listOf(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome()) }
            }
        }

        registerOverworldSurfaceRules {
            SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(
                    SurfaceRules.waterBlockCheck(-1, 0),
                    SurfaceRules.ifTrue(
                        SurfaceRules.isBiome(key),
                        SurfaceRules.sequence(
                            SurfaceRules.ifTrue(
                                SurfaceRules.stoneDepthCheck(0, true, 20, CaveSurface.FLOOR),
                                SurfaceRules.ifTrue(
                                    SurfaceRules.noiseCondition(Noises.SURFACE_SECONDARY, getSurfaceNoiseThreshold(Noises.SURFACE_SECONDARY, 0.50), Double.MAX_VALUE),
                                    SurfaceRules.state(BlockMaterialCard.RESIN_CEMENT.block().defaultBlockState())
                                ),
                            ),
                            SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.sequence(
                                    SurfaceRules.ifTrue(
                                        SurfaceRules.noiseCondition(Noises.SURFACE, getSurfaceNoiseThreshold(Noises.SURFACE, 0.30), Double.MAX_VALUE),
                                        SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState())
                                    ),
                                    SurfaceRules.ifTrue(
                                        SurfaceRules.noiseCondition(Noises.SURFACE, getSurfaceNoiseThreshold(Noises.SURFACE, 0.40), Double.MAX_VALUE),
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
