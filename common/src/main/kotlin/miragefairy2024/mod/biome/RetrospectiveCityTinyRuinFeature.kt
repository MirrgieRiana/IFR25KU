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
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter

object RetrospectiveCityTinyRuinFeatureCard {
    val identifier = MirageFairy2024.identifier("retrospective_city_tiny_ruin")
    val feature = RetrospectiveCityTinyRuinFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) {
                    count(4) + flower(square, surface) + listOf(
                        BlockPredicateFilter.forPredicate(BlockPredicate.matchesTag(Direction.DOWN.normal, RETROSPECTIVE_CITY_FLOOR_BLOCK_TAG))
                    )
                }
            }
        }
    }
}

class RetrospectiveCityTinyRuinFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        fun BlockState.isConflicting() = this isIn RETROSPECTIVE_CITY_BUILDING_BLOCK_TAG && this isNotIn RETROSPECTIVE_CITY_FLOOR_BLOCK_TAG

        val height = random.nextIntBetweenInclusive(2, 8)
        val isBricks = random.nextBoolean()

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
                            0 -> (if (isBricks) BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS else BlockMaterialCard.AURA_RESISTANT_CERAMIC_STAIRS).block().defaultBlockState().with(StairBlock.FACING, Direction.NORTH)
                            1 -> (if (isBricks) BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS else BlockMaterialCard.AURA_RESISTANT_CERAMIC_STAIRS).block().defaultBlockState().with(StairBlock.FACING, Direction.SOUTH)
                            2 -> (if (isBricks) BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS else BlockMaterialCard.AURA_RESISTANT_CERAMIC_STAIRS).block().defaultBlockState().with(StairBlock.FACING, Direction.WEST)
                            3 -> (if (isBricks) BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_STAIRS else BlockMaterialCard.AURA_RESISTANT_CERAMIC_STAIRS).block().defaultBlockState().with(StairBlock.FACING, Direction.EAST)
                            4, 5 -> (if (isBricks) BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS_SLAB else BlockMaterialCard.AURA_RESISTANT_CERAMIC_SLAB).block().defaultBlockState().with(SlabBlock.TYPE, SlabType.BOTTOM)
                            else -> (if (isBricks) BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS else BlockMaterialCard.AURA_RESISTANT_CERAMIC).block().defaultBlockState()
                        }
                    } else if (dy <= -1) {
                        BlockMaterialCard.SMOOTH_AURA_RESISTANT_CERAMIC.block().defaultBlockState()
                    } else {
                        if (isBricks) {
                            if (random.nextInt(3) == 0) {
                                BlockMaterialCard.CRACKED_AURA_RESISTANT_CERAMIC_BRICKS.block().defaultBlockState()
                            } else {
                                BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS.block().defaultBlockState()
                            }
                        } else {
                            BlockMaterialCard.AURA_RESISTANT_CERAMIC.block().defaultBlockState()
                        }
                    }
                    level.setBlock(blockPos.offset(0, dy, 0), blockState, 2)
                }
            }
        }

        val schedules = mutableListOf<() -> Unit>()
        fun f(dx: Int, dz: Int, chance: Float, hasRandomHeight: Boolean): Boolean {
            if (random.nextFloat() < chance) {
                val maxHeight = if (hasRandomHeight) random.nextIntBetweenInclusive(1, height) else height
                schedules += schedule(originBlockPos.offset(dx, 0, dz), maxHeight) ?: return false
            }
            return true
        }
        if (!f(0, 0, 1.0F, false)) return false
        if (!f(-1, 0, 0.5F, true)) return false
        if (!f(1, 0, 0.5F, true)) return false
        if (!f(0, -1, 0.5F, true)) return false
        if (!f(0, 1, 0.5F, true)) return false
        if (!f(-1, -1, 0.25F, true)) return false
        if (!f(-1, 1, 0.25F, true)) return false
        if (!f(1, -1, 0.25F, true)) return false
        if (!f(1, 1, 0.25F, true)) return false

        schedules.forEach {
            it()
        }
        return schedules.isNotEmpty()
    }
}

