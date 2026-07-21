package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.flower
import miragefairy2024.util.generator
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
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import kotlin.math.roundToInt

object RetrospectiveCityWallRuinFeatureCard {
    val identifier = MirageFairy2024.identifier("retrospective_city_wall_ruin")
    val feature = RetrospectiveCityWallRuinFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { count(2) + flower(square, surface) + retrospectiveCityFloorPlacementModifiers }
            }
        }
    }
}

class RetrospectiveCityWallRuinFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        fun schedule(blockPos: BlockPos, maxHeight: Int, isChiseledPosition: Boolean): (() -> Unit)? {
            val (down, up) = checkConflict(level, blockPos, maxHeight) ?: return null
            return {
                (-down..up).forEach { dy ->
                    val blockState = if (isChiseledPosition && dy == 2 && up >= 2) {
                        BlockMaterialCard.CHISELED_AURA_RESISTANT_CERAMIC.block().defaultBlockState()
                    } else if (dy == up) {
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
        val lengthPositive = random.nextIntBetweenInclusive(3, 5)
        val lengthNegative = random.nextIntBetweenInclusive(3, 5)
        val isXAxis = random.nextBoolean()

        val schedules = mutableListOf<() -> Unit>()
        schedules += schedule(originBlockPos, (height.toFloat() * nextHeightFactor()).roundToInt(), false) ?: return false
        (1..lengthPositive).forEach { distance ->
            val maxHeight = (height.toFloat() * Mth.cos(Mth.HALF_PI * (distance.toFloat() / (lengthPositive + 1).toFloat())) * nextHeightFactor()).roundToInt()
            val blockPos = if (isXAxis) originBlockPos.offset(distance, 0, 0) else originBlockPos.offset(0, 0, distance)
            schedules += schedule(blockPos, maxHeight, distance == 1) ?: return false
        }
        (1..lengthNegative).forEach { distance ->
            val maxHeight = (height.toFloat() * Mth.cos(Mth.HALF_PI * (distance.toFloat() / (lengthNegative + 1).toFloat())) * nextHeightFactor()).roundToInt()
            val blockPos = if (isXAxis) originBlockPos.offset(-distance, 0, 0) else originBlockPos.offset(0, 0, -distance)
            schedules += schedule(blockPos, maxHeight, distance == 1) ?: return false
        }

        schedules.forEach {
            it()
        }
        return schedules.isNotEmpty()
    }
}
