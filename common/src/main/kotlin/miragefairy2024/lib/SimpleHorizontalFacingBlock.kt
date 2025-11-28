package miragefairy2024.lib

import miragefairy2024.util.with
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition

abstract class SimpleHorizontalFacingBlock(settings: Properties) : HorizontalDirectionalBlock(settings) {
    init {
        registerDefaultState(defaultBlockState().with(FACING, Direction.NORTH))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        return defaultBlockState().with(FACING, ctx.horizontalDirection.opposite)
    }
}
