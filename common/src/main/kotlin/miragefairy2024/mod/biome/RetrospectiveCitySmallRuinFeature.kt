package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotIn
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.util.Mth
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter
import kotlin.math.roundToInt

object RetrospectiveCitySmallRuinFeatureCard {
    val identifier = MirageFairy2024.identifier("retrospective_city_small_ruin")
    val feature = RetrospectiveCitySmallRuinFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) {
                    count(2) + flower(square, surface) + listOf(
                        BlockPredicateFilter.forPredicate(BlockPredicate.matchesTag(Direction.DOWN.normal, RETROSPECTIVE_CITY_FLOOR_BLOCK_TAG))
                    )
                }
            }
        }
    }
}

class RetrospectiveCitySmallRuinFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        fun BlockState.isConflicting() = this isIn RETROSPECTIVE_CITY_BUILDING_BLOCK_TAG && this isNotIn RETROSPECTIVE_CITY_FLOOR_BLOCK_TAG

        fun schedule(blockPos: BlockPos, maxHeight: Int): (() -> Unit)? {

            // 下方向にNGブロックを探索しつつ空洞を壁で埋める
            var down = 0
            while (true) {
                if (level.getBlockState(blockPos.below(down + 1)).isConflicting()) return null // 床に道を構成するブロックがある
                if (down >= 3) break
                if (!level.isEmptyBlock(blockPos.below(down + 1))) break // これより下に拡張できないので抜ける
                down++
            }

            // 上方向にNGブロックを判定
            var up = 0
            while (true) {
                if (level.getBlockState(blockPos.above(up)).isConflicting()) return null // そのマスに建物を構成するブロックがある
                if (up + 1 >= maxHeight) break // 現在の高さが最大高さに到達しているので抜ける
                if (!level.isEmptyBlock(blockPos.above(up + 1))) break // これより上に拡張できないので抜ける
                up++
            }

            return {
                (-down..up).forEach { dy ->
                    val blockState = if (dy == up) {
                        when (random.nextInt(8)) {
                            0 -> BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS.block().defaultBlockState().with(StairBlock.FACING, Direction.NORTH)
                            1 -> BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS.block().defaultBlockState().with(StairBlock.FACING, Direction.SOUTH)
                            2 -> BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS.block().defaultBlockState().with(StairBlock.FACING, Direction.WEST)
                            3 -> BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS.block().defaultBlockState().with(StairBlock.FACING, Direction.EAST)
                            4, 5 -> BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_SLAB.block().defaultBlockState().with(SlabBlock.TYPE, SlabType.BOTTOM)
                            else -> BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS.block().defaultBlockState()
                        }
                    } else if (dy <= -1) {
                        BlockMaterialCard.SMOOTH_AURA_RESISTANT_CERAMIC.block().defaultBlockState()
                    } else {
                        if (random.nextInt(3) == 0) {
                            BlockMaterialCard.CRACKED_AURA_RESISTANT_CERAMIC_BRICKS.block().defaultBlockState()
                        } else {
                            BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS.block().defaultBlockState()
                        }
                    }
                    level.setBlock(blockPos.offset(0, dy, 0), blockState, 2)
                }
            }
        }

        fun nextHeightFactor() = 0.75F + 0.25F * random.nextFloat()

        val height = random.nextIntBetweenInclusive(3, 6)
        val lengthX = random.nextIntBetweenInclusive(3, 6)
        val lengthZ = random.nextIntBetweenInclusive(3, 6)
        val signX = if (random.nextBoolean()) 1 else -1
        val signZ = if (random.nextBoolean()) 1 else -1

        val schedules = mutableListOf<() -> Unit>()
        schedules += schedule(originBlockPos, (height.toFloat() * nextHeightFactor()).roundToInt()) ?: return false
        (1..lengthX).forEach { distance ->
            val offset = distance * signX
            val maxHeight = (height.toFloat() * Mth.cos(Mth.HALF_PI * (distance.toFloat() / (lengthX + 1).toFloat())) * nextHeightFactor()).roundToInt()
            schedules += schedule(originBlockPos.offset(offset, 0, 0), maxHeight) ?: return false
        }
        (1..lengthZ).forEach { distance ->
            val offset = distance * signZ
            val maxHeight = (height.toFloat() * Mth.cos(Mth.HALF_PI * (distance.toFloat() / (lengthZ + 1).toFloat())) * nextHeightFactor()).roundToInt()
            schedules += schedule(originBlockPos.offset(0, 0, offset), maxHeight) ?: return false
        }

        schedules.forEach {
            it()
        }
        return schedules.isNotEmpty()
    }
}
