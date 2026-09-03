package miragefairy2024.mod.tree.contents

import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.util.get
import miragefairy2024.util.isNotIn
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.BlockHitResult

@Suppress("OVERRIDE_DEPRECATION")
abstract class IncisableLogBlock(settings: Properties) : RotatedPillarBlock(settings) {
    protected abstract fun getIncisedLog(): TreeBlockCard

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (state[AXIS] != Direction.Axis.Y) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 縦方向でなければスルー
        if (stack isNotIn ItemTags.SWORDS) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 剣でなければスルー
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = if (hitResult.direction.axis === Direction.Axis.Y) player.direction.opposite else hitResult.direction

        // 加工
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
        level.setBlock(pos, getIncisedLog().block().defaultBlockState().with(HorizontalDirectionalBlock.FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)
        level.gameEvent(player, GameEvent.SHEAR, pos)
        player.awardStat(Stats.ITEM_USED.get(stack.item))

        // エフェクト
        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F)

        return ItemInteractionResult.CONSUME
    }
}
