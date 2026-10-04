package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.isIn
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext

class NectarflowerBlock(settings: Properties) : BushBlock(settings) {
    companion object {
        val CODEC: MapCodec<NectarflowerBlock> = simpleCodec(::NectarflowerBlock)
    }

    override fun codec() = CODEC

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = DayflowerBlock.SHAPE

    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos) = super.mayPlaceOn(state, level, pos) || state isIn BlockMaterialCard.RESIN_CEMENT.block()
}
