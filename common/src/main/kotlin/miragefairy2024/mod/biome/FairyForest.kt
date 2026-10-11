package miragefairy2024.mod.biome

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.rootAdvancement
import miragefairy2024.mod.magicplant.contents.magicplants.PhantomFlowerCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.tree.contents.haimeviska.GIANT_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.tree.contents.haimeviska.SMALL_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.count
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BiomeDefaultFeatures
import net.minecraft.data.worldgen.placement.AquaticPlacements
import net.minecraft.data.worldgen.placement.PlacementUtils
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
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object FairyForestBiomeCard : BiomeCard(
    "fairy_forest", EnJa("Fairy Forest", "妖精の森"),
    advancementCreator = {
        AdvancementCard(
            identifier = identifier,
            context = AdvancementCard.Sub { rootAdvancement.await() },
            icon = { PhantomFlowerCard.item().createItemStack() }, // TODO もっと相応しいアイコンに変える
            name = EnJa("Fairylands", "世界のそこかしこにあるおとぎの国"),
            description = EnJa("Travel the overworld and discover the Fairy Forest", "地上を旅して妖精の森を探す"),
            criterion = AdvancementCard.visit(key),
            type = AdvancementCardType.TOAST_ONLY,
        )
    },
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, ConventionalBiomeTags.IS_FLORAL, FAIRY_BIOME_TAG,
) {
    private val mirameraPlacedFeatureKey = Registries.PLACED_FEATURE with MirageFairy2024.identifier("miramera_fairy_forest")

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

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GIANT_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, SMALL_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY)
                BiomeDefaultFeatures.addOtherBirchTrees(lookupBackedBuilder)

                BiomeDefaultFeatures.addDefaultFlowers(lookupBackedBuilder)
                BiomeDefaultFeatures.addMeadowVegetation(lookupBackedBuilder)
                BiomeDefaultFeatures.addTaigaGrass(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultMushrooms(lookupBackedBuilder)
                BiomeDefaultFeatures.addDefaultExtraVegetation(lookupBackedBuilder)
                BiomeDefaultFeatures.addCommonBerryBushes(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, mirameraPlacedFeatureKey)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        super.init()

        // ミラメラ
        Feature.FLOWER.generator(MirageFairy2024.identifier("miramera_fairy_forest")) {
            registerConfiguredFeature { RandomPatchConfiguration(4, 7, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(BlockStateProvider.simple(BlockMaterialCard.MIRAMERA.block())))) }.generator {
                registerPlacedFeature(mirameraPlacedFeatureKey) { count(3) + flower(square, surface) }
            }
        }

        registerOverworldBiomeOverride(Biomes.FOREST)
        registerOverworldBiomeOverride(Biomes.WINDSWEPT_FOREST)
        registerOverworldBiomeOverride(Biomes.FLOWER_FOREST)
        registerOverworldBiomeOverride(Biomes.BIRCH_FOREST)
        registerOverworldBiomeOverride(Biomes.OLD_GROWTH_BIRCH_FOREST)
        registerOverworldBiomeOverride(Biomes.DARK_FOREST)

    }
}
