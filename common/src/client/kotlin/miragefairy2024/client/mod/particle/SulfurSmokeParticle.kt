package miragefairy2024.client.mod.particle

import miragefairy2024.client.util.setRgb
import net.minecraft.client.particle.BaseAshSmokeParticle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.util.Mth

private const val WIND_CYCLE_TICKS = 20F * 60F

// 摩擦0.96と釣り合って、終端速度は0.05になるのだぁ✨
// 毎ティック 0.002 足して、0.96 をかける、を繰り返すと、だんだん速さが一定に近づくのだぁ～🌱
// その落ち着き先が、0.002 を 1 引く 0.96 の 0.04 で割った値で、0.05 なのだぁ～✨
private const val WIND_HORIZONTAL_ACCELERATION = 0.002

fun createSulfurSmokeParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<SimpleParticleType> { _, level, x, y, z, xSpeed, ySpeed, zSpeed ->
        /**
         * バニラの白い煙が、灰色の煙の色だけを差し替えたものなので、それに倣ったのだぁ✨
         *
         * @see net.minecraft.client.particle.WhiteSmokeParticle
         * @see net.minecraft.client.particle.LargeSmokeParticle
         */
        object : BaseAshSmokeParticle(
            level,
            x, y, z,
            0.1F, 0.1F, 0.1F,
            xSpeed, ySpeed, zSpeed,
            2.5F * (1F + level.random.nextFloat()),
            spriteProvider,
            0.3F,
            27,
            -0.1F,
            true,
        ) {
            init {
                setRgb(0xE2D690)
            }

            override fun tick() {
                super.tick()
                // 火山の噴煙は真上には昇らず、風を受けて横へたなびくのだぁ🌱 風向きはワールド全体で共通で、ゆっくりとひとまわりするのだぁ✨
                val windAngle = level.gameTime / WIND_CYCLE_TICKS * Mth.TWO_PI
                xd += Mth.cos(windAngle) * WIND_HORIZONTAL_ACCELERATION
                zd += Mth.sin(windAngle) * WIND_HORIZONTAL_ACCELERATION
            }
        }
    }
}
