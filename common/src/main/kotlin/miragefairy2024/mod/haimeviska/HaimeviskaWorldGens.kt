package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.get
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.tree
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer
import java.util.OptionalInt

object HaimeviskaTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    val treeDecorator = HaimeviskaTreeDecorator()
    private val codec: MapCodec<HaimeviskaTreeDecorator> = MapCodec.unit { treeDecorator }
    val type: TreeDecoratorType<HaimeviskaTreeDecorator> = TreeDecoratorType(codec)
}

val HAIMEVISKA_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("haimeviska")
val HAIMEVISKA_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("haimeviska")
val HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("haimeviska_fairy_forest")
val HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("haimeviska_deep_fairy_forest")

context(ModContext)
fun initHaimeviskaWorldGens() {

    // TreeDecoratorの登録
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, HaimeviskaTreeDecoratorCard.identifier) { HaimeviskaTreeDecoratorCard.type }.register()

    // ConfiguredFeatureの登録
    registerDynamicGeneration(HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
        Feature.TREE with TreeConfiguration.TreeConfigurationBuilder(
            BlockStateProvider.simple(HaimeviskaBlockCard.LOG.block()),
            FancyTrunkPlacer(22, 10, 0), // 最大32
            BlockStateProvider.simple(HaimeviskaBlockCard.LEAVES.block()),
            FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(2), 4),
            TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)),
        ).ignoreVines().decorators(listOf(HaimeviskaTreeDecoratorCard.treeDecorator)).build()
    }

    // まばらなPlacedFeature
    registerDynamicGeneration(HAIMEVISKA_PLACED_FEATURE_KEY) {
        val placementModifiers = placementModifiers { per(512) + tree(HaimeviskaBlockCard.SAPLING.block()) }
        Registries.CONFIGURED_FEATURE[HAIMEVISKA_CONFIGURED_FEATURE_KEY] with placementModifiers
    }

    // 高密度のPlacedFeature
    registerDynamicGeneration(HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY) {
        val placementModifiers = placementModifiers { per(16) + tree(HaimeviskaBlockCard.SAPLING.block()) }
        Registries.CONFIGURED_FEATURE[HAIMEVISKA_CONFIGURED_FEATURE_KEY] with placementModifiers
    }

    // 超高密度のPlacedFeature
    registerDynamicGeneration(HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY) {
        val placementModifiers = placementModifiers { count(8) + tree(HaimeviskaBlockCard.SAPLING.block()) }
        Registries.CONFIGURED_FEATURE[HAIMEVISKA_CONFIGURED_FEATURE_KEY] with placementModifiers
    }

    // 平原・森林バイオームに配置
    HAIMEVISKA_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { +ConventionalBiomeTags.IS_PLAINS + +ConventionalBiomeTags.IS_FOREST }

}

class HaimeviskaTreeDecorator : TreeDecorator() {
    override fun type() = HaimeviskaTreeDecoratorCard.type
    override fun place(generator: Context) {
        generator.logs().forEach { blockPos ->
            if (!generator.level().isStateAtPosition(blockPos) { it == HaimeviskaBlockCard.LOG.block().defaultBlockState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみ
            val direction = Direction.from2DDataValue(generator.random().nextInt(4))
            if (!generator.isAir(blockPos.relative(direction))) return@forEach // 正面が空気の場合のみ
            val r = generator.random().nextInt(100)
            if (r < 25) {
                generator.setBlock(blockPos, HaimeviskaBlockCard.DRIPPING_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            } else if (r < 35) {
                generator.setBlock(blockPos, HaimeviskaBlockCard.HOLLOW_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            }
        }
    }
}
