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
    private fun getExperienceAmount(time: Int, amplifier: Int): Int {
        // 時間に比例して増える、累積の経験値獲得量なのだ～🌱
        // +10で四捨五入することで、獲得の位置が各回の持ち時間の中央に来るのだ～🌱
        // +20は時刻が0でも被除数が負にならないようにするもので、0方向への切り捨てで値がずれるのを防ぐのだ～🌱
        // 累積量をレベルの分だけ平行移動させるだけだから、増分には影響しないのだ～🌱
        fun getExperienceCount(time: Int) = ((time.toLong() + 20) * (amplifier + 1) + 10) / 20
        // 累積量の増分を与えるのだ～🌱
        // 20がレベルで割り切れない場合でも、20tickあたりレベル個になるように均等な間隔へ配分するのだ～🌱
        // 1tickあたり2個以上も与えられるから、レベル21以上でも毎秒20個で頭打ちにならないのだ～🌱
        return (getExperienceCount(time) - getExperienceCount(time - 1)).toInt()
    }

    override fun shouldApplyEffectTickThisTick(duration: Int, amplifier: Int) = getExperienceAmount(duration, amplifier) > 0

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        super.applyEffectTick(entity, amplifier)
        val level = entity.level()
        if (level.isServer && entity is Player) {
            // MobEffectInstance は残り効果時間を減らす前にここを呼ぶから、shouldApplyEffectTickThisTick に渡されたものと同じ時刻がここで読めるのだ～🌱
            // 無限の効果ではそちらへ残り効果時間の代わりにエンティティの経過 tick が渡されるので、ここもそれに合わせるのだ～🌱
            val mobEffectInstance = entity.getEffect(experienceStatusEffect.getHolder())!!
            val time = if (mobEffectInstance.isInfiniteDuration) entity.tickCount else mobEffectInstance.duration
            entity.giveExperiencePoints(getExperienceAmount(time, amplifier))
            level.playSound(null, entity.x, entity.y, entity.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.35F + 0.9F)
        }
        return true
    }
}
