package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.ClientPlayerEvent
import miragefairy2024.mixins.api.LevelEvent
import miragefairy2024.mod.enchantment.MultiMineHandler
import miragefairy2024.util.MultiMine
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

private class MiningAreaResult(val miningArea: MultiMine.MiningArea?)

private var expectedMiningAreaResult: MiningAreaResult? = null

context(ModContext)
fun initEnchantmentClientModule() {

    // 採掘範囲が変わったら採掘をキャンセル
    ClientPlayerEvent.SAME_DESTROY_TARGET.register { pos ->
        val minecraft = Minecraft.getInstance() ?: return@register true
        val level = minecraft.level ?: return@register true
        val player = minecraft.player ?: return@register true
        if (expectedMiningAreaResult == null) return@register true

        val miningArea = run {
            val hitResult = minecraft.hitResult ?: return@run null
            if (hitResult.type != HitResult.Type.BLOCK) return@run null // なぜかブロックをタゲっていない
            hitResult as BlockHitResult
            val multiMine = run {
                MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                    it.create(
                        hitResult.direction,
                        level, pos, level.getBlockState(pos),
                        player, player.mainHandItem.item, player.mainHandItem,
                    )
                }
            } ?: return@run null // 範囲採掘の能力がない
            val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
            miningArea
        }
        val actualMiningAreaResult = MiningAreaResult(miningArea)
        actualMiningAreaResult.miningArea?.hardness == expectedMiningAreaResult!!.miningArea?.hardness
    }

    // 操作プレイヤーの採掘の開始時と終了時に採掘範囲を再計算
    LevelEvent.DESTROY_BLOCK_PROGRESS.register { level, breakerId, pos, progress ->
        val player = level.getEntity(breakerId) as? Player ?: return@register // 破壊者がゾンビとか
        val minecraft = Minecraft.getInstance() ?: return@register
        val clientPlayer = minecraft.player ?: return@register
        if (player != clientPlayer) return@register // 他プレイヤーは無視
        if (progress != -1) return@register // 採掘が進行中

        val miningArea = run {
            val hitResult = minecraft.hitResult ?: return@run null
            if (hitResult.type != HitResult.Type.BLOCK) return@run null // なぜかブロックをタゲっていない
            hitResult as BlockHitResult
            val multiMine = run {
                MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                    it.create(
                        hitResult.direction,
                        level, pos, level.getBlockState(pos),
                        player, player.mainHandItem.item, player.mainHandItem,
                    )
                }
            } ?: return@run null // 範囲採掘の能力がない
            val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
            miningArea
        }
        expectedMiningAreaResult = MiningAreaResult(miningArea)
    }

}
