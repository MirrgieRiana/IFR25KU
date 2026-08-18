package miragefairy2024.mod.tree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.contents.GiantHaimeviskaFoliagePlacer
import miragefairy2024.mod.tree.contents.GiantHaimeviskaTrunkPlacer
import miragefairy2024.mod.tree.contents.GiantPlasticTreeFoliagePlacer
import miragefairy2024.mod.tree.contents.GiantPlasticTreeTrunkPlacer
import miragefairy2024.mod.tree.contents.HaimeviskaTreeDecorator
import miragefairy2024.mod.tree.contents.PlasticTreeTreeDecorator
import miragefairy2024.mod.tree.contents.SmallHaimeviskaFoliagePlacer
import miragefairy2024.mod.tree.contents.SmallHaimeviskaTrunkPlacer
import miragefairy2024.mod.tree.contents.SmallPlasticTreeTrunkPlacer
import miragefairy2024.util.count
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.plus
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.tree
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.registries.Registries
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator

val SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("small_haimeviska")
val SMALL_HAIMEVISKA_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_haimeviska")
val SMALL_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_haimeviska_fairy_forest")

val GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("giant_haimeviska")
val GIANT_HAIMEVISKA_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_haimeviska")
val GIANT_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_haimeviska_fairy_forest")
val GIANT_HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_haimeviska_deep_fairy_forest")

val SMALL_PLASTIC_TREE_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("small_plastic_tree")
val SMALL_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("small_plastic_tree_old_growth_amber_forest")

val GIANT_PLASTIC_TREE_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("giant_plastic_tree")
val GIANT_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("giant_plastic_tree_old_growth_amber_forest")

context(ModContext)
fun initTreeWorldGens() {
    Feature.TREE.generator(MirageFairy2024.identifier("small_haimeviska")) {
        registerConfiguredFeature(SMALL_HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.LOG.block()),
                SmallHaimeviskaTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.LEAVES.block()),
                SmallHaimeviskaFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), 0, 0.0F),
                TwoLayersFeatureSize(1, 0, 1),
            ).ignoreVines().decorators(listOf(HaimeviskaTreeDecorator, TrunkVineDecorator.INSTANCE, LeaveVineDecorator(0.05F))).build()
        }.generator {

            // まばら
            registerPlacedFeature(SMALL_HAIMEVISKA_PLACED_FEATURE_KEY) { per(1024) + tree(TreeBlockCard.SAPLING.block()) }.placeWhenVegetalDecoration { +ConventionalBiomeTags.IS_PLAINS + +ConventionalBiomeTags.IS_FOREST } // 平原・森林バイオームに配置

            // 高密度
            registerPlacedFeature(SMALL_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY) { per(16) + tree(TreeBlockCard.SAPLING.block()) }

        }
    }

    Feature.TREE.generator(MirageFairy2024.identifier("giant_haimeviska")) {
        registerConfiguredFeature(GIANT_HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.LOG.block()),
                GiantHaimeviskaTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.LEAVES.block()),
                GiantHaimeviskaFoliagePlacer,
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(HaimeviskaTreeDecorator, TrunkVineDecorator.INSTANCE, LeaveVineDecorator(0.05F))).build()
        }.generator {

            // まばら
            registerPlacedFeature(GIANT_HAIMEVISKA_PLACED_FEATURE_KEY) { per(1024) + tree(TreeBlockCard.SAPLING.block()) }.placeWhenVegetalDecoration { +ConventionalBiomeTags.IS_PLAINS + +ConventionalBiomeTags.IS_FOREST } // 平原・森林バイオームに配置

            // 高密度
            registerPlacedFeature(GIANT_HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY) { per(16) + tree(TreeBlockCard.SAPLING.block()) }

            // 超高密度
            registerPlacedFeature(GIANT_HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY) { count(8) + tree(TreeBlockCard.SAPLING.block()) }

        }
    }

    Feature.TREE.generator(MirageFairy2024.identifier("small_plastic_tree")) {
        registerConfiguredFeature(SMALL_PLASTIC_TREE_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LOG.block()),
                SmallPlasticTreeTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LEAVES.block()),
                SmallHaimeviskaFoliagePlacer(ConstantInt.of(1), ConstantInt.of(0), 0, 0.4F),
                TwoLayersFeatureSize(1, 0, 1),
            ).ignoreVines().decorators(listOf(PlasticTreeTreeDecorator)).build() // プラノキにはツタが全く生えないのだ～🌱
        }.generator {

            // 琥珀色の原生林の主役なのだ～🌱
            registerPlacedFeature(SMALL_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY) { count(3) + tree(TreeBlockCard.PLASTIC_TREE_SAPLING.block()) }

        }
    }

    Feature.TREE.generator(MirageFairy2024.identifier("giant_plastic_tree")) {
        registerConfiguredFeature(GIANT_PLASTIC_TREE_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LOG.block()),
                GiantPlasticTreeTrunkPlacer,
                BlockStateProvider.simple(TreeBlockCard.PLASTIC_TREE_LEAVES.block()),
                GiantPlasticTreeFoliagePlacer,
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(PlasticTreeTreeDecorator)).build() // プラノキにはツタが全く生えないのだ～🌱
        }.generator {

            // 小さいものと同じ頻度なのだ～🌱
            registerPlacedFeature(GIANT_PLASTIC_TREE_OLD_GROWTH_AMBER_FOREST_PLACED_FEATURE_KEY) { count(3) + tree(TreeBlockCard.PLASTIC_TREE_SAPLING.block()) }

        }
    }
}
