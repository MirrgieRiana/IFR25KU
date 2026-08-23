package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.tree.TreeConfiguration
import miragefairy2024.util.get
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState

@Suppress("OVERRIDE_DEPRECATION")
class IncisedLogBlock(private val tree: TreeConfiguration, settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedLogBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                TreeConfiguration.CODEC.fieldOf("tree").forGetter { it.tree },
                propertiesCodec(),
            ).apply(instance, ::IncisedLogBlock)
        }
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (random.nextInt(100) == 0) {
            level.setBlock(pos, tree.getDrippingLogBlock().defaultBlockState().with(FACING, state[FACING]), UPDATE_ALL)
        }
    }
}
