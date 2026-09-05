package miragefairy2024.mod.tree.contents

import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.randomInt
import miragefairy2024.util.with
import mirrg.kotlin.helium.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

@Suppress("OVERRIDE_DEPRECATION")
abstract class DrippingLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    protected abstract fun getIncisedLogBlock(): Block
    protected abstract fun getSapItem(): Item
    protected abstract fun getRosinItem(): Item

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = state[FACING]

        // 消費
        level.setBlock(pos, getIncisedLogBlock().defaultBlockState().with(FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)

        fun drop(item: Item, count: Double) {
            val actualCount = level.random.randomInt(count) atMost item.defaultMaxStackSize
            if (actualCount <= 0) return
            val itemStack = item.createItemStack(actualCount)
            val itemEntity = ItemEntity(level, pos.x + 0.5 + direction.stepX * 0.65, pos.y + 0.1, pos.z + 0.5 + direction.stepZ * 0.65, itemStack)
            itemEntity.setDeltaMovement(0.05 * direction.stepX + level.random.nextDouble() * 0.02, 0.05, 0.05 * direction.stepZ + level.random.nextDouble() * 0.02)
            level.addFreshEntity(itemEntity)
        }

        // 生産
        val fortune = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, Enchantments.FORTUNE], stack)
        drop(getSapItem(), 1.0 + 0.25 * fortune) // 樹液
        drop(getRosinItem(), 0.03 + 0.01 * fortune) // 涙

        // エフェクト
        level.playSound(null, pos, SoundEvents.SLIME_JUMP, SoundSource.BLOCKS, 0.75F, 1.0F + 0.5F * level.random.nextFloat())

        return ItemInteractionResult.CONSUME
    }
}
