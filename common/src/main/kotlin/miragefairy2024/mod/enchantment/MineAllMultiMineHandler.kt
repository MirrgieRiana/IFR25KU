package miragefairy2024.mod.enchantment

import miragefairy2024.util.MultiMine
import miragefairy2024.util.get
import miragefairy2024.util.isIn
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
        if (mineAllLevel <= 0) return null
        return object : MultiMine(level, blockPos, blockState, miner, toolItem, toolItemStack) {
            override fun isValidBaseBlockState() = blockState isIn ConventionalBlockTags.ORES
            override fun visit(visitor: Visitor): Float {
                visitor.visit(
                    listOf(blockPos),
                    miningDamage = 1.0,
                    maxDistance = 19,
                    maxCount = 31,
                    canContinue = { _, blockState2 -> blockState2.block === blockState.block },
                )
                return blockState.getDestroySpeed(level, blockPos)
            }
        }
    }
}
