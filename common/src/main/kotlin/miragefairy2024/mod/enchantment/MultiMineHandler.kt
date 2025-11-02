package miragefairy2024.mod.enchantment

import miragefairy2024.util.MultiMine
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

interface MultiMineHandler {
    companion object {
        val REGISTRY = mutableListOf<MultiMineHandler>()
    }

    fun create(
        miningDirection: Direction,
        level: Level, blockPos: BlockPos, blockState: BlockState,
        miner: Player, toolItem: Item, toolItemStack: ItemStack,
    ): MultiMine?
}
