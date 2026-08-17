package miragefairy2024.mod.wood

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.util.valueproviders.ConstantInt
import net.minecraft.world.level.LevelSimulatedReader
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer
import java.util.function.BiConsumer
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

// 大木の幹なのだ～🌱
// 中心にまっすぐな2x2の主幹を建てて、そこから斜め上に向かう枝を何本も伸ばすのだ。
// FoliagePlacerには、幹に対する方角の情報が一切渡らないので、葉の位置はすべてこちら側で決めて、FoliageAttachmentとして渡すのだ。
abstract class WoodTrunkPlacer(baseHeight: Int, heightRandA: Int, heightRandB: Int) : TrunkPlacer(baseHeight, heightRandA, heightRandB) {
    companion object {
        /** 葉を配置する高さの間隔なのだ。 */
        private const val FOLIAGE_STEP = 2

        /** 葉を配置する高さの下限を、幹の高さに対する割合で表したものなのだ。 */
        private const val FOLIAGE_LOWER_LIMIT_RATIO = 0.3

        /** 幹からの距離に掛かる乱数の倍率の上限なのだ。下限は1.0なのだ。 */
        private const val FOLIAGE_DISTANCE_RANDOM_FACTOR = 1.3

        /** 枝の傾きなのだ。枝の根元は、幹からの距離にこの値を掛けた分だけ、葉より下になるのだ。 */
        private const val BRANCH_SLOPE = 0.5

        /** 2x2の幹を構成する4本の柱の、基準点からのXZ方向のずれなのだ。 */
        private val TRUNK_OFFSETS = listOf(0 to 0, 1 to 0, 1 to 1, 0 to 1)
    }

    /** 1段下の葉に移るときの、新しい方位角を決めるのだ。方位角は、北を0として時計回りに測ったラジアンなのだ。 */
    protected abstract fun getNextFoliageAngle(random: RandomSource, angle: Double): Double

    override fun placeTrunk(
        level: LevelSimulatedReader,
        blockSetter: BiConsumer<BlockPos, BlockState>,
        random: RandomSource,
        freeTreeHeight: Int,
        pos: BlockPos,
        config: TreeConfiguration,
    ): List<FoliagePlacer.FoliageAttachment> {

        // 2x2の幹が乗る4マス分の足元を土にするのだ
        val belowBlockPos = pos.below()
        setDirtAt(level, blockSetter, random, belowBlockPos, config)
        setDirtAt(level, blockSetter, random, belowBlockPos.east(), config)
        setDirtAt(level, blockSetter, random, belowBlockPos.south(), config)
        setDirtAt(level, blockSetter, random, belowBlockPos.south().east(), config)

        // 2x2の主幹を、頂上まで太さを保ったまま積むのだ
        val mutableBlockPos = BlockPos.MutableBlockPos()
        (0..<freeTreeHeight).forEach { trunkOffsetY ->
            TRUNK_OFFSETS.forEach { (trunkOffsetX, trunkOffsetZ) ->
                mutableBlockPos.setWithOffset(pos, trunkOffsetX, trunkOffsetY, trunkOffsetZ)
                placeLogIfFree(level, blockSetter, random, mutableBlockPos, config)
            }
        }

        // 2x2の幹の中央のXZ座標なのだ。ブロックの中心を半整数とみなすと、4本の柱の角が集まるここは整数座標になるのだ
        val centerX = pos.x + 1.0
        val centerZ = pos.z + 1.0

        // 葉の位置を、幹の頂上から下に向かって決めていくのだ
        val foliageAttachments = mutableListOf<FoliagePlacer.FoliageAttachment>()
        var angle = random.nextDouble() * (Math.PI * 2) // 最初の方位角はランダムなのだ
        var offsetY = freeTreeHeight - 1 // 葉の最上部は、幹の頂上と同じ高さなのだ
        while (offsetY >= freeTreeHeight * FOLIAGE_LOWER_LIMIT_RATIO) {

            // 幹からの距離は、縦の差し渡しが幹の高さ、横の差し渡しがその1/3であるような楕円で決まるのだ
            // 下部30%より下には葉が無いので、全体としては、下側がちょん切れた回転楕円体になるのだ
            val verticalRadius = freeTreeHeight / 2.0
            val horizontalRadius = freeTreeHeight / 6.0
            val normalizedY = (offsetY - verticalRadius) / verticalRadius
            val distance = horizontalRadius * sqrt(1.0 - normalizedY * normalizedY) * (1.0 + random.nextDouble() * (FOLIAGE_DISTANCE_RANDOM_FACTOR - 1.0))

            // 方位角は、北を0として時計回りに測るのだ
            val leafX = centerX + distance * sin(angle)
            val leafZ = centerZ - distance * cos(angle)
            val leafBlockPos = BlockPos(floor(leafX).toInt(), pos.y + offsetY, floor(leafZ).toInt())

            // 枝の根元は、葉より、幹からの距離の半分だけ下なのだ。これにより、枝は常に一定の傾きの坂になるのだ
            placeBranch(level, blockSetter, random, leafBlockPos, centerX, pos.y + offsetY + 0.5 - distance * BRANCH_SLOPE, centerZ, pos, config)

            foliageAttachments += FoliagePlacer.FoliageAttachment(leafBlockPos, 0, false)

            angle = getNextFoliageAngle(random, angle)
            offsetY -= FOLIAGE_STEP
        }

        return foliageAttachments
    }

    /** 枝先から根元に向かって、1ブロックずつ原木を置いていくのだ。 */
    private fun placeBranch(
        level: LevelSimulatedReader,
        blockSetter: BiConsumer<BlockPos, BlockState>,
        random: RandomSource,
        tipBlockPos: BlockPos,
        baseX: Double,
        baseY: Double,
        baseZ: Double,
        trunkBlockPos: BlockPos,
        config: TreeConfiguration,
    ) {
        val tipX = tipBlockPos.x + 0.5
        val tipY = tipBlockPos.y + 0.5
        val tipZ = tipBlockPos.z + 0.5
        val steps = ceil(maxOf(abs(baseX - tipX), abs(baseY - tipY), abs(baseZ - tipZ))).toInt()
        if (steps <= 0) return
        (0..steps).forEach { step ->
            val ratio = step.toDouble() / steps
            val blockPos = BlockPos(
                floor(tipX + (baseX - tipX) * ratio).toInt(),
                floor(tipY + (baseY - tipY) * ratio).toInt(),
                floor(tipZ + (baseZ - tipZ) * ratio).toInt(),
            )
            if (blockPos.x in trunkBlockPos.x..trunkBlockPos.x + 1 && blockPos.z in trunkBlockPos.z..trunkBlockPos.z + 1) return@forEach // 主幹の柱の上には置かないのだ
            val axis = if (abs(blockPos.x + 0.5 - baseX) >= abs(blockPos.z + 0.5 - baseZ)) Direction.Axis.X else Direction.Axis.Z // 幹の中央から見て、より遠く離れている方の軸を向けるのだ
            placeLog(level, blockSetter, random, blockPos, config) { it.trySetValue(RotatedPillarBlock.AXIS, axis) }
        }
    }
}

// 大木の葉なのだ～🌱
// FoliagePlacerには、幹に対する方角の情報が一切渡らないので、回転対称な形しか作れないのだ。
// そこで、ここでは1個の葉の塊だけを作って、枝の張り方はWoodTrunkPlacerに任せているのだ。
abstract class WoodFoliagePlacer : FoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)) {
    companion object {
        /** 葉を積む段数なのだ。 */
        private const val FOLIAGE_LAYER_COUNT = 3
    }

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
        // 上の段ほど半径が小さい円盤を積むのだ
        (0..<FOLIAGE_LAYER_COUNT).forEach { localY ->
            placeLeavesRow(level, blockSetter, random, config, attachment.pos(), foliageRadius + attachment.radiusOffset() - localY, offset + localY, attachment.doubleTrunk())
        }
    }

    override fun foliageHeight(random: RandomSource, height: Int, config: TreeConfiguration) = FOLIAGE_LAYER_COUNT - 1

    override fun shouldSkipLocation(random: RandomSource, localX: Int, localY: Int, localZ: Int, range: Int, large: Boolean) = localX * localX + localZ * localZ > range * range // 円盤型にするのだ
}
