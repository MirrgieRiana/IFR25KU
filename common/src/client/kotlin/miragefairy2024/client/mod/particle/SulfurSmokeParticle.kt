package miragefairy2024.client.mod.particle

import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.BaseAshSmokeParticle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.core.particles.SimpleParticleType

/** 重力の向きは引数で決まるのだぁ🌱 負の値を渡すと、煙が上へ昇っていくのだぁ✨ */
fun createSulfurSmokeParticleFactory(gravity: Float) = { spriteProvider: SpriteSet ->
    ParticleProvider<SimpleParticleType> { _, world, x, y, z, velocityX, velocityY, velocityZ ->
        SulfurSmokeParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider, gravity)
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
    gravity: Float,
) : BaseAshSmokeParticle(world, x, y, z, 0.1F, 0.1F, 0.1F, velocityX, velocityY, velocityZ, 2.5F, spriteProvider, 0.3F, 8, gravity, true) {
    init {
        // 析出した硫黄のテクスチャの代表的な色なのだぁ🌱
        setColor(226 / 255F, 214 / 255F, 144 / 255F)
    }
}
