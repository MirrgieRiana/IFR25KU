package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.empty
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance

object StatusEffectPassiveSkillEffect : AbstractPassiveSkillEffect<StatusEffectPassiveSkillEffect.Value>("status_effect") {
    class Value(val map: Map<Holder<MobEffect>, Entry>)
    class Entry(val level: Int, val additionalSeconds: Int)

    override fun getText(value: Value) = getTexts(value).join(text { ","() })
    override fun getTexts(value: Value): List<Component> {
        return value.map.entries.sortedBy { (it.key as? Holder.Reference<*>)?.key()?.location() }.map { (statusEffect, entry) ->
            text { Emoji.POTION() + " "() + statusEffect.value().displayName + if (entry.level >= 2) " "() + entry.level.toRomanText() else empty() }
        }
    }

    override val unit = Value(mapOf())
    override fun castOrNull(value: Any?) = value as? Value
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (statusEffect, bEntry) ->
            val aEntry = map[statusEffect]
            if (aEntry == null || aEntry.level < bEntry.level || aEntry.additionalSeconds < bEntry.additionalSeconds) {
                map[statusEffect] = bEntry
            }
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) {
        newValue.map.forEach { (statusEffect, entry) ->
            context.player.addEffect(MobEffectInstance(statusEffect, 20 * (1 + 1 + entry.additionalSeconds), entry.level - 1, true, false, true))
        }
    }
}
