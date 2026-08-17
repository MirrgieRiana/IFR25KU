package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.wood.WoodFoliagePlacer
import miragefairy2024.mod.wood.WoodTrunkPlacer
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
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType

object HaimeviskaTrunkPlacerCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    private val codec: MapCodec<HaimeviskaTrunkPlacer> = MapCodec.unit { HaimeviskaTrunkPlacer }
    val type: TrunkPlacerType<HaimeviskaTrunkPlacer> = TrunkPlacerType(codec)
}

object HaimeviskaFoliagePlacerCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    private val codec: MapCodec<HaimeviskaFoliagePlacer> = MapCodec.unit { HaimeviskaFoliagePlacer }
    val type: FoliagePlacerType<HaimeviskaFoliagePlacer> = FoliagePlacerType(codec)
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

    // TrunkPlacerの登録
    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, HaimeviskaTrunkPlacerCard.identifier) { HaimeviskaTrunkPlacerCard.type }.register()

    // FoliagePlacerの登録
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, HaimeviskaFoliagePlacerCard.identifier) { HaimeviskaFoliagePlacerCard.type }.register()

    // TreeDecoratorの登録
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, HaimeviskaTreeDecoratorCard.identifier) { HaimeviskaTreeDecoratorCard.type }.register()

    // 地形生成
    Feature.TREE.generator(MirageFairy2024.identifier("haimeviska")) {
        registerConfiguredFeature(HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
            TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(HaimeviskaBlockCard.LOG.block()),
                HaimeviskaTrunkPlacer, // 2x2の太い幹と、そこから伸びる枝
                BlockStateProvider.simple(HaimeviskaBlockCard.LEAVES.block()),
                HaimeviskaFoliagePlacer, // 枝先の葉の塊
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(HaimeviskaTreeDecoratorCard.treeDecorator, TrunkVineDecorator.INSTANCE, LeaveVineDecorator(0.25F))).build()
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

// ハイメヴィスカの幹なのだ～🌱
object HaimeviskaTrunkPlacer : WoodTrunkPlacer(22, 10, 0) { // 最大32
    /** 1段下の葉ごとに、方位角を時計回りにずらす角度なのだ。DNAの二重螺旋のメタファーなのだ。 */
    private const val FOLIAGE_ANGLE_STEP = 80.0

    override fun type() = HaimeviskaTrunkPlacerCard.type

    override fun getNextFoliageAngle(random: RandomSource, angle: Double) = angle + Math.toRadians(FOLIAGE_ANGLE_STEP)
}

// ハイメヴィスカの葉なのだ～🌱
object HaimeviskaFoliagePlacer : WoodFoliagePlacer() {
    override fun type() = HaimeviskaFoliagePlacerCard.type
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
