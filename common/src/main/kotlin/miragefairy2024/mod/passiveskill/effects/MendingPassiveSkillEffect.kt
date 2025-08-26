package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.darkGray
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.repair
import miragefairy2024.util.text
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.network.chat.Component
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object MendingPassiveSkillEffect : AbstractPassiveSkillEffect<MendingPassiveSkillEffect.Value>("mending") {
    class Value(val map: Map<TagKey<Item>, Double>)

    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}" }, "Durability Regeneration: %s/s", "耐久値自然回復: %s/秒")
    override fun getText(value: Value) = getTexts(value).join(text { ","() })
    override fun getTexts(value: Value): List<Component> {
        val player = clientProxy?.getClientPlayer()
        return value.map.entries.sortedBy { it.key.location() }.map { (tag, value) ->
            val ok = if (player != null) {
                var ok = false
                fun f(itemStack: ItemStack) {
                    if (itemStack.`is`(tag)) {
                        ok = true
                    }
                }
                f(player.mainHandItem)
                f(player.offhandItem)
                player.armorSlots.forEach {
                    f(it)
                }
                ok
            } else {
                true
            }
            text { (translation(value formatAs "%+.3f") + " ("() + translate(tag.translationKey) + ")"()).let { if (ok) it else it.darkGray } }
        }
    }

    override val unit = Value(mapOf())
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (tag, value) ->
            map[tag] = map.getOrElse(tag) { 0.0 } + value
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) {
        newValue.map.forEach { (tag, value) ->
            if (value <= 0.0) return@forEach
            fun f(itemStack: ItemStack) {
                if (itemStack.`is`(tag)) {
                    itemStack.repair(context.world.random.randomInt(value))
                }
            }
            f(context.player.mainHandItem)
            f(context.player.offhandItem)
            context.player.armorSlots.forEach {
                f(it)
            }
        }
    }

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
    }
}
