package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.passiveskill.PassiveSkillEffectFilter
import miragefairy2024.util.Translation
import miragefairy2024.util.empty
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atLeast

object IgnitionPassiveSkillEffect : AbstractBooleanPassiveSkillEffect("ignition") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}" }, "Ignition", "発火")
    override fun getText(value: Boolean) = text { if (value) translation() else empty() }
    override fun update(context: PassiveSkillContext, oldValue: Boolean, newValue: Boolean) {
        if (!newValue) return
        if (context.player.isInWaterRainOrBubble || context.player.isInPowderSnow || context.player.wasInPowderSnow) return
        context.player.remainingFireTicks = 30 atLeast context.player.remainingFireTicks
    }

    override fun getFilters(samples: List<Boolean>): List<PassiveSkillEffectFilter<Boolean>> = listOf(PassiveSkillEffectFilter(this, identifier, text { translation() }) { true })

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
    }
}
