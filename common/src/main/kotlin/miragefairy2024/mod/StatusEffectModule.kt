package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.en
import miragefairy2024.util.isServer
import miragefairy2024.util.ja
import miragefairy2024.util.register
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

val experienceStatusEffect = Registration(BuiltInRegistries.MOB_EFFECT, MirageFairy2024.identifier("experience")) { ExperienceStatusEffect() }

context(ModContext)
fun initStatusEffectModule() {
    experienceStatusEffect.register()
    en { experienceStatusEffect().descriptionId to "Experience" }
    ja { experienceStatusEffect().descriptionId to "経験値獲得" }
}

class ExperienceStatusEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0x2FFF00) {
    override fun shouldApplyEffectTickThisTick(duration: Int, amplifier: Int) = true
    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        super.applyEffectTick(entity, amplifier)
        val level = entity.level()
        if (level.gameTime % 5 != 0L) return true
        if (level.isServer && entity is Player) {
            entity.giveExperiencePoints(1 + amplifier)
            level.playSound(null, entity.x, entity.y, entity.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.35F + 0.9F)
        }
        return true
    }
}
