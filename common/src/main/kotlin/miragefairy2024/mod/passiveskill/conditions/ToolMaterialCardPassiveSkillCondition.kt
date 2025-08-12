package miragefairy2024.mod.passiveskill.conditions

import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.text
import miragefairy2024.util.translate
import net.minecraft.network.chat.Component

class ToolMaterialCardPassiveSkillCondition(private val card: ToolMaterialCard) : PassiveSkillCondition {
    override val text: Component get() = text { translate(card.tag.translationKey) }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.mainHandItem.`is`(card.tag)
}
