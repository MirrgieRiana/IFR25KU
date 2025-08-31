package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PER_SECOND_TRANSLATION
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs

object HungerPassiveSkillEffect : AbstractDoublePassiveSkillEffect("hunger") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}" }, "Hunger", "空腹")
    override fun getText(value: Double) = text { translation() + ": "() + PER_SECOND_TRANSLATION(-value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        context.player.causeFoodExhaustion(newValue.toFloat() * 4F)
    }

    override fun getFilters(samples: List<Double>): List<PassiveSkillEffectFilter<Double>> = listOf(PassiveSkillEffectFilter(this, identifier, text { translation() }) { true })

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
    }
}
