package miragefairy2024.mod.mantle

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

/** マントルディメンションの岩石と同じ採掘の厳しさを持ちながら、経験値を落とす鉱石なのだ～🌱 */
class MantleOreBlock(private val xpRange: IntProvider, properties: Properties) : MantleBlock(properties) {
    companion object {
        val CODEC: MapCodec<MantleOreBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                IntProvider.codec(0, 10000).fieldOf("experience").forGetter { it.xpRange },
                propertiesCodec(),
            ).apply(instance, ::MantleOreBlock)
        }
    }

    override fun codec() = CODEC

    override fun spawnAfterBreak(state: BlockState, level: ServerLevel, pos: BlockPos, stack: ItemStack, dropExperience: Boolean) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience)
        if (dropExperience) tryDropExperience(level, pos, stack, xpRange)
    }
}
