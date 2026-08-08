package miragefairy2024.client.mod.particle

import miragefairy2024.client.util.setColor
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.BaseAshSmokeParticle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.util.Mth

fun createSulfurSmokeParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<SimpleParticleType> { _, world, x, y, z, velocityX, velocityY, velocityZ ->
        SulfurSmokeParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider)
    }
}

/**
 * 硫黄の色をした、大きめの煙なのだぁ🌱
 *
 * バニラの白い煙が、灰色の煙の色だけを差し替えたものなので、それに倣ったのだぁ✨
 *
 * @see net.minecraft.client.particle.WhiteSmokeParticle
 * @see net.minecraft.client.particle.LargeSmokeParticle
 */
class SulfurSmokeParticle(
    world: ClientLevel,
    x: Double,
    y: Double,
    z: Double,
    velocityX: Double,
    velocityY: Double,
    velocityZ: Double,
    spriteProvider: SpriteSet,
) : BaseAshSmokeParticle(world, x, y, z, 0.1F, 0.1F, 0.1F, velocityX, velocityY, velocityZ, 2.5F + world.random.nextFloat() * 2.5F, spriteProvider, 0.3F, 27, -0.1F, true) {
    companion object {
        /** 風がひとまわりするのにかかるティック数なのだぁ🌱 */
        private const val WIND_CYCLE = 1200.0F

        /** 毎ティック風から受ける水平方向の加速度なのだぁ🌱 摩擦0.96と釣り合って、終端速度は0.05になるのだぁ✨ */
        private const val WIND_ACCELERATION = 0.002
    }

    init {
        // 析出した硫黄のテクスチャの代表的な色なのだぁ🌱
        setColor(0xE2D690)
    }

    override fun tick() {
        super.tick()
        // 火山の噴煙は真上には昇らず、風を受けて横へたなびくのだぁ🌱 風向きはワールド全体で共通で、ゆっくりとひとまわりするのだぁ✨
        val windAngle = level.gameTime / WIND_CYCLE * Mth.TWO_PI
        xd += Mth.cos(windAngle) * WIND_ACCELERATION
        zd += Mth.sin(windAngle) * WIND_ACCELERATION
    }
}
