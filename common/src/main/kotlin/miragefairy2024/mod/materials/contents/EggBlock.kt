package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.util.randomBoolean
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FallingBlock

class EggBlock(settings: Properties) : FallingBlock(settings) {
    companion object {
        val CODEC: MapCodec<EggBlock> = simpleCodec(::EggBlock)
    }

    override fun codec() = CODEC

    // 卵は着地時に必ず割れて、ブロックとして残らず、自分自身もドロップしない
    override fun falling(entity: FallingBlockEntity) {
        entity.disableDrop()
    }

    override fun onBrokenAfterFall(level: Level, pos: BlockPos, fallingBlock: FallingBlockEntity) {
        level.levelEvent(2001, pos, Block.getId(fallingBlock.blockState))
        if (level.random.randomBoolean(2, 1)) {
            val chicken = EntityType.CHICKEN.create(level) ?: return
            chicken.setAge(-24000)
            chicken.moveTo(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, level.random.nextFloat() * 360.0F, 0.0F)
            level.addFreshEntity(chicken)
        }
    }
}
