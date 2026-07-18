package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.util.get
import miragefairy2024.util.getLevel
import miragefairy2024.util.randomBoolean
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class EggBlock(settings: Properties) : FallingBlock(settings) {
    companion object {
        val CODEC: MapCodec<EggBlock> = simpleCodec(::EggBlock)
        private val SHAPE: VoxelShape = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
    }

    override fun codec() = CODEC

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPE

    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false

    private fun spawnChickens(level: Level, x: Double, y: Double, z: Double) {
        if (!level.random.randomBoolean(2, 1)) return
        val count = if (level.random.randomBoolean(8, 1)) 4 else 1
        repeat(count) {
            val chicken = EntityType.CHICKEN.create(level) ?: return
            chicken.setAge(-24000)
            chicken.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F)
            level.addFreshEntity(chicken)
        }
    }

    // 卵は着地時に必ず割れて、ブロックとして残らず、自分自身もドロップしない
    override fun falling(entity: FallingBlockEntity) {
        entity.disableDrop()
    }

    override fun onBrokenAfterFall(level: Level, pos: BlockPos, fallingBlock: FallingBlockEntity) {
        level.levelEvent(2001, pos, Block.getId(fallingBlock.blockState))
        spawnChickens(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
    }

    override fun spawnAfterBreak(state: BlockState, level: ServerLevel, pos: BlockPos, stack: ItemStack, dropExperience: Boolean) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience)
        if (level.registryAccess()[Registries.ENCHANTMENT, Enchantments.SILK_TOUCH].getLevel(stack) <= 0) {
            spawnChickens(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        }
    }
}
