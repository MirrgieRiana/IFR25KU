package miragefairy2024.mod.haimeviska.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.util.get
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

class HaimeviskaIncisedLogBlockCard(configuration: WoodBlockConfiguration) : HaimeviskaHorizontalFacingLogBlockCard(configuration) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = IncisedHaimeviskaLogBlock(properties)
}

@Suppress("OVERRIDE_DEPRECATION")
class IncisedHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedHaimeviskaLogBlock> = simpleCodec(::IncisedHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(100) == 0) {
            world.setBlock(pos, HaimeviskaBlockCard.DRIPPING_LOG.block().defaultBlockState().with(FACING, state[FACING]), UPDATE_ALL)
        }
    }
}
