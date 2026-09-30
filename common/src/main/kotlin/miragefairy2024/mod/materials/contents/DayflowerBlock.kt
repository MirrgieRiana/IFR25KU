package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext

// ミツクサと同じく、シダと同じ姿かたちを持つ草なのだ～🌱
class DayflowerBlock(settings: Properties) : BushBlock(settings) {
    companion object {
        val CODEC: MapCodec<DayflowerBlock> = simpleCodec(::DayflowerBlock)
    }

    override fun codec() = CODEC

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = NectarflowerBlock.SHAPE
}
