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

object SmallHaimeviskaTrunkPlacer : TrunkPlacer(5, 3, 0) {
    private const val LOWEST_LEAF_OFFSET_Y = 2 // 地面から3マスの高さなのだ～🌱
    private const val LOWEST_LEAF_DISTANCE = 2.0
    private val ANGLE_STEP = Math.toRadians(137.5) // 実際の植物の葉序と同じ黄金角なのだ～🌱

    override fun type() = SmallHaimeviskaTrunkPlacerCard.type

    // 枝を持たない1x1の主幹を建てて、その周りに葉を一重らせん状に付けるのだ～🌱
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

        // 幹に沿う葉は、上に行くほど幹に近付きながららせんを描くのだ～🌱
        val topLeafOffsetY = freeTreeHeight - 1
        var angle = (Math.PI * 2) * random.nextDouble() // 最初の方位角はランダムなのだ～🌱
        (LOWEST_LEAF_OFFSET_Y..topLeafOffsetY).forEach { leafOffsetY ->
            val distance = if (topLeafOffsetY <= LOWEST_LEAF_OFFSET_Y) 0.0 else LOWEST_LEAF_DISTANCE * (topLeafOffsetY - leafOffsetY) / (topLeafOffsetY - LOWEST_LEAF_OFFSET_Y)
            val leafBlockPos = BlockPos(
                (pos.x + 0.5 + distance * sin(angle)).floorToInt(),
                pos.y + leafOffsetY,
                (pos.z + 0.5 - distance * cos(angle)).floorToInt(),
            )
            foliageAttachments += FoliagePlacer.FoliageAttachment(leafBlockPos, 0, false)
            angle += ANGLE_STEP
        }

        return foliageAttachments
    }
}
