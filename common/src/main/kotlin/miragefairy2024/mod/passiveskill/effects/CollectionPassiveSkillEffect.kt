package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PER_SECOND_TRANSLATION
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.collectItem
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs

object CollectionPassiveSkillEffect : AbstractDoublePassiveSkillEffect("collection") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}" }, "Collection", "収集")
    override fun getText(value: Double) = text { translation() + ": "() + PER_SECOND_TRANSLATION(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        val world = context.world
        val actualAmount = world.random.randomInt(newValue)
        if (actualAmount <= 0) return
        val player = context.player
        collectItem(world, player.eyeBlockPos, reach = 15, maxCount = actualAmount, predicate = { !it.boundingBox.intersects(player.boundingBox) }) { // 既に触れているアイテムには無反応
            it.teleportTo(player.x, player.y, player.z)
            it.setNoPickUpDelay()
            true
        }
    }

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
    }
}
