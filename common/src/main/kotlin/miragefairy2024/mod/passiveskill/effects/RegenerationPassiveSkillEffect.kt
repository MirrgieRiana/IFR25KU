package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PER_SECOND_TRANSLATION
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs

object RegenerationPassiveSkillEffect : AbstractDoublePassiveSkillEffect("regeneration") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}" }, "Regeneration", "持続回復")
    override fun getText(value: Double) = text { translation() + ": "() + PER_SECOND_TRANSLATION(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        if (context.player.health < context.player.maxHealth) {
            context.player.heal(newValue.toFloat())
        }
    }

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
    }
}
