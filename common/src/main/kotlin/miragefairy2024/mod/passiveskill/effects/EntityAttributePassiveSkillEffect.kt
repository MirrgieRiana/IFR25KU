package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.Holder
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier

object EntityAttributePassiveSkillEffect : AbstractPassiveSkillEffect<EntityAttributePassiveSkillEffect.Value>("entity_attribute") {
    val FORMATTERS = mutableMapOf<Holder<Attribute>, (Double) -> String>()

    private val defaultFormatter: (Double) -> String = { it formatAs "%+.2f" }
    private val ATTRIBUTE_MODIFIER_IDENTIFIER = MirageFairy2024.identifier("passive_skill")

    class Value(val map: Map<Holder<Attribute>, Double>)

    override fun getText(value: Value) = getTexts(value).join(text { ","() })
    override fun getTexts(value: Value): List<Component> {
        return value.map.entries.sortedBy { (it.key as? Holder.Reference<*>)?.key()?.location() }.map { (attribute, value) ->
            text { Emoji.HUMAN() + " "() + translate(attribute.value().descriptionId) + " ${FORMATTERS.getOrElse(attribute) { defaultFormatter }(value)}"() }
        }
    }

    override val unit = Value(mapOf())
    override fun castOrNull(value: Any?) = value as? Value
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (attribute, value) ->
            map[attribute] = map.getOrElse(attribute) { 0.0 } + value
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) {

        // 削除するべきものを削除
        oldValue.map.forEach { (attribute, _) ->
            val customInstance = context.player.attributes.getInstance(attribute) ?: return@forEach
            if (attribute !in newValue.map) {
                customInstance.removeModifier(ATTRIBUTE_MODIFIER_IDENTIFIER)
            }
        }

        // 追加および変更
        newValue.map.forEach { (attribute, value) ->
            val customInstance = context.player.attributes.getInstance(attribute) ?: return@forEach
            val oldModifier = customInstance.getModifier(ATTRIBUTE_MODIFIER_IDENTIFIER)
            if (oldModifier == null) {
                val modifier = AttributeModifier(ATTRIBUTE_MODIFIER_IDENTIFIER, value, AttributeModifier.Operation.ADD_VALUE)
                customInstance.addTransientModifier(modifier)
            } else if (oldModifier.amount != value) {
                customInstance.removeModifier(ATTRIBUTE_MODIFIER_IDENTIFIER)
                val modifier = AttributeModifier(ATTRIBUTE_MODIFIER_IDENTIFIER, value, AttributeModifier.Operation.ADD_VALUE)
                customInstance.addTransientModifier(modifier)
            }
        }

    }
}
