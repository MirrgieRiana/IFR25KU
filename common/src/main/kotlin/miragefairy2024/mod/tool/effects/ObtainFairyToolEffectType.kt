package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.enchantment.contents.withStickyMining
import miragefairy2024.mod.fairy.FairyDreamRecipes
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.fairyHistoryContainer
import miragefairy2024.mod.fairy.getRandomFairy
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.mutate
import miragefairy2024.util.text
import miragefairy2024.util.toBox
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity

fun <T : ToolConfiguration> T.obtainFairy(appearanceRateBonus: Double) = this.merge(ObtainFairyToolEffectType, appearanceRateBonus)

object ObtainFairyToolEffectType : DoubleAddToolEffectType<ToolConfiguration>() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.obtain_fairy" }, "Obtain a fairy when mined or killed", "採掘・撃破時に妖精を入手")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    override fun apply(configuration: ToolConfiguration, value: Double) {
        if (value <= 0.0) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.onAfterBreakBlockListeners += fail@{ _, level, player, pos, state, _, tool ->
            if (player !is ServerPlayer) return@fail // 使用者がプレイヤーでない

            // モチーフの判定
            val motifSet = FairyDreamRecipes.BLOCK.test(state.block)

            // 抽選
            val result = getRandomFairy(level.random, motifSet, value) ?: return@fail

            // 入手
            val fairyItemStack = result.motif.createFairyItemStack(condensation = result.condensation, count = result.count)
            withStickyMining(level, pos.toBox(), player, tool) {
                level.addFreshEntity(ItemEntity(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, fairyItemStack))
            }

            // 妖精召喚履歴に追加
            player.fairyHistoryContainer.mutate { it[result.motif] += result.condensation * result.count.toBigInteger() }

        }
        configuration.onKilledListeners += fail@{ _, entity, attacker, _ ->
            if (attacker !is ServerPlayer) return@fail // 使用者がプレイヤーでない

            // モチーフの判定
            val motifSet = FairyDreamRecipes.ENTITY_TYPE.test(entity.type)

            // 抽選
            val result = getRandomFairy(entity.level().random, motifSet, value) ?: return@fail

            // 入手
            val fairyItemStack = result.motif.createFairyItemStack(condensation = result.condensation, count = result.count)
            entity.level().addFreshEntity(ItemEntity(entity.level(), entity.x, entity.y, entity.z, fairyItemStack))

            // 妖精召喚履歴に追加
            attacker.fairyHistoryContainer.mutate { it[result.motif] += result.condensation * result.count.toBigInteger() }

        }
    }
}
