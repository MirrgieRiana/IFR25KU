package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.MultiMine
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.isInMagicMining
import miragefairy2024.util.text
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.server.level.ServerPlayer

fun <T : ToolConfiguration> T.mineAll() = this.merge(MineAllToolEffectType, true)

object MineAllToolEffectType : BooleanToolEffectType<ToolConfiguration>() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    override fun apply(configuration: ToolConfiguration, value: Boolean) {
        if (!value) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (world.isClientSide) return@fail
            if (miner !is ServerPlayer) return@fail
            if (isInMagicMining.get()) return@fail
            object : MultiMine(world, pos, state, miner, item, stack) {
                override fun isValidBaseBlockState() = blockState isIn ConventionalBlockTags.ORES
                override fun executeImpl() {
                    visit(
                        listOf(pos),
                        configuration.magicMiningDamage,
                        maxDistance = 19,
                        maxCount = 31,
                        canContinue = { _, blockState -> blockState.block === state.block },
                    )
                }
            }.execute()
        }
    }
}
