package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.util.get
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState

@Suppress("OVERRIDE_DEPRECATION")
class IncisedLogBlock(private val drippingLog: () -> TreeBlockCard, settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedLogBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ResourceLocation.CODEC.xmap<() -> TreeBlockCard>(
                    { identifier -> { TreeBlockCard.entries.first { it.identifier == identifier } } },
                    { it().identifier },
                ).fieldOf("dripping_log").forGetter { it.drippingLog },
                propertiesCodec(),
            ).apply(instance, ::IncisedLogBlock)
        }
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(100) == 0) {
            level.setBlock(pos, drippingLog().block().defaultBlockState().with(FACING, state[FACING]), UPDATE_ALL)
        }
    }
}
