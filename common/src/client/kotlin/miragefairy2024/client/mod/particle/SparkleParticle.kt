package miragefairy2024.client.mod.particle

import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.core.particles.SimpleParticleType

private const val HOVER_TICKS = 50
private const val RISE_TICKS = 70

private const val RISE_SPEED = 0.014

/** 寿命のうち、この割合を過ぎてから薄くなり始めるのだ～🌱 */
private const val FADE_START_RATE = 0.7F

private const val TWINKLE_INTERVAL_TICKS = 3

fun createSparkleParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<SimpleParticleType> { _, level, x, y, z, _, _, _ ->
        object : TextureSheetParticle(level, x, y, z) {

            init {
                quadSize *= 0.4F + Math.random().toFloat() * 0.2F
                lifetime = HOVER_TICKS + RISE_TICKS
                setSprite(spriteProvider.get(0, 7))
            }

            override fun getRenderType(): ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT

            /**
             * [net.minecraft.client.particle.GlowParticle.getLightColor] と同じく、周りの明るさに関わらず自分で光るのだ～🌱
             */
            override fun getLightColor(partialTick: Float) = 240 or (super.getLightColor(partialTick) shr 16 and 0xFF shl 16)

            override fun tick() {
                xo = this.x
                yo = this.y
                zo = this.z

                age++
                if (age >= lifetime) {
                    remove()
                    return
                }

                // 瞬きは、8 枚のテクスチャを往復させることで、光条が伸びては縮む様子を繰り返すのだ～🌱
                val phase = age / TWINKLE_INTERVAL_TICKS % 14
                setSprite(spriteProvider.get(if (phase < 8) phase else 14 - phase, 7))

                if (age >= HOVER_TICKS) {
                    yd = RISE_SPEED
                    move(xd, yd, zd)
                    // ブロックの下面に当たったら、そこで昇るのをやめるのだ～🌱
                    if (this.y == yo) yd = 0.0
                }

                val lifeRate = age.toFloat() / lifetime.toFloat()
                setAlpha((1.0F - lifeRate) / (1.0F - FADE_START_RATE) atLeast 0.0F atMost 1.0F)
            }
        }
    }
}
