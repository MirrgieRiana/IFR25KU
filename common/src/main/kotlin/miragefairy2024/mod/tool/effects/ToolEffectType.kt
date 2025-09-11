package miragefairy2024.mod.tool.effects

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import miragefairy2024.ModContext
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import mirrg.kotlin.helium.max
import net.minecraft.core.Direction
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import java.util.UUID

val breakDirectionCache = mutableMapOf<UUID, Direction>()

context(ModContext)
fun initToolEffectType() {
    AreaMiningToolEffectType.init()
    MineAllToolEffectType.init()
    CutAllToolEffectType.init()
    SelfMendingToolEffectType.init()
    ObtainFairyToolEffectType.init()
    CollectionToolEffectType.init()
    SoulStreamContainableToolEffectType.init()
    TillingRecipeHoeToolEffectType.init()
    EffectiveToolEffectType.init()

    BlockEvent.BREAK.register { level, pos, state, player, xp ->
        if (level.isClientSide) return@register EventResult.pass()
        val direction = run {
            val d = player.blockInteractionRange() max player.entityInteractionRange()
            val hitResult = player.pick(d, 0F, false)
            if (hitResult.type != HitResult.Type.BLOCK) {
                null
            } else {
                (hitResult as BlockHitResult).direction
            }
        }
        if (direction == null) {
            breakDirectionCache.remove(player.uuid)
        } else {
            breakDirectionCache[player.uuid] = direction
        }
        EventResult.pass()
    }
}

abstract class BooleanToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Boolean> {
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun merge(a: Boolean, b: Boolean) = a || b
}

abstract class IntAddToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Int> {
    override fun castOrThrow(value: Any?) = value as Int
    override fun merge(a: Int, b: Int) = a + b
}

abstract class IntMaxToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Int> {
    override fun castOrThrow(value: Any?) = value as Int
    override fun merge(a: Int, b: Int) = a max b
}

abstract class DoubleAddToolEffectType<in C : ToolConfiguration> : ToolEffectType<C, Double> {
    override fun castOrThrow(value: Any?) = value as Double
    override fun merge(a: Double, b: Double) = a + b
}
