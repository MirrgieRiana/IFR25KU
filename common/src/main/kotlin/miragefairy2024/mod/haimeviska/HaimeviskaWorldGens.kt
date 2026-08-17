package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.tree
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer

object HaimeviskaFoliagePlacerCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    private val codec: MapCodec<HaimeviskaConiferFoliagePlacer> = MapCodec.unit { HaimeviskaConiferFoliagePlacer }
    val type: FoliagePlacerType<HaimeviskaConiferFoliagePlacer> = FoliagePlacerType(codec)
}

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

    // FoliagePlacerの登録
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, HaimeviskaFoliagePlacerCard.identifier) { HaimeviskaFoliagePlacerCard.type }.register()

    // TreeDecoratorの登録
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, HaimeviskaTreeDecoratorCard.identifier) { HaimeviskaTreeDecoratorCard.type }.register()

    // 地形生成
    Feature.TREE.generator(MirageFairy2024.identifier("haimeviska")) {
        registerConfiguredFeature(HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(HaimeviskaBlockCard.LOG.block()),
                GiantTrunkPlacer(22, 10, 0), // 2x2の太い幹、最大32
                BlockStateProvider.simple(HaimeviskaBlockCard.LEAVES.block()),
                HaimeviskaConiferFoliagePlacer, // 枝付き針葉樹風の葉配置
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(HaimeviskaTreeDecoratorCard.treeDecorator)).build()
        }.generator {

            // まばら
            registerPlacedFeature(HAIMEVISKA_PLACED_FEATURE_KEY) { per(512) + tree(HaimeviskaBlockCard.SAPLING.block()) }.placeWhenVegetalDecoration { +ConventionalBiomeTags.IS_PLAINS + +ConventionalBiomeTags.IS_FOREST } // 平原・森林バイオームに配置

            // 高密度
            registerPlacedFeature(HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY) { per(16) + tree(HaimeviskaBlockCard.SAPLING.block()) }

            // 超高密度
            registerPlacedFeature(HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY) { count(8) + tree(HaimeviskaBlockCard.SAPLING.block()) }

        }
    }

}

// 枝付き針葉樹風の葉配置なのだ～🌱
// foliageHeightは4で、元のFancyFoliagePlacerと同じ高さなのだ。
// 各層に円形の葉を置いた上で、さらに各層で東西南北のうちランダムな方向に枝先の葉の房を追加するのだ。
// これにより、針葉樹らしく各段から横に枝が出ているように見えるのだ。
object HaimeviskaConiferFoliagePlacer : FoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)) {
    override fun type() = HaimeviskaFoliagePlacerCard.type

    override fun createFoliage(
        level: LevelSimulatedReader,
        blockSetter: FoliageSetter,
        random: RandomSource,
        config: TreeConfiguration,
        maxFreeTreeHeight: Int,
        attachment: FoliageAttachment,
        foliageHeight: Int,
        foliageRadius: Int,
        offset: Int,
    ) {
        val blockPos = attachment.pos()

        // 上から下に各層をループするのだ
        (offset downTo offset - foliageHeight).forEach { localY ->
            // この層の針葉樹的な基本半径（上ほど小さく、下ほど大きい）なのだ
            val depth = offset - localY // 0 = 頂上, foliageHeight = 最下層
            val baseRadius = foliageRadius + attachment.radiusOffset() + Mth.floor(depth.toFloat() / foliageHeight.toFloat() * 2.0F)

            // 中心の水平円形の葉の層を置くのだ
            placeLeavesRow(level, blockSetter, random, config, blockPos, baseRadius, localY, attachment.doubleTrunk())

            // 各層から枝先の葉の房を追加するのだ（1〜2方向）
            val branchCount = if (depth % 2 == 0) 2 else 1 // 偶数層は2方向、奇数層は1方向なのだ
            // 東西南北の中からbranchCount個の方向をランダムに選ぶのだ
            val allDirections = Direction.Plane.HORIZONTAL.toMutableList()
            val directions = (0 until branchCount).map { allDirections.removeAt(random.nextInt(allDirections.size)) }
            directions.forEach { direction ->
                // 枝先の中心位置（中心から baseRadius + 1 だけ離れた位置）なのだ
                val branchTipBlockPos = BlockPos(
                    blockPos.x + direction.stepX * (baseRadius + 1),
                    blockPos.y,
                    blockPos.z + direction.stepZ * (baseRadius + 1),
                )
                // 枝先に小さな葉の房を置くのだ（半径1）
                placeLeavesRow(level, blockSetter, random, config, branchTipBlockPos, 1, localY, attachment.doubleTrunk())
            }
        }
    }

    override fun foliageHeight(random: RandomSource, height: Int, config: TreeConfiguration) = 4 // 元のFancyFoliagePlacerと同じ高さなのだ

    override fun shouldSkipLocation(random: RandomSource, localX: Int, localY: Int, localZ: Int, range: Int, large: Boolean): Boolean {
        // 円形に近い形状にするのだ
        return localX * localX + localZ * localZ > range * range
    }
}

class HaimeviskaTreeDecorator : TreeDecorator() {
    override fun type() = HaimeviskaTreeDecoratorCard.type
    override fun place(generator: Context) {
        generator.logs().forEach { blockPos ->
            if (!generator.level().isStateAtPosition(blockPos) { it == HaimeviskaBlockCard.LOG.block().defaultBlockState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみ
            val direction = Direction.from2DDataValue(generator.random().nextInt(4))
            if (!generator.isAir(blockPos.relative(direction))) return@forEach // 正面が空気の場合のみ
            // 2x2幹では幹ブロック数が約4倍になるため、装飾の出現確率を約1/4に調整するのだ
            val r = generator.random().nextInt(100)
            if (r < 6) {
                generator.setBlock(blockPos, HaimeviskaBlockCard.DRIPPING_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            } else if (r < 9) {
                generator.setBlock(blockPos, HaimeviskaBlockCard.HOLLOW_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            }
        }
    }
}
