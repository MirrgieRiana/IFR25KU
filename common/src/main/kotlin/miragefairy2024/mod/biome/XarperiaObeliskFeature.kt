package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.Registration
import miragefairy2024.util.chanceTo
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.with
import mirrg.kotlin.helium.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.block.state.properties.StairsShape
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

object XarperiaObeliskFeatureCard {
    val identifier = MirageFairy2024.identifier("xarperia_obelisk")
    val feature = XarperiaObeliskFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(32) + flower(square, surface) }
            }
        }
    }
}

class XarperiaObeliskFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        fun schedule(blockPos: BlockPos, maxHeight: Int): (() -> Unit)? {
            val (down, _) = checkConflict(level, blockPos, maxHeight) ?: return null
            return {
                (-down until 0).forEach { dy ->
                    val blockState = BlockMaterialCard.MIRAGIDIAN_STEEL_TILES.block().defaultBlockState()
                    level.setBlock(blockPos.offset(0, dy, 0), blockState, 2)
                }
            }
        }

        val schedules = mutableListOf<() -> Unit>()
        val maxHeight = random.nextIntBetweenInclusive(BottomPlacer.height + TopPlacer.height, 32) atMost level.maxBuildHeight - originBlockPos.y
        var currentHeight = 0

        fun add(placer: Placer) {
            val dy = currentHeight
            schedules += {
                placer.place(level, random, originBlockPos.above(dy))
            }
            currentHeight += placer.height
        }

        // 基礎・衝突判定
        (-1..1).forEach { dx ->
            (-1..1).forEach { dz ->
                schedules += schedule(originBlockPos.offset(dx, 0, dz), 3) ?: return false
            }
        }

        // 本体
        add(BottomPlacer)
        while (true) {
            val placer = middlePlacers.weightedRandom(random)!!
            if (currentHeight + placer.height + TopPlacer.height > maxHeight) break
            add(placer)
        }
        add(TopPlacer)

        if (currentHeight > maxHeight) return false

        schedules.forEach {
            it()
        }
        return schedules.isNotEmpty()
    }
}


// BlockState Selectors

fun interface BlockStateSelector<T> {
    fun select(random: RandomSource, value: T): BlockState
}

private val normal = BlockStateSelector { random, _: Unit ->
    (if (random.nextInt(3) == 0) BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC else BlockMaterialCard.AURA_RESISTANT_CERAMIC).block().defaultBlockState()
}
private val stairs = BlockStateSelector { _, values: Pair<Direction, StairsShape> ->
    BlockMaterialCard.AURA_RESISTANT_CERAMIC_STAIRS.block().defaultBlockState().with(StairBlock.FACING, values.first).with(StairBlock.SHAPE, values.second)
}
private val slab = BlockStateSelector { _, type: SlabType ->
    BlockMaterialCard.AURA_RESISTANT_CERAMIC_SLAB.block().defaultBlockState().with(SlabBlock.TYPE, type)
}
private val smooth = BlockStateSelector { random, _: Unit ->
    (if (random.nextInt(3) == 0) BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC else BlockMaterialCard.SMOOTH_AURA_RESISTANT_CERAMIC).block().defaultBlockState()
}
private val polished = BlockStateSelector { random, _: Unit ->
    (if (random.nextInt(3) == 0) BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC else BlockMaterialCard.POLISHED_AURA_RESISTANT_CERAMIC).block().defaultBlockState()
}
private val chiseled = BlockStateSelector { _, _: Unit ->
    BlockMaterialCard.CHISELED_AURA_RESISTANT_CERAMIC.block().defaultBlockState()
}


// Place Utilities

private fun placeBlock(level: WorldGenLevel, random: RandomSource, blockPos: BlockPos, blockStateSelector: BlockStateSelector<Unit>) {
    if (level.isEmptyBlock(blockPos)) level.setBlock(blockPos, blockStateSelector.select(random, Unit), 2)
}

private fun placeFilling(level: WorldGenLevel, random: RandomSource, blockPos: BlockPos, blockStateSelector: BlockStateSelector<Unit>) {
    (-1..1).forEach { dx ->
        (-1..1).forEach { dz ->
            placeBlock(level, random, blockPos.offset(dx, 0, dz), blockStateSelector)
        }
    }
}

private fun placeStairsRing(level: WorldGenLevel, random: RandomSource, blockPos: BlockPos, blockStateSelector: BlockStateSelector<Pair<Direction, StairsShape>>) {
    placeBlock(level, random, blockPos.offset(-1, 0, -1)) { random, _ -> blockStateSelector.select(random, Pair(Direction.EAST, StairsShape.OUTER_RIGHT)) }
    placeBlock(level, random, blockPos.offset(-1, 0, 0)) { random, _ -> blockStateSelector.select(random, Pair(Direction.EAST, StairsShape.STRAIGHT)) }
    placeBlock(level, random, blockPos.offset(-1, 0, 1)) { random, _ -> blockStateSelector.select(random, Pair(Direction.EAST, StairsShape.OUTER_LEFT)) }
    placeBlock(level, random, blockPos.offset(0, 0, -1)) { random, _ -> blockStateSelector.select(random, Pair(Direction.SOUTH, StairsShape.STRAIGHT)) }

    placeBlock(level, random, blockPos.offset(0, 0, 1)) { random, _ -> blockStateSelector.select(random, Pair(Direction.NORTH, StairsShape.STRAIGHT)) }
    placeBlock(level, random, blockPos.offset(1, 0, -1)) { random, _ -> blockStateSelector.select(random, Pair(Direction.WEST, StairsShape.OUTER_LEFT)) }
    placeBlock(level, random, blockPos.offset(1, 0, 0)) { random, _ -> blockStateSelector.select(random, Pair(Direction.WEST, StairsShape.STRAIGHT)) }
    placeBlock(level, random, blockPos.offset(1, 0, 1)) { random, _ -> blockStateSelector.select(random, Pair(Direction.WEST, StairsShape.OUTER_RIGHT)) }
}


// Placer

private abstract class Placer(val height: Int) {
    abstract fun place(level: WorldGenLevel, random: RandomSource, blockPos: BlockPos)
}

private fun Placer(height: Int, block: (level: WorldGenLevel, random: RandomSource, blockPos: BlockPos) -> Unit): Placer {
    return object : Placer(height) {
        override fun place(level: WorldGenLevel, random: RandomSource, blockPos: BlockPos) {
            block(level, random, blockPos)
        }
    }
}

private object BottomPlacer : Placer(2) {
    override fun place(level: WorldGenLevel, random: RandomSource, blockPos: BlockPos) {
        (-1..1).forEach { dx ->
            (-1..1).forEach { dz ->
                placeBlock(level, random, blockPos.offset(dx, 0, dz), polished)
                placeBlock(level, random, blockPos.offset(dx, 1, dz), polished)
            }
        }
    }
}

private object TopPlacer : Placer(5) {
    override fun place(level: WorldGenLevel, random: RandomSource, blockPos: BlockPos) {
        placeBlock(level, random, blockPos.above(0), normal)
        placeStairsRing(level, random, blockPos.above(0), stairs)
        placeBlock(level, random, blockPos.above(1), normal)
        placeBlock(level, random, blockPos.above(2), normal)
        placeBlock(level, random, blockPos.above(3), chiseled)
        placeBlock(level, random, blockPos.above(4)) { random, _ -> slab.select(random, SlabType.BOTTOM) }
    }
}

private val middlePlacers = listOf(
    1.0 chanceTo Placer(2) { level, random, blockPos -> // 磨かれた
        placeFilling(level, random, blockPos.above(0), polished)
        placeFilling(level, random, blockPos.above(1), polished)
    },
    0.2 chanceTo Placer(2) { level, random, blockPos -> // 通常
        placeFilling(level, random, blockPos.above(0), normal)
        placeFilling(level, random, blockPos.above(1), normal)
    },
    0.2 chanceTo Placer(2) { level, random, blockPos -> // スムース
        placeFilling(level, random, blockPos.above(0), smooth)
        placeFilling(level, random, blockPos.above(1), smooth)
    },
    0.1 chanceTo Placer(3) { level, random, blockPos -> // 窪み
        placeFilling(level, random, blockPos.above(0), normal)
        placeBlock(level, random, blockPos.above(1), normal)
        placeStairsRing(level, random, blockPos.above(1), stairs)
        placeFilling(level, random, blockPos.above(2), normal)
    },
    0.05 chanceTo Placer(1) { level, random, blockPos -> // 金
        placeFilling(level, random, blockPos.above(0)) { _, _ -> Blocks.GOLD_BLOCK.defaultBlockState() }
    },
    0.05 chanceTo Placer(1) { level, random, blockPos -> // レッドストーン
        placeFilling(level, random, blockPos.above(0)) { _, _ -> Blocks.REDSTONE_BLOCK.defaultBlockState() }
    },
    0.03 chanceTo Placer(1) { level, random, blockPos -> // ラピスラズリ
        placeFilling(level, random, blockPos.above(0)) { _, _ -> Blocks.LAPIS_BLOCK.defaultBlockState() }
    },
    0.03 chanceTo Placer(1) { level, random, blockPos -> // エメラルド
        placeFilling(level, random, blockPos.above(0)) { _, _ -> Blocks.EMERALD_BLOCK.defaultBlockState() }
    },
    0.03 chanceTo Placer(1) { level, random, blockPos -> // 銅
        placeFilling(level, random, blockPos.above(0)) { _, _ -> Blocks.OXIDIZED_COPPER.defaultBlockState() }
    },
    0.03 chanceTo Placer(1) { level, random, blockPos -> // ネフライト
        placeFilling(level, random, blockPos.above(0)) { _, _ -> BlockMaterialCard.NEPHRITE_BLOCK.block().defaultBlockState() }
    },
    0.02 chanceTo Placer(1) { level, random, blockPos -> // ミラジディアンスチール
        placeFilling(level, random, blockPos.above(0)) { _, _ -> BlockMaterialCard.MIRAGIDIAN_STEEL_TILES.block().defaultBlockState() }
    },
    0.01 chanceTo Placer(1) { level, random, blockPos -> // 紅天石
        placeFilling(level, random, blockPos.above(0)) { _, _ -> BlockMaterialCard.XARPITE_BLOCK.block().defaultBlockState() }
    },
    0.01 chanceTo Placer(1) { level, random, blockPos -> // 鉄
        placeFilling(level, random, blockPos.above(0)) { _, _ -> Blocks.IRON_BLOCK.defaultBlockState() }
    },
    0.01 chanceTo Placer(1) { level, random, blockPos -> // ミラジディアン
        placeFilling(level, random, blockPos.above(0)) { _, _ -> BlockMaterialCard.MIRAGIDIAN_BLOCK.block().defaultBlockState() }
    },
    0.001 chanceTo Placer(1) { level, random, blockPos -> // ダイヤモンド
        placeFilling(level, random, blockPos.above(0)) { _, _ -> Blocks.DIAMOND_BLOCK.defaultBlockState() }
    },
)
