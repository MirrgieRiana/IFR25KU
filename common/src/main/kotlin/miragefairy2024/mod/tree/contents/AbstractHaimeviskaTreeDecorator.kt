package miragefairy2024.mod.tree.contents

import miragefairy2024.util.with
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator

/** 置き換え先は、原木のブロックと、それが選ばれる100分率の確率の組で与えるのだ～🌱 */
abstract class AbstractHaimeviskaTreeDecorator(
    private val log: () -> Block,
    private val drippingLogReplacement: Pair<() -> Block, Int>?,
    private val hollowLogReplacement: Pair<() -> Block, Int>?,
) : TreeDecorator() {
    override fun place(generator: Context) {
        generator.logs().forEach { blockPos ->
            if (!generator.level().isStateAtPosition(blockPos) { it == log().defaultBlockState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみ
            val direction = Direction.from2DDataValue(generator.random().nextInt(4))
            if (!generator.isAir(blockPos.relative(direction))) return@forEach // 正面が空気の場合のみ
            val random = generator.random().nextInt(100)
            var threshold = 0
            if (drippingLogReplacement != null) {
                threshold += drippingLogReplacement.second
                if (random < threshold) generator.setBlock(blockPos, drippingLogReplacement.first().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
                return
            }
            if (hollowLogReplacement != null) {
                threshold += hollowLogReplacement.second
                if (random < threshold) generator.setBlock(blockPos, hollowLogReplacement.first().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
                return
            }
        }
    }
}
