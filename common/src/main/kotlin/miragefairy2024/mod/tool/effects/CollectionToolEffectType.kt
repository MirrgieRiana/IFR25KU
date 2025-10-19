package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.isValid
import miragefairy2024.util.text
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity

fun <T : ToolConfiguration> T.collection() = this.merge(CollectionToolEffectType, true)

object CollectionToolEffectType : BooleanToolEffectType<ToolConfiguration>() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.collection" }, "Collect drop items when killed", "撃破時にドロップ品を回収")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    override fun apply(configuration: ToolConfiguration, value: Boolean) {
        if (!value) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.onKilledListeners += fail@{ _, entity, attacker, _ -> // TODO エンチャントにする
            if (attacker.level() != entity.level()) return@fail
            entity.level().getEntitiesOfClass(ItemEntity::class.java, entity.boundingBox) { it.isValid }.forEach {
                it.teleportTo(attacker.x, attacker.y, attacker.z)
                it.setNoPickUpDelay()
            }
            entity.level().getEntitiesOfClass(ExperienceOrb::class.java, entity.boundingBox) { it.isValid }.forEach {
                it.teleportTo(attacker.x, attacker.y, attacker.z)
            }
        }
    }
}
