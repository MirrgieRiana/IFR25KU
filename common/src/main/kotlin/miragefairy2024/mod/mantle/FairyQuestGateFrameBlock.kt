package miragefairy2024.mod.mantle

import com.mojang.serialization.MapCodec
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotIn
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

/** ゲートの内側として認められる、最小の幅なのだ～🌱 */
private const val MIN_WIDTH = 2

/** ゲートの内側として認められる、最大の幅なのだ～🌱 */
private const val MAX_WIDTH = 21

/** ゲートの内側として認められる、最小の高さなのだ～🌱 */
private const val MIN_HEIGHT = 3

/** ゲートの内側として認められる、最大の高さなのだ～🌱 */
private const val MAX_HEIGHT = 21

/**
 * フェアリークエストゲートの枠となるブロックなのだ～🌱
 *
 * ネザーポータルの黒曜石と同じく、枠を組んで火を点けると、内側にポータルが現れるのだ～🌱
 */
class FairyQuestGateFrameBlock(properties: Properties) : MantleBlock(properties) {
    companion object {
        val CODEC: MapCodec<FairyQuestGateFrameBlock> = simpleCodec(::FairyQuestGateFrameBlock)
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (stack isNotIn Items.FLINT_AND_STEEL && stack isNotIn Items.FIRE_CHARGE) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)

        val insideBlockPos = pos.relative(hitResult.direction)
        if (!tryLightFairyQuestGate(level, insideBlockPos)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION

        level.playSound(player, insideBlockPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F)
        if (!level.isClientSide) {
            if (stack isIn Items.FLINT_AND_STEEL) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
            } else {
                stack.consume(1, player)
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide)
    }
}

/**
 * [insideBlockPos] を含む、枠に囲まれた矩形を探して、ポータルで満たすのだ～🌱
 *
 * 枠の形は、ネザーポータルと共通なのだ～🌱
 */
fun tryLightFairyQuestGate(level: LevelAccessor, insideBlockPos: BlockPos): Boolean {
    listOf(Direction.Axis.X, Direction.Axis.Z).forEach { axis ->
        val shape = findFairyQuestGateShape(level, insideBlockPos, axis) ?: return@forEach
        val portalBlockState = MantleBlockCard.FAIRY_QUEST_GATE_PORTAL.block().defaultBlockState().setValue(FairyQuestGatePortalBlock.AXIS, axis)
        val widthDirection = if (axis == Direction.Axis.X) Direction.EAST else Direction.SOUTH
        (0 until shape.width).forEach { dw ->
            (0 until shape.height).forEach { dy ->
                level.setBlock(shape.originBlockPos.relative(widthDirection, dw).above(dy), portalBlockState, Block.UPDATE_ALL)
            }
        }
        return true
    }
    return false
}

/** 枠に囲まれた矩形の、左下の角と、その大きさなのだ～🌱 */
private class FairyQuestGateShape(val originBlockPos: BlockPos, val width: Int, val height: Int)

private fun findFairyQuestGateShape(level: LevelAccessor, insideBlockPos: BlockPos, axis: Direction.Axis): FairyQuestGateShape? {
    val widthDirection = if (axis == Direction.Axis.X) Direction.EAST else Direction.SOUTH

    fun isFrame(blockPos: BlockPos) = level.getBlockState(blockPos) isIn MantleBlockCard.FAIRY_QUEST_GATE.block()
    fun isEmpty(blockPos: BlockPos) = level.getBlockState(blockPos).isAir

    if (!isEmpty(insideBlockPos)) return null

    // 足元の枠まで降りるのだ～🌱
    var bottomBlockPos = insideBlockPos
    while (isEmpty(bottomBlockPos.below())) {
        if (insideBlockPos.y - bottomBlockPos.y >= MAX_HEIGHT) return null
        bottomBlockPos = bottomBlockPos.below()
    }
    if (!isFrame(bottomBlockPos.below())) return null

    // 左端の枠まで進むのだ～🌱
    var originBlockPos = bottomBlockPos
    while (isEmpty(originBlockPos.relative(widthDirection.opposite))) {
        if (bottomBlockPos.distManhattan(originBlockPos) >= MAX_WIDTH) return null
        originBlockPos = originBlockPos.relative(widthDirection.opposite)
    }
    if (!isFrame(originBlockPos.relative(widthDirection.opposite))) return null

    // 幅を測るのだ～🌱
    var width = 0
    while (width <= MAX_WIDTH && isEmpty(originBlockPos.relative(widthDirection, width))) width++
    if (width < MIN_WIDTH || width > MAX_WIDTH) return null
    if (!isFrame(originBlockPos.relative(widthDirection, width))) return null

    // 高さを測るのだ～🌱 底辺の枠も一緒に確かめるのだ～🌱
    if (!(0 until width).all { isFrame(originBlockPos.relative(widthDirection, it).below()) }) return null
    var height = 0
    while (height <= MAX_HEIGHT && (0 until width).all { isEmpty(originBlockPos.relative(widthDirection, it).above(height)) }) height++
    if (height < MIN_HEIGHT || height > MAX_HEIGHT) return null
    if (!(0 until width).all { isFrame(originBlockPos.relative(widthDirection, it).above(height)) }) return null

    // 左右の枠を確かめるのだ～🌱
    if (!(0 until height).all { isFrame(originBlockPos.above(it).relative(widthDirection.opposite)) }) return null
    if (!(0 until height).all { isFrame(originBlockPos.above(it).relative(widthDirection, width)) }) return null

    return FairyQuestGateShape(originBlockPos, width, height)
}
