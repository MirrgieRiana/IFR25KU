package miragefairy2024.mod.common

import miragefairy2024.util.SubscribableBuffer
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

object CommonRenderingEvents {
    val onRenderBlockPosesOutline = SubscribableBuffer<RenderBlockPosesOutlineListener>()
}

fun interface RenderBlockPosesOutlineListener {
    fun getBlockPoses(context: RenderBlockPosesOutlineContext): Pair<BlockPos, Set<BlockPos>>?
}

fun interface RenderBlockPosesOutlineListenerItem {
    fun getBlockPoses(hand: InteractionHand, context: RenderBlockPosesOutlineContext): Pair<BlockPos, Set<BlockPos>>?
}

interface RenderBlockPosesOutlineContext {
    val level: Level
    val player: Player
    val hitResult: HitResult?
}
