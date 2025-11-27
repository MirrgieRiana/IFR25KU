package miragefairy2024.mod.biome

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.haimeviska.HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.haimeviska.HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.magicplant.contents.magicplants.PhantomFlowerCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.contents.MiragidianLampBlock
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.toBiomeTag
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.HolderGetter
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BiomeDefaultFeatures
import net.minecraft.data.worldgen.placement.AquaticPlacements
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BiomeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeGenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import terrablender.api.SurfaceRuleManager
import java.util.function.Consumer

val FAIRY_BIOME_TAG = MirageFairy2024.identifier("fairy").toBiomeTag()

object BiomeCards {
    val entries = listOf(
        FairyForestBiomeCard,
        DeepFairyForestBiomeCard,
        RetrospectiveCityBiomeCard,
    )
}

abstract class BiomeCard(
    path: String,
    en: String,
    ja: String,
    vararg val tags: TagKey<Biome>,
) {
    abstract fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome

    context(ModContext)
    open fun init() = Unit

    val identifier = MirageFairy2024.identifier(path)
    val registryKey = Registries.BIOME with identifier
    val translation = Translation({ identifier.toLanguageKey("biome") }, en, ja)
}

context(ModContext)
fun initBiomeModule() {
    FAIRY_BIOME_TAG.enJa(EnJa("Fairy", "妖精"))
    BiomeCards.entries.forEach { card ->

        // バイオームの生成
        registerDynamicGeneration(card.registryKey) {
            card.createBiome(lookup(Registries.PLACED_FEATURE), lookup(Registries.CONFIGURED_CARVER))
        }

        // このバイオームをタグに登録
        card.tags.forEach { tag ->
            tag.generator.registerChild(card.identifier)
        }

        // 翻訳生成
        card.translation.enJa()

        card.init()
    }
    ModEvents.onTerraBlenderInitialized {

        // 地上世界用の共通RegionをTerraBlenderに登録
        Regions.register(object : Region(MirageFairy2024.identifier("overworld"), RegionType.OVERWORLD, 1) {
            override fun addBiomes(registry: Registry<Biome>, mapper: Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>) {
                addModifiedVanillaOverworldBiomes(mapper) {
                    it.replaceBiome(Biomes.FOREST, FairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.WINDSWEPT_FOREST, FairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.FLOWER_FOREST, FairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.BIRCH_FOREST, FairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.OLD_GROWTH_BIRCH_FOREST, FairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.DARK_FOREST, FairyForestBiomeCard.registryKey)

                    it.replaceBiome(Biomes.TAIGA, DeepFairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.OLD_GROWTH_PINE_TAIGA, DeepFairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.OLD_GROWTH_SPRUCE_TAIGA, DeepFairyForestBiomeCard.registryKey)
                    it.replaceBiome(Biomes.SNOWY_TAIGA, DeepFairyForestBiomeCard.registryKey)

                    it.replaceBiome(Biomes.SAVANNA, RetrospectiveCityBiomeCard.registryKey)
                    it.replaceBiome(Biomes.SAVANNA_PLATEAU, RetrospectiveCityBiomeCard.registryKey)
                    it.replaceBiome(Biomes.WINDSWEPT_SAVANNA, RetrospectiveCityBiomeCard.registryKey)
                    it.replaceBiome(Biomes.BADLANDS, RetrospectiveCityBiomeCard.registryKey)
                    it.replaceBiome(Biomes.ERODED_BADLANDS, RetrospectiveCityBiomeCard.registryKey)
                    it.replaceBiome(Biomes.WOODED_BADLANDS, RetrospectiveCityBiomeCard.registryKey)
                }
            }
        })

    }
}


object FairyForestBiomeCard : BiomeCard(
    "fairy_forest", "Fairy Forest", "妖精の森",
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, ConventionalBiomeTags.IS_FLORAL, FAIRY_BIOME_TAG,
) {
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { PhantomFlowerCard.item().createItemStack() }, // TODO もっと相応しいアイコンに変える
        name = EnJa("Fairylands", "世界のそこかしこにあるおとぎの国"),
        description = EnJa("Travel the overworld and discover the Fairy Forest", "地上を旅して妖精の森を探す"),
        criterion = AdvancementCard.visit(registryKey),
        type = AdvancementCardType.TOAST_ONLY,
    )

    override fun createBiome(placedFeatureLookup: HolderGetter<PlacedFeature>, configuredCarverLookup: HolderGetter<ConfiguredWorldCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.4F)
            .downfall(0.6F)
            .specialEffects(
                BiomeSpecialEffects.Builder()
                    .waterColor(0xF3D9FF)
                    .waterFogColor(0xF3D9FF)
                    .fogColor(0xD3C9FF)
                    .skyColor(0xA0A9FF)
                    .grassColorOverride(0x82FFBF)
                    .foliageColorOverride(0xCDAFFF)
                    .build()
            )
            .mobSpawnSettings(MobSpawnSettings.Builder().also { spawnSettings ->

                BiomeDefaultFeatures.caveSpawns(spawnSettings)

                spawnSettings.addSpawn(MobCategory.CREATURE, MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
                spawnSettings.addSpawn(MobCategory.CREATURE, MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4))

                spawnSettings.addSpawn(MobCategory.MONSTER, MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4))

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

                BiomeDefaultFeatures.addFerns(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultOres(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY)
                BiomeDefaultFeatures.addOtherBirchTrees(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultFlowers(lookupBackedBuilder)
                BiomeDefaultFeatures.addMeadowVegetation(lookupBackedBuilder)
                BiomeDefaultFeatures.addTaigaGrass(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultMushrooms(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultExtraVegetation(lookupBackedBuilder)
                BiomeDefaultFeatures.addCommonBerryBushes(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        advancement.init()
    }
}

object DeepFairyForestBiomeCard : BiomeCard(
    "deep_fairy_forest", "Deep Fairy Forest", "妖精の樹海",
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, FAIRY_BIOME_TAG,
) {
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { FairyForestBiomeCard.advancement.await() },
        icon = { HaimeviskaBlockCard.SAPLING.item().createItemStack() },
        name = EnJa("The Forest of Memories", "記憶の森"),
        description = EnJa("Travel the overworld and discover the Deep Fairy Forest", "地上を旅して妖精の樹海を探す"),
        criterion = AdvancementCard.visit(registryKey),
        type = AdvancementCardType.TOAST_ONLY,
    )

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
        ModEvents.onTerraBlenderInitialized {
            val rule = SurfaceRules.ifTrue(
                SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(
                    SurfaceRules.ON_FLOOR,
                    SurfaceRules.ifTrue(
                        SurfaceRules.waterBlockCheck(-1, 0),
                        SurfaceRules.ifTrue(
                            SurfaceRules.isBiome(registryKey),
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
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MirageFairy2024.MOD_ID, rule)
        }
        advancement.init()
    }
}

object RetrospectiveCityBiomeCard : BiomeCard(
    "retrospective_city", "Retrospective City", "過去を見つめる都市",
    BiomeTags.IS_OVERWORLD, ConventionalBiomeTags.IS_COLD_OVERWORLD, ConventionalBiomeTags.IS_DRY_OVERWORLD,
) {
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { FairyForestBiomeCard.advancement.await() },
        icon = { BlockMaterialCard.AURA_RESISTANT_CERAMIC_TILES.item().createItemStack() },
        name = EnJa("Graveyard of Civilization", "文明の墓場"),
        description = EnJa("Travel the overworld and discover the Retrospective City", "地上を旅して過去を見つめる都市を探す"),
        criterion = AdvancementCard.visit(registryKey),
        type = AdvancementCardType.TOAST_ONLY,
    )

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
                            SurfaceRules.isBiome(registryKey),
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
        advancement.init()

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

class MiragidianLampFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val blockPos = context.origin()
        val height = context.random().nextIntBetweenInclusive(3, 8)

        repeat(height) { i ->
            val targetBlockPos = blockPos.above(i)
            if (targetBlockPos.y >= level.maxBuildHeight) return false
            if (!level.isEmptyBlock(targetBlockPos)) return false
        }
        if (!level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) return false

        repeat(height) { i ->
            val targetBlockPos = blockPos.above(i)
            val part = when (i) {
                0 -> MiragidianLampBlock.Part.FOOT
                height - 1 -> MiragidianLampBlock.Part.HEAD
                else -> MiragidianLampBlock.Part.POLE
            }
            val blockState = BlockMaterialCard.MIRAGIDIAN_LAMP.block().defaultBlockState().setValue(MiragidianLampBlock.PART, part)
            level.setBlock(targetBlockPos, blockState, 2)
        }

        return true
    }
}
