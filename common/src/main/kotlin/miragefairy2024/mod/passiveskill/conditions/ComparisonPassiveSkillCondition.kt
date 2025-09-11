package miragefairy2024.mod.passiveskill.conditions

import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import miragefairy2024.util.lightProxy
import miragefairy2024.util.plus
import miragefairy2024.util.text
import mirrg.kotlin.helium.stripTrailingZeros
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.network.chat.Component

class IntComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Int) : PassiveSkillCondition {
    companion object {
        val LIGHT_LEVEL_TERM = Term(Emoji.LIGHT) { context, _, _ -> context.player.level().lightProxy.getLightLevel(context.player.eyeBlockPos) }
        val FOOD_LEVEL_TERM = Term(Emoji.FOOD, 2) { context, _, _ -> context.player.foodData.foodLevel }
        val LEVEL_TERM = Term(Emoji.LEVEL) { context, _, _ -> context.player.experienceLevel }
    }

    class Term(val emoji: Emoji, val unit: Int = 1, val getValue: (context: PassiveSkillContext, level: Double, mana: Double) -> Int)

    private fun format(double: Double) = (double formatAs "%.8f").stripTrailingZeros()
    override val text: Component get() = text { term.emoji() + format(threshold / term.unit.toDouble())() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        val value = term.getValue(context, level, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}

class DoubleComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Double) : PassiveSkillCondition {
    companion object {
        val FAIRY_LEVEL_TERM = Term(Emoji.STAR) { _, level, _ -> level }
        val MANA_TERM = Term(Emoji.MANA) { _, _, mana -> mana }
        val HEALTH_TERM = Term(Emoji.HEART, 2.0) { context, _, _ -> context.player.health.toDouble() }
    }

    class Term(val emoji: Emoji, val unit: Double = 1.0, val getValue: (context: PassiveSkillContext, level: Double, mana: Double) -> Double)

    private fun format(double: Double) = (double formatAs "%.8f").stripTrailingZeros()
    override val text: Component get() = text { term.emoji() + format(threshold / term.unit)() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        val value = term.getValue(context, level, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}
