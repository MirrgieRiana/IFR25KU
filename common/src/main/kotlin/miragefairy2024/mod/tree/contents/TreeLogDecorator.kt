package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.util.with
import net.minecraft.core.Direction
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType

abstract class TreeLogDecorator(
    private val log: () -> TreeBlockCard,
    private val drippingLog: () -> TreeBlockCard,
    private val drippingLogPercentage: Int,
    private val hollowLog: (() -> TreeBlockCard)?,
    private val hollowLogPercentage: Int,
) : TreeDecorator() {
    override fun place(generator: Context) {
        generator.logs().forEach { blockPos ->
            if (!generator.level().isStateAtPosition(blockPos) { it == log().block().defaultBlockState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみ
            val direction = Direction.from2DDataValue(generator.random().nextInt(4))
            if (!generator.isAir(blockPos.relative(direction))) return@forEach // 正面が空気の場合のみ
            val r = generator.random().nextInt(100)
            if (r < drippingLogPercentage) {
                generator.setBlock(blockPos, drippingLog().block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            } else if (hollowLog != null && r < drippingLogPercentage + hollowLogPercentage) {
                generator.setBlock(blockPos, hollowLog.invoke().block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction))
            }
        }
    }
}

object HaimeviskaTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    private val codec: MapCodec<TreeLogDecorator> = MapCodec.unit { HaimeviskaTreeDecorator }
    val type: TreeDecoratorType<TreeLogDecorator> = TreeDecoratorType(codec)
}

object PlasticTreeTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("plastic_tree")
    private val codec: MapCodec<TreeLogDecorator> = MapCodec.unit { PlasticTreeTreeDecorator }
    val type: TreeDecoratorType<TreeLogDecorator> = TreeDecoratorType(codec)
}

object HaimeviskaTreeDecorator : TreeLogDecorator({ TreeBlockCard.LOG }, { TreeBlockCard.DRIPPING_LOG }, 12, { TreeBlockCard.HOLLOW_LOG }, 6) {
    override fun type() = HaimeviskaTreeDecoratorCard.type
}

object PlasticTreeTreeDecorator : TreeLogDecorator({ TreeBlockCard.PLASTIC_TREE_LOG }, { TreeBlockCard.PLASTIC_TREE_DRIPPING_LOG }, 25, null, 0) {
    override fun type() = PlasticTreeTreeDecoratorCard.type
}
