package miragefairy2024.mod.enchantment

import miragefairy2024.util.MultiMine
import miragefairy2024.util.NeighborType
import miragefairy2024.util.get
import miragefairy2024.util.isIn
import mirrg.kotlin.helium.min
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
        val accelerationLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.AREA_MINING_ACCELERATION.key], toolItemStack)
        if (cutAllLevel <= 0) return null
        return object : MultiMine(level, blockPos, blockState, miner, toolItem, toolItemStack) {
            override fun isValidBaseBlockState() = blockState isIn BlockTags.LOGS
            override fun visit(visitor: Visitor): Float {
                val maxCount = if (accelerationLevel <= 30) 1 shl accelerationLevel else Int.MAX_VALUE
                var count = 1
                var sumHardness = blockState.getDestroySpeed(level, blockPos)
                val logBlockPosList = mutableListOf<BlockPos>()
                visitor.visit(
                    listOf(blockPos),
                    miningDamage = 1.0,
                    maxDistance = 19,
                    maxCount = 19,
                    neighborType = NeighborType.VERTICES,
                    canContinue = { _, blockState2 -> blockState2 isIn BlockTags.LOGS },
                    onMine = { blockPos ->
                        count++
                        sumHardness += level.getBlockState(blockPos).getDestroySpeed(level, blockPos)
                        logBlockPosList += blockPos
                    },
                ).let { if (!it) return sumHardness / (maxCount min count) }
                visitor.visit(
                    logBlockPosList,
                    miningDamage = 0.1,
                    maxDistance = 8,
                    onMine = {
                        count++
                        sumHardness += level.getBlockState(blockPos).getDestroySpeed(level, blockPos)
                    },
                    canContinue = { _, blockState -> blockState isIn BlockTags.LEAVES },
                )
                return sumHardness / (maxCount min count)
            }
        }
    }
}
