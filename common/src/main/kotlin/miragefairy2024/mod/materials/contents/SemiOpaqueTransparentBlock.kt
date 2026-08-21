package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.TransparentBlock
import net.minecraft.world.level.block.state.BlockState

class SemiOpaqueTransparentBlock(settings: Properties) : TransparentBlock(settings) {
    companion object {
        val CODEC: MapCodec<SemiOpaqueTransparentBlock> = simpleCodec(::SemiOpaqueTransparentBlock)
    }

    override fun codec() = CODEC

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getLightBlock(state: BlockState, level: BlockGetter, pos: BlockPos) = 1
}
