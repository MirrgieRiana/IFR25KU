package miragefairy2024.mod.enchantment

import miragefairy2024.util.MultiMine
import miragefairy2024.util.NeighborType
import miragefairy2024.util.get
import miragefairy2024.util.isIn
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object CutAllMultiMineHandler : MultiMineHandler {
    override fun create(
        miningDirection: Direction,
        level: Level, blockPos: BlockPos, blockState: BlockState,
        miner: Player, toolItem: Item, toolItemStack: ItemStack,
    ): MultiMine? {
        val cutAllLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.CUT_ALL.key], toolItemStack)
        if (cutAllLevel <= 0) return null
        return object : MultiMine(level, blockPos, blockState, miner, toolItem, toolItemStack) {
            override fun isValidBaseBlockState() = blockState isIn BlockTags.LOGS
            override fun visit(visitor: Visitor): Float {
                val logBlockPosList = mutableListOf<BlockPos>()
                visitor.visit(
                    listOf(blockPos),
                    miningDamage = 1.0,
                    maxDistance = 19,
                    maxCount = 19,
                    neighborType = NeighborType.VERTICES,
                    canContinue = { _, blockState2 -> blockState2 isIn BlockTags.LOGS },
                    onMine = { blockPos ->
                        logBlockPosList += blockPos
                    },
                ).let { if (!it) return blockState.getDestroySpeed(level, blockPos) }
                visitor.visit(
                    logBlockPosList,
                    miningDamage = 0.1,
                    maxDistance = 8,
                    canContinue = { _, blockState -> blockState isIn BlockTags.LEAVES },
                )
                return blockState.getDestroySpeed(level, blockPos)
            }
        }
    }
}
