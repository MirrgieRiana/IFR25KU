package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import mirrg.kotlin.helium.floorToInt
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType
import java.util.function.BiConsumer
import kotlin.math.cos
import kotlin.math.sin

object SmallHaimeviskaTrunkPlacerCard {
    val identifier = MirageFairy2024.identifier("small_haimeviska")
    private val codec: MapCodec<SmallHaimeviskaTrunkPlacer> = MapCodec.unit { SmallHaimeviskaTrunkPlacer }
    val type: TrunkPlacerType<SmallHaimeviskaTrunkPlacer> = TrunkPlacerType(codec)
}

object SmallHaimeviskaTrunkPlacer : TrunkPlacer(8, 4, 0) {
    private const val LOWEST_LEAF_OFFSET_Y = 2

    override fun type() = SmallHaimeviskaTrunkPlacerCard.type

    // 枝を持たない1x1の主幹を建てて、その周りに葉をらせん状に付けるのだ～🌱
    override fun placeTrunk(
        level: LevelSimulatedReader,
        blockSetter: BiConsumer<BlockPos, BlockState>,
        random: RandomSource,
        freeTreeHeight: Int,
        pos: BlockPos,
        config: TreeConfiguration,
    ): List<FoliagePlacer.FoliageAttachment> {

        setDirtAt(level, blockSetter, random, pos.below(), config)

        (0..<freeTreeHeight).forEach { y ->
            placeLog(level, blockSetter, random, pos.above(y), config)
        }

        val foliageAttachments = mutableListOf<FoliagePlacer.FoliageAttachment>()

        // 幹の最上部のY+1に樹冠を乗せるのだ～🌱
        foliageAttachments += FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), 0, false)

        // 葉の位置を、幹の頂上から下に向かって決めていくのだ～🌱
        var angle = (Math.PI * 2) * random.nextDouble() // 最初の方位角はランダムなのだ～🌱
        val maxLeafOffsetY = freeTreeHeight - 1
        (maxLeafOffsetY downTo LOWEST_LEAF_OFFSET_Y).forEach { leafOffsetY ->

            // leafOffsetY == maxLeafOffsetY -> 0
            // leafOffsetY == LOWEST_LEAF_OFFSET_Y -> 1
            val ratio = if (LOWEST_LEAF_OFFSET_Y - maxLeafOffsetY <= 0) 0.0 else (leafOffsetY - maxLeafOffsetY).toDouble() / (LOWEST_LEAF_OFFSET_Y - maxLeafOffsetY).toDouble()
            val horizontalDistance = 1.0 + 1.4 * ratio

            val leafBlockPos = BlockPos(
                (pos.x + 0.5 + horizontalDistance * sin(angle)).floorToInt(),
                pos.y + leafOffsetY,
                (pos.z + 0.5 - horizontalDistance * cos(angle)).floorToInt(),
            )
            foliageAttachments += FoliagePlacer.FoliageAttachment(leafBlockPos, 0, false)

            angle += Math.toRadians(70.0)
        }

        return foliageAttachments
    }
}
