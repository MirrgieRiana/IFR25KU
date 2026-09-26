package miragefairy2024.mod.mantle

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

/** [MantleBlock] と同じ採掘の厳しさを持つ、ハーフブロックなのだ～🌱 */
class MantleSlabBlock(properties: Properties) : SlabBlock(properties) {
    companion object {
        val CODEC: MapCodec<MantleSlabBlock> = simpleCodec(::MantleSlabBlock)
    }

    override fun codec() = CODEC

    override fun getDestroyProgress(state: BlockState, player: Player, level: BlockGetter, pos: BlockPos): Float {
        if (!player.hasCorrectToolForDrops(state)) return 0.0F
        return super.getDestroyProgress(state, player, level, pos)
    }

    override fun playerDestroy(level: Level, player: Player, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool)
        tool.hurtAndBreak(MANTLE_MINING_DAMAGE_FACTOR - 1, player, EquipmentSlot.MAINHAND)
    }
}

/** [MantleBlock] と同じ採掘の厳しさを持つ、階段なのだ～🌱 */
class MantleStairBlock(baseState: BlockState, properties: Properties) : StairBlock(baseState, properties) {
    companion object {
        val CODEC: MapCodec<MantleStairBlock> = simpleCodec { MantleStairBlock(MantleBlockCard.REINFORCED_METAL_BLOCK.block().defaultBlockState(), it) }
    }

    override fun codec() = CODEC

    override fun getDestroyProgress(state: BlockState, player: Player, level: BlockGetter, pos: BlockPos): Float {
        if (!player.hasCorrectToolForDrops(state)) return 0.0F
        return super.getDestroyProgress(state, player, level, pos)
    }

    override fun playerDestroy(level: Level, player: Player, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool)
        tool.hurtAndBreak(MANTLE_MINING_DAMAGE_FACTOR - 1, player, EquipmentSlot.MAINHAND)
    }
}
