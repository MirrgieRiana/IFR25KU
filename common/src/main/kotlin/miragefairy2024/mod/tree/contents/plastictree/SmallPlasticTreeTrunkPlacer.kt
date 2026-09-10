package miragefairy2024.mod.tree.contents.plastictree

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
import kotlin.math.sqrt

object SmallPlasticTreeTrunkPlacerCard {
    val identifier = MirageFairy2024.identifier("small_plastic_tree")
    private val codec: MapCodec<SmallPlasticTreeTrunkPlacer> = MapCodec.unit { SmallPlasticTreeTrunkPlacer }
    val type: TrunkPlacerType<SmallPlasticTreeTrunkPlacer> = TrunkPlacerType(codec)
}

object SmallPlasticTreeTrunkPlacer : TrunkPlacer(6, 2, 0) {
    private const val LOWEST_LEAF_OFFSET_Y = 2

    // 葉は付着点を中心とする3x3で置かれるから、付着点が幹から両軸とも2ブロック離れると、幹と角でしか接さなくなって葉が崩れちゃうのだ～🌱
    // 片方の軸が2ブロック離れるには水平成分の絶対値が1.5必要だから、両軸が同時にそうなる最短の距離が1.5√2なのだ～🌱
    // その境界にちょうど乗せると浮動小数点数の丸めでどちらに転ぶか分からないから、0.99を掛けて確実に手前に置くのだ～🌱
    private val MAX_HORIZONTAL_DISTANCE = 1.5 * sqrt(2.0) * 0.99

    override fun type() = SmallPlasticTreeTrunkPlacerCard.type

    // 枝を持たない1x1の主幹を建てて、その周りに、方角を大きく変えながら葉を付けるのだ～🌱
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
            val ratio = if (LOWEST_LEAF_OFFSET_Y - maxLeafOffsetY == 0) 0.0 else (leafOffsetY - maxLeafOffsetY).toDouble() / (LOWEST_LEAF_OFFSET_Y - maxLeafOffsetY).toDouble()
            val horizontalDistance = 1.0 + (MAX_HORIZONTAL_DISTANCE - 1.0) * ratio

            val leafBlockPos = BlockPos(
                (pos.x + 0.5 + horizontalDistance * sin(angle)).floorToInt(),
                pos.y + leafOffsetY,
                (pos.z + 0.5 - horizontalDistance * cos(angle)).floorToInt(),
            )
            foliageAttachments += FoliagePlacer.FoliageAttachment(leafBlockPos, 0, false)

            angle += Math.toRadians(90.0 + 180.0 * random.nextDouble()) // 次の葉は、半周を挟んだ90°～270°だけ回った先に付くのだ～🌱
        }

        return foliageAttachments
    }
}
