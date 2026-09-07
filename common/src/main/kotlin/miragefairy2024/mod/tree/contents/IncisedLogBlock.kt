package miragefairy2024.mod.tree.contents

import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.util.get
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

@Suppress("OVERRIDE_DEPRECATION")
abstract class IncisedLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    protected abstract fun getDrippingLogBlock(): Block

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(100) == 0) {
            level.setBlock(pos, getDrippingLogBlock().defaultBlockState().with(FACING, state[FACING]), UPDATE_ALL)
        }
    }
}
