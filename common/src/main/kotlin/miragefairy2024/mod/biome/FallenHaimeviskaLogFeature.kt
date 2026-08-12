package miragefairy2024.mod.biome

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.Registration
import miragefairy2024.util.center
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.ground
import miragefairy2024.util.per
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

object FallenHaimeviskaLogFeatureCard {
    val identifier = MirageFairy2024.identifier("fallen_haimeviska_log")
    val feature = FallenHaimeviskaLogFeature(NoneFeatureConfiguration.CODEC)
    val placedFeatureKey = Registries.PLACED_FEATURE with identifier

    context(ModContext)
    fun init() {
        Registration(BuiltInRegistries.FEATURE, identifier) { feature }.register()
        feature.generator(identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature(placedFeatureKey) { per(8) + flower(center, ground) }
            }
        }
    }
}

class FallenHaimeviskaLogFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        // 幹が倒れていく方向と、その最大の長さなのだぁ🌱
        val direction = Direction.from2DDataValue(random.nextInt(4))
        val maxLength = random.nextIntBetweenInclusive(7, 13)

        // 折れ残った根元の高さなのだぁ🌱
        val stumpHeight = random.nextIntBetweenInclusive(1, 2)

        // 直下が地面で、かつ自身が空気や草などの置き換え可能なブロックである場合のみ、丸太を置けるのだぁ🌱
        fun canPlaceLog(blockPos: BlockPos): Boolean {
            val blockState = level.getBlockState(blockPos)
            if (!blockState.canBeReplaced()) return false
            if (!blockState.fluidState.isEmpty) return false // 水中や溶岩の中には倒れないのだぁ💧
            val belowBlockPos = blockPos.below()
            return level.getBlockState(belowBlockPos).isSolidRender(level, belowBlockPos)
        }

        // 地形の起伏に沿わせるため、基準の高さから上下1ブロックの範囲で、丸太を置ける高さを探すのだぁ🌱
        fun findLogBlockPos(baseBlockPos: BlockPos): BlockPos? {
            listOf(0, 1, -1).forEach { dy ->
                val blockPos = baseBlockPos.above(dy)
                if (canPlaceLog(blockPos)) return blockPos
            }
            return null
        }

        // 根元の位置なのだぁ🌱
        val stumpBlockPos = findLogBlockPos(originBlockPos) ?: return false

        // 根元から1ブロックの隙間を空けた先に、倒れた幹を地形に沿って伸ばすのだぁ🌱
        val logBlockPosList = mutableListOf<BlockPos>()
        run {
            var previousBlockPos = stumpBlockPos
            repeat(maxLength) {
                val blockPos = findLogBlockPos(previousBlockPos.relative(direction, if (logBlockPosList.isEmpty()) 2 else 1)) ?: return@run
                logBlockPosList += blockPos
                previousBlockPos = blockPos
            }
        }

        // 大径木の倒木と呼ぶには短すぎる場合は、生成をやめるのだぁ💧
        if (logBlockPosList.size < 5) return false

        // この時点で生成は確定なのだぁ🌱

        val logBlockState = HaimeviskaBlockCard.LOG.block().defaultBlockState()

        // 折れ残った根元なのだぁ🌱
        repeat(stumpHeight) { dy ->
            val blockPos = stumpBlockPos.above(dy)
            if (dy > 0 && !canPlaceLog(blockPos)) return@repeat
            level.setBlock(blockPos, logBlockState.with(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2)
        }

        // 地面に横たわる幹なのだぁ🌱
        logBlockPosList.forEach { blockPos ->
            level.setBlock(blockPos, logBlockState.with(RotatedPillarBlock.AXIS, direction.axis), 2)
        }

        // 幹の上には苔がまばらに生えるのだぁ✨
        logBlockPosList.forEach { blockPos ->
            if (random.nextFloat() >= 0.3F) return@forEach
            val mossBlockPos = blockPos.above()
            if (!level.getBlockState(mossBlockPos).canBeReplaced()) return@forEach
            level.setBlock(mossBlockPos, Blocks.MOSS_CARPET.defaultBlockState(), 2)
        }

        return true
    }
}
