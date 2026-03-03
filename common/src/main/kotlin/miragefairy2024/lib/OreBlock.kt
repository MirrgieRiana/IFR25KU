package miragefairy2024.lib

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class OreBlock(private val xpRange: IntProvider, properties: Properties) : Block(properties) {
    companion object {
        val CODEC: MapCodec<OreBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                IntProvider.codec(0, 10000).fieldOf("experience").forGetter { it.xpRange },
                propertiesCodec(),
            ).apply(instance, ::OreBlock)
        }
    }

    override fun codec(): MapCodec<out OreBlock> = CODEC

    override fun spawnAfterBreak(state: BlockState, level: ServerLevel, pos: BlockPos, stack: ItemStack, dropExperience: Boolean) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience)
        if (dropExperience) {
            this.tryDropExperience(level, pos, stack, this.xpRange)
        }
    }
}
