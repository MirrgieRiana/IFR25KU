package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.util.get
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState

@Suppress("OVERRIDE_DEPRECATION")
class IncisedPlasticTreeLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedPlasticTreeLogBlock> = simpleCodec(::IncisedPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(100) == 0) {
            // 樹液が滴るプラノキの原木が実装されるまでの間、ハイメヴィスカのものに変化するのだ～🌱
            level.setBlock(pos, TreeBlockCard.DRIPPING_LOG.block().defaultBlockState().with(FACING, state[FACING]), UPDATE_ALL)
        }
    }
}
