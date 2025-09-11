package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.MultiMine
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.max
import net.minecraft.core.BlockBox
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer

fun <T : ToolConfiguration> T.areaMining(horizontal: Int, front: Int, back: Int) = this.merge(AreaMiningToolEffectType, AreaMiningToolEffectType.Value(horizontal, front, back))

object AreaMiningToolEffectType : ToolEffectType<ToolConfiguration, AreaMiningToolEffectType.Value> {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.area_mining" }, "Area mining %s %s %s", "範囲採掘 %s %s %s")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    class Value(val horizontal: Int, val front: Int, val back: Int)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.horizontal max b.horizontal, a.front max b.front, a.back max b.back)

    override fun apply(configuration: ToolConfiguration, value: Value) {
        if (value.horizontal <= 0 && value.front <= 0 && value.back <= 0) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION(value.horizontal, value.front, value.back) })
        configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (world.isClientSide) return@fail
            if (miner !is ServerPlayer) return@fail
            object : MultiMine(world, pos, state, miner, item, stack) {
                override fun executeImpl() {
                    visit(
                        listOf(pos),
                        configuration.magicMiningDamage,
                        region = run {
                            val breakDirection = breakDirectionCache[miner.uuid] ?: return // 向きの判定が不正
                            val h = value.horizontal
                            val f = value.front
                            val b = value.back
                            val (xRange, yRange, zRange) = when (breakDirection) {
                                Direction.DOWN -> Triple(-h..h, -b..f, -h..h)
                                Direction.UP -> Triple(-h..h, -f..b, -h..h)
                                Direction.NORTH -> Triple(-h..h, -h..h, -b..f)
                                Direction.SOUTH -> Triple(-h..h, -h..h, -f..b)
                                Direction.WEST -> Triple(-b..f, -h..h, -h..h)
                                Direction.EAST -> Triple(-f..b, -h..h, -h..h)
                            }
                            BlockBox.of(
                                BlockPos(pos.x + xRange.first, pos.y + yRange.first, pos.z + zRange.first),
                                BlockPos(pos.x + xRange.last, pos.y + yRange.last, pos.z + zRange.last),
                            )
                        },
                        canContinue = { blockPos, blockState -> item.isCorrectToolForDrops(stack, blockState) },
                    )
                }
            }.execute()
        }
    }
}
