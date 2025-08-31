package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.passiveskill.conditions.ItemFoodIngredientPassiveSkillCondition
import miragefairy2024.mod.passiveskill.conditions.SimplePassiveSkillConditionCard
import miragefairy2024.mod.passiveskill.effects.CollectionPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ElementPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.EntityAttributePassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ExperiencePassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.HungerPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.IgnitionPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.MendingPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.MiningSpeedPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.RegenerationPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.StatusEffectPassiveSkillEffect
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.ai.attributes.Attributes

val passiveSkillEffectRegistryKey: ResourceKey<Registry<PassiveSkillEffect<*>>> = ResourceKey.createRegistryKey(MirageFairy2024.identifier("passive_skill_effect"))
val passiveSkillEffectRegistry: Registry<PassiveSkillEffect<*>> = FabricRegistryBuilder.createSimple(passiveSkillEffectRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

val PER_SECOND_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill.per_second" }, "%s/s", "%s/秒")

context(ModContext)
fun initPassiveSkillModule() {

    // Condition

    SimplePassiveSkillConditionCard.entries.forEach { card ->
        card.init()
    }
    ItemFoodIngredientPassiveSkillCondition.translation.enJa()


    // Effect

    PER_SECOND_TRANSLATION.enJa()

    listOf(
        ManaBoostPassiveSkillEffect,
        EntityAttributePassiveSkillEffect,
        StatusEffectPassiveSkillEffect,
        IgnitionPassiveSkillEffect,
        ExperiencePassiveSkillEffect,
        RegenerationPassiveSkillEffect,
        HungerPassiveSkillEffect,
        MendingPassiveSkillEffect,
        CollectionPassiveSkillEffect,
        ElementPassiveSkillEffect,
        MiningSpeedPassiveSkillEffect,
    ).forEach { card ->
        card.init()
    }

    ModEvents.onInitialize {
        EntityAttributePassiveSkillEffect.FORMATTERS[Attributes.MOVEMENT_SPEED] = { (it / 0.1) * 100 formatAs "%+.0f%%" }
    }


    // Execution

    initPassiveSkillExecution()

}
