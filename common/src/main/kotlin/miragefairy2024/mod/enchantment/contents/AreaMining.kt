package miragefairy2024.mod.enchantment.contents

import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mixins.api.LevelEvent
import miragefairy2024.mod.CommonRenderingEvents
import miragefairy2024.mod.enchantment.AreaMiningMultiMineHandler
import miragefairy2024.mod.enchantment.CutAllMultiMineHandler
import miragefairy2024.mod.enchantment.MineAllMultiMineHandler
import miragefairy2024.mod.enchantment.MultiMineHandler
import miragefairy2024.util.isInMagicMining
import miragefairy2024.util.serverSideOrNull
import mirrg.kotlin.helium.max
import net.minecraft.core.Direction
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

context(ModContext)
fun initAreaMining() {

    // 採掘範囲オーバーレイ
    CommonRenderingEvents.onRenderBlockPosesOutline.add { context ->
        val level = context.level ?: return@add null
        val player = context.player ?: return@add null
        val hitResult = context.hitResult ?: return@add null

        if (hitResult.type != HitResult.Type.BLOCK) return@add null // ブロックをタゲっていない
        hitResult as BlockHitResult

        val miningArea = run {
            val multiMine = run {
                MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                    it.create(
                        hitResult.direction,
                        level, hitResult.blockPos, level.getBlockState(hitResult.blockPos),
                        player, player.mainHandItem.item, player.mainHandItem,
                    )
                }
            } ?: return@run null // 範囲採掘の能力がない
            val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
            miningArea
        } ?: return@add null // 範囲採掘が発動しなかった

        Pair(
            hitResult.blockPos.relative(hitResult.direction),
            miningArea.visitedBlockEntry.map { it.blockPos }.toSet() + setOf(miningArea.multiMine.blockPos),
        )
    }

    // 両サイドにおいて、採掘の際に採掘速度を上書き
    BlockCallback.OVERRIDE_DESTROY_SPEED.register { state, player, _, pos, f ->
        val miningArea = run {
            val miningDirection = calculateMiningDirection(player) ?: return@run null // なぜかブロックをタゲっていない
            val multiMine = run {
                MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                    it.create(
                        miningDirection,
                        player.level(), pos, state,
                        player, player.mainHandItem.item, player.mainHandItem,
                    )
                }
            } ?: return@run null // 範囲採掘の能力がない
            val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
            miningArea
        } ?: return@register f // 範囲採掘が発動しなかった
        miningArea.hardness
    }

    // サーバーサイドにおいて、最後にプレイヤーがブロックを採掘した際の向きを記憶
    LevelEvent.HANDLE_PLAYER_ACTION.register { listener, packet ->
        latestPlayerMiningDirectionCache[listener.player.id] = Pair(listener.player.level().gameTime, packet.direction)
    }

    // サーバーサイドにおいて、ブロック破壊後に範囲採掘の効果
    BlockCallback.AFTER_BREAK.register { world, player, pos, state, _, _ ->
        val serverSide = world.serverSideOrNull ?: return@register
        if (isInMagicMining.get()) return@register

        val miningDirectionCache = latestPlayerMiningDirectionCache[player.id] ?: return@register // なぜか向きが記録されていない
        if (miningDirectionCache.first != world.gameTime) return@register // なぜか向きが記録されていない
        val miningArea = run {
            val multiMine = run {
                MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                    it.create(
                        miningDirectionCache.second,
                        world, pos, state,
                        player, player.mainHandItem.item, player.mainHandItem,
                    )
                }
            } ?: return@run null // 範囲採掘の能力がない
            val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
            miningArea
        } ?: return@register // 範囲採掘が発動しなかった

        miningArea.multiMine.execute(serverSide)
    }

    MultiMineHandler.REGISTRY += AreaMiningMultiMineHandler
    MultiMineHandler.REGISTRY += CutAllMultiMineHandler
    MultiMineHandler.REGISTRY += MineAllMultiMineHandler

}

private val latestPlayerMiningDirectionCache = mutableMapOf<Int, Pair<Long, Direction>>()

private fun calculateMiningDirection(player: Player): Direction? {
    val d = player.blockInteractionRange() max player.entityInteractionRange()
    val hitResult = player.pick(d, 0F, false)
    if (hitResult.type != HitResult.Type.BLOCK) return null
    hitResult as BlockHitResult
    return hitResult.direction
}
