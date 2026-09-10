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
    fun getBlockPoses(context: RenderBlockPosesOutlineContext): BlockPosesOutline?
}

fun interface RenderBlockPosesOutlineListenerItem {
    fun getBlockPoses(hand: InteractionHand, context: RenderBlockPosesOutlineContext): BlockPosesOutline?
}

/**
 * @param baseBlockPos 枠全体の明るさを決めるために参照する位置なのだ～🌱
 * @param rgb 枠の色なのだ～🌱 実際に描かれる色は、これに明るさを掛けたものになるのだ～🌱
 */
class BlockPosesOutline(val baseBlockPos: BlockPos, val blockPoses: Set<BlockPos>, val rgb: Int)

interface RenderBlockPosesOutlineContext {
    val level: Level
    val player: Player
    val hitResult: HitResult?
}
