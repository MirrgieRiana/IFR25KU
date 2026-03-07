package miragefairy2024.mod.enchantment

import miragefairy2024.util.MultiMine
import miragefairy2024.util.get
import miragefairy2024.util.isIn
import mirrg.kotlin.helium.min
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object MineAllMultiMineHandler : MultiMineHandler {
    override fun create(
        miningDirection: Direction,
        level: Level, blockPos: BlockPos, blockState: BlockState,
        miner: Player, toolItem: Item, toolItemStack: ItemStack,
    ): MultiMine? {
        val mineAllLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.MINE_ALL.key], toolItemStack)
        val accelerationLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.AREA_MINING_ACCELERATION.key], toolItemStack)
        if (mineAllLevel <= 0) return null
        return object : MultiMine(level, blockPos, blockState, miner, toolItem, toolItemStack) {
            override fun isValidBaseBlockState() = blockState isIn ConventionalBlockTags.ORES
            override fun visit(visitor: Visitor): Float {
                val maxCount = if (accelerationLevel <= 30) 1 shl accelerationLevel else Int.MAX_VALUE
                var count = 1
                var sumHardness = blockState.getDestroySpeed(level, blockPos)
                visitor.visit(
                    listOf(blockPos),
                    miningDamage = 1.0,
                    maxDistance = 19,
                    maxCount = 31,
                    onMine = {
                        count++
                        sumHardness += level.getBlockState(blockPos).getDestroySpeed(level, blockPos)
                    },
                    canContinue = { _, blockState2 -> blockState2.block === blockState.block },
                )
                return sumHardness / (maxCount min count)
            }
        }
    }
}
