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
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.floorToInt
import mirrg.kotlin.helium.max
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.util.RandomSource
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType
import java.util.function.BiConsumer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
    private val codec: MapCodec<HaimeviskaTreeDecorator> = MapCodec.unit { HaimeviskaTreeDecorator }
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
                HaimeviskaTrunkPlacer,
                BlockStateProvider.simple(HaimeviskaBlockCard.LEAVES.block()),
                HaimeviskaFoliagePlacer,
                TwoLayersFeatureSize(1, 1, 2),
            ).ignoreVines().decorators(listOf(HaimeviskaTreeDecorator, TrunkVineDecorator.INSTANCE, LeaveVineDecorator(0.25F))).build()
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

object HaimeviskaTrunkPlacer : TrunkPlacer(22, 10, 0) {
    override fun type() = HaimeviskaTrunkPlacerCard.type

    // 中心にまっすぐな2x2の主幹を建てて、そこから斜め上に向かう枝を二重らせん状に何本も伸ばすのだ～🌱
    override fun placeTrunk(
        level: LevelSimulatedReader,
        blockSetter: BiConsumer<BlockPos, BlockState>,
        random: RandomSource,
        freeTreeHeight: Int,
        pos: BlockPos,
        config: TreeConfiguration,
    ): List<FoliagePlacer.FoliageAttachment> {

        // 2x2の幹が乗る4マス分の足元を土にするのだ～🌱
        run {
            val blockPos = pos.below()
            setDirtAt(level, blockSetter, random, blockPos, config)
            setDirtAt(level, blockSetter, random, blockPos.east(), config)
            setDirtAt(level, blockSetter, random, blockPos.south(), config)
            setDirtAt(level, blockSetter, random, blockPos.south().east(), config)
        }

        // 2x2の主幹を、頂上まで太さを保ったまま積むのだ～🌱
        run {
            val blockPos = BlockPos.MutableBlockPos()
            (0..<freeTreeHeight).forEach { y ->
                blockPos.setWithOffset(pos, 0, y, 0)
                placeLogIfFree(level, blockSetter, random, blockPos, config)
                blockPos.setWithOffset(pos, 1, y, 0)
                placeLogIfFree(level, blockSetter, random, blockPos, config)
                blockPos.setWithOffset(pos, 1, y, 1)
                placeLogIfFree(level, blockSetter, random, blockPos, config)
                blockPos.setWithOffset(pos, 0, y, 1)
                placeLogIfFree(level, blockSetter, random, blockPos, config)
            }
        }

        val foliageAttachments = mutableListOf<FoliagePlacer.FoliageAttachment>()

        // 頂上の1個の葉なのだ～🌱
        foliageAttachments += FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight - 1), 1, true)

        // 葉の位置を、幹の頂上から下に向かって決めていくのだ～🌱
        var angle = (Math.PI * 2) * random.nextDouble() // 最初の方位角はランダムなのだ～🌱
        var leafOffsetY = freeTreeHeight - 1 // 葉の最上部は、幹の頂上と同じ高さなのだ～🌱
        while (leafOffsetY >= freeTreeHeight * 0.3) { // 下部30%未満には葉を付けないのだ～🌱

            fun placeBranch(distanceMultiplier: Double) {

                // 幹の中心から葉までの水平距離は、木全体を回転楕円体とした関数で決まるのだ～🌱
                val horizontalDistance = run {
                    // freeTreeHeight = 10 のとき
                    val maxLeafOffsetY = freeTreeHeight - 1 // 9
                    val verticalRadius = maxLeafOffsetY / 2.0 // 4.5
                    val horizontalRadius = maxLeafOffsetY / 2.0 / 2.5 // 1.8
                    val normalizedY = (leafOffsetY - verticalRadius) / verticalRadius // -1 ～ 0 ～ 1
                    val normalizedDistance = sqrt(1.0 - normalizedY * normalizedY) // 0 ～ 1 ～ 0
                    horizontalRadius * normalizedDistance * distanceMultiplier // 0 ～ 1.8 * distanceMultiplier
                }

                val baseX = pos.x + 1.0
                val baseY = pos.y + 0.5 + ((leafOffsetY - horizontalDistance * 0.5) atLeast 0.0) // 幹の接続部分のYは葉の水平距離に応じて下に下がるのだ～🌱
                val baseZ = pos.z + 1.0

                val leafX = baseX + horizontalDistance * sin(angle)
                val leafY = pos.y + 0.5 + leafOffsetY
                val leafZ = baseZ - horizontalDistance * cos(angle)

                val steps = abs(leafX - baseX) max abs(leafY - baseY) max abs(leafZ - baseZ) // 3.2, 3.6, 4.5 のとき 4.5
                val axis = if (abs(leafX - baseX) >= abs(leafZ - baseZ)) Direction.Axis.X else Direction.Axis.Z

                // 枝の原木を設置するのだ～🌱
                fun placeLog(step: Double) {
                    val ratio = step / steps
                    val blockPos = BlockPos(
                        (leafX + (baseX - leafX) * ratio).floorToInt(),
                        (leafY + (baseY - leafY) * ratio).floorToInt(),
                        (leafZ + (baseZ - leafZ) * ratio).floorToInt(),
                    )
                    if (blockPos.x in pos.x..pos.x + 1 && blockPos.z in pos.z..pos.z + 1) return // 主幹の上には置かないのだ～🌱
                    placeLog(level, blockSetter, random, blockPos, config) { it.with(RotatedPillarBlock.AXIS, axis) }
                }
                repeat(steps.floorToInt()) { step ->
                    placeLog(step.toDouble())
                }
                placeLog(steps)

                // 葉を定義するのだ～🌱♪
                val leafBlockPos = BlockPos(leafX.floorToInt(), leafY.floorToInt(), leafZ.floorToInt())
                foliageAttachments += FoliagePlacer.FoliageAttachment(leafBlockPos, 0, false)

            }
            placeBranch(1.0 * (1.0 + 0.3 * random.nextDouble()))
            placeBranch(-1.0 * (1.0 + 0.3 * random.nextDouble()))

            angle += Math.toRadians(35.0)
            leafOffsetY -= 1
        }

        return foliageAttachments
    }
}

object HaimeviskaFoliagePlacer : FoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)) {
    private const val FOLIAGE_LAYER_COUNT = 2

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
        (0..<FOLIAGE_LAYER_COUNT).forEach { localY ->
            // 上の段ほど半径が小さい円盤を積むのだ～🌱
            val range = foliageRadius + attachment.radiusOffset() - localY
            if (range > 0) {
                // 幹に対する方角の情報が一切渡らないので、回転対称な形しか作れないのだぁ…🌧️
                placeLeavesRow(
                    level,
                    blockSetter,
                    random,
                    config,
                    attachment.pos(),
                    range,
                    offset + localY,
                    attachment.doubleTrunk(),
                )
            }
        }
    }

    override fun foliageHeight(random: RandomSource, height: Int, config: TreeConfiguration) = FOLIAGE_LAYER_COUNT - 1

    override fun shouldSkipLocation(random: RandomSource, localX: Int, localY: Int, localZ: Int, range: Int, large: Boolean) = localX * localX + localZ * localZ > range * range
}

object HaimeviskaTreeDecorator : TreeDecorator() {
    override fun type() = HaimeviskaTreeDecoratorCard.type
    override fun place(generator: Context) {
        generator.logs().forEach { blockPos ->
            if (!generator.level().isStateAtPosition(blockPos) { it == HaimeviskaBlockCard.LOG.block().defaultBlockState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみ
            val direction = Direction.from2DDataValue(generator.random().nextInt(4))
            if (!generator.isAir(blockPos.relative(direction))) return@forEach // 正面が空気の場合のみ
            val r = generator.random().nextInt(100)
            if (r < 12) {
                generator.setBlock(blockPos, HaimeviskaBlockCard.DRIPPING_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            } else if (r < 18) {
                generator.setBlock(blockPos, HaimeviskaBlockCard.HOLLOW_LOG.block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            }
        }
    }
}
