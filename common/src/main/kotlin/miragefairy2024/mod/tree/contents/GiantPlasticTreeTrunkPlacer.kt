package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.with
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.floorToInt
import mirrg.kotlin.helium.max
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType
import java.util.function.BiConsumer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GiantPlasticTreeTrunkPlacerCard {
    val identifier = MirageFairy2024.identifier("giant_plastic_tree")
    private val codec: MapCodec<GiantPlasticTreeTrunkPlacer> = MapCodec.unit { GiantPlasticTreeTrunkPlacer }
    val type: TrunkPlacerType<GiantPlasticTreeTrunkPlacer> = TrunkPlacerType(codec)
}

object GiantPlasticTreeTrunkPlacer : TrunkPlacer(15, 7, 0) {
    private const val BRANCH_THINNING_RATE = 0.4

    override fun type() = GiantPlasticTreeTrunkPlacerCard.type

    // 中心にまっすぐな2x2の主幹を建てて、そこからばらばらの方角へ斜め上に向かう枝を何本も伸ばすのだ～🌱
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
        var leafOffsetY = freeTreeHeight - 1 // 葉の最上部は、幹の頂上と同じ高さなのだ～🌱
        while (leafOffsetY >= freeTreeHeight * 0.3) { // 下部30%未満には葉を付けないのだ～🌱

            fun placeBranch() {
                if (random.nextDouble() < BRANCH_THINNING_RATE) return

                val angle = (Math.PI * 2) * random.nextDouble()

                // 幹の中心から葉までの水平距離は、木全体を回転楕円体とした関数とランダムなぶれで決まるのだ～🌱
                val horizontalDistance = run {
                    // freeTreeHeight = 10 のとき
                    val maxLeafOffsetY = freeTreeHeight - 1 // 9
                    val verticalRadius = maxLeafOffsetY / 2.0 // 4.5
                    val horizontalRadius = maxLeafOffsetY / 2.0 / 2.5 // 1.8
                    val normalizedY = if (verticalRadius <= 0.0) 0.0 else (leafOffsetY - verticalRadius) / verticalRadius // -1 ～ 0 ～ 1
                    val normalizedDistance = sqrt(1.0 - normalizedY * normalizedY) // 0 ～ 1 ～ 0
                    horizontalRadius * normalizedDistance * (1.0 + 0.3 * random.nextDouble()) // 0 ～ 1.8 * 1.3
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
                    val ratio = if (steps <= 0.0) 0.0 else step / steps
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
            placeBranch()
            placeBranch()

            leafOffsetY -= 1
        }

        return foliageAttachments
    }
}
