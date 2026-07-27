package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

// シダの TallGrassBlock は、シダ以外だと骨粉で背の高い草に化けてしまううえ、codec の型が固定されていて継承できないのだ～🌱
// だから、シダと同じ姿かたちを持つ草を、親クラスの BushBlock から組み立てるのだ～🌱
class NectarflowerBlock(settings: Properties) : BushBlock(settings) {
    companion object {
        val CODEC: MapCodec<NectarflowerBlock> = simpleCodec(::NectarflowerBlock)

        // バニラのシダと同じ大きさなのだ～🌱
        private val SHAPE: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0)
    }

    override fun codec() = CODEC

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPE
}
