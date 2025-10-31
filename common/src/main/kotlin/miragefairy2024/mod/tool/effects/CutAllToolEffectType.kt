package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.MultiMine
import miragefairy2024.util.NeighborType
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.isInMagicMining
import miragefairy2024.util.text
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags

fun <T : ToolConfiguration> T.cutAll() = this.merge(CutAllToolEffectType, true)

object CutAllToolEffectType : BooleanToolEffectType<ToolConfiguration>() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.cut_all" }, "Cut down the entire tree", "木全体を伐採")

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
                override fun isValidBaseBlockState() = blockState isIn BlockTags.LOGS
                override fun executeImpl() {
                    val logBlockPosList = mutableListOf<BlockPos>()
                    visit(
                        listOf(pos),
                        configuration.magicMiningDamage,
                        maxDistance = 19,
                        maxCount = 19,
                        neighborType = NeighborType.VERTICES,
                        canContinue = { _, blockState -> blockState isIn BlockTags.LOGS },
                        onMine = { blockPos ->
                            logBlockPosList += blockPos
                        },
                    ).let { if (!it) return }
                    visit(
                        logBlockPosList,
                        configuration.magicMiningDamage * 0.1F,
                        maxDistance = 8,
                        canContinue = { _, blockState -> blockState isIn BlockTags.LEAVES },
                    )
                }
            }.execute()
        }
    }
}
