package miragefairy2024.mod.enchantment

import miragefairy2024.util.MultiMine
import miragefairy2024.util.get
import mirrg.kotlin.helium.max
import net.minecraft.core.BlockBox
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object AreaMiningMultiMineHandler : MultiMineHandler {
    override fun create(
        miningDirection: Direction,
        level: Level, blockPos: BlockPos, blockState: BlockState,
        miner: Player, toolItem: Item, toolItemStack: ItemStack,
    ): MultiMine? {
        val forwardLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.FORWARD_AREA_MINING.key], toolItemStack)
        val lateralLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.LATERAL_AREA_MINING.key], toolItemStack)
        val backwardLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.BACKWARD_AREA_MINING.key], toolItemStack)
        if (forwardLevel <= 0 && lateralLevel <= 0 && backwardLevel <= 0) return null
        return object : MultiMine(level, blockPos, blockState, miner, toolItem, toolItemStack) {
            override fun visit(visitor: Visitor): Float {
                var requiredMiningPower = blockState.getDestroySpeed(level, blockPos)
                visitor.visit(
                    listOf(blockPos),
                    miningDamage = 1.0,
                    onMine = { blockPos ->
                        requiredMiningPower = requiredMiningPower max level.getBlockState(blockPos).getDestroySpeed(level, blockPos)
                    },
                    region = run {
                        val l = lateralLevel
                        val f = forwardLevel
                        val b = backwardLevel
                        val (xRange, yRange, zRange) = when (miningDirection) {
                            Direction.DOWN -> Triple(-l..l, -b..f, -l..l)
                            Direction.UP -> Triple(-l..l, -f..b, -l..l)
                            Direction.NORTH -> Triple(-l..l, -l..l, -b..f)
                            Direction.SOUTH -> Triple(-l..l, -l..l, -f..b)
                            Direction.WEST -> Triple(-b..f, -l..l, -l..l)
                            Direction.EAST -> Triple(-f..b, -l..l, -l..l)
                        }
                        BlockBox.of(
                            BlockPos(blockPos.x + xRange.first, blockPos.y + yRange.first, blockPos.z + zRange.first),
                            BlockPos(blockPos.x + xRange.last, blockPos.y + yRange.last, blockPos.z + zRange.last),
                        )
                    },
                    canContinue = { _, blockState2 -> toolItem.isCorrectToolForDrops(toolItemStack, blockState2) },
                )
                return requiredMiningPower
            }
        }
    }
}
