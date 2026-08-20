package miragefairy2024.mod.mantle

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

/** 破壊時にツールの耐久値へ与えるダメージの倍率なのだ～🌱 */
const val MANTLE_MINING_DAMAGE_FACTOR = 100

/**
 * 適正でないツールでは岩盤と同じく全く掘り進められず、
 * 破壊時にツールの耐久値へ [MANTLE_MINING_DAMAGE_FACTOR] 倍のダメージが入るブロックなのだ～🌱
 */
open class MantleBlock(properties: Properties) : Block(properties) {
    override fun getDestroyProgress(state: BlockState, player: Player, level: BlockGetter, pos: BlockPos): Float {
        if (!player.hasCorrectToolForDrops(state)) return 0.0F
        return super.getDestroyProgress(state, player, level, pos)
    }

    override fun playerDestroy(level: Level, player: Player, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool)
        // 通常の破壊で既に 1 回分のダメージが入っているから、残りの分だけを追加で与えるのだ～🌱
        tool.hurtAndBreak(MANTLE_MINING_DAMAGE_FACTOR - 1, player, EquipmentSlot.MAINHAND)
    }
}
