package miragefairy2024.mod.plasticwood

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.count
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.tree
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.minecraft.core.registries.Registries
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer

// ConfiguredFeature と PlacedFeature のキーなのだ
val PLASTIC_TREE_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("plastic_tree")
val PLASTIC_TREE_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("plastic_tree")

// 琥珀色の原生林専用配置キーなのだ（高密度）
val PLASTIC_TREE_AMBER_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("plastic_tree_amber_forest")

context(ModContext)
fun initPlasticWoodWorldGens() {

    // 地形生成
    Feature.TREE.generator(MirageFairy2024.identifier("plastic_tree")) {
        registerConfiguredFeature(PLASTIC_TREE_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(PlasticWoodBlockCard.LOG.block()),
                GiantTrunkPlacer(18, 8, 0), // 2x2の太い幹、ハイメヴィスカより少し小さめなのだ（最大26）
                BlockStateProvider.simple(PlasticWoodBlockCard.LEAVES.block()),
                MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(10, 14)), // 針葉樹風の円錐形の葉、ハイメヴィスカより少し小さめなのだ
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().build()
        }.generator {

            // まばらな配置（将来的に他のバイオームにも生えることがあれば使うのだ）
            registerPlacedFeature(PLASTIC_TREE_PLACED_FEATURE_KEY) { per(1024) + tree(PlasticWoodBlockCard.SAPLING.block()) }

            // 琥珀色の原生林への高密度配置なのだ
            registerPlacedFeature(PLASTIC_TREE_AMBER_FOREST_PLACED_FEATURE_KEY) { count(6) + tree(PlasticWoodBlockCard.SAPLING.block()) }

        }
    }

}
