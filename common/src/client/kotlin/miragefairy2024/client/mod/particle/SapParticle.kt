package miragefairy2024.client.mod.particle

import miragefairy2024.client.util.setRgb
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

@Suppress("LeakingThis")
open class SapParticle(level: ClientLevel, x: Double, y: Double, z: Double) : TextureSheetParticle(level, x, y, z) {
    init {
        setSize(0.01F, 0.01F)
        gravity = 0.06F
        setRgb(0xFF9F32)
    }

    override fun getRenderType(): ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_OPAQUE

    override fun tick() {
        xo = x
        yo = y
        zo = z

        lifetime--
        if (lifetime <= 0) {
            remove()
            onDiedByAge()
        }
        if (removed) return

        yd -= gravity
        move(xd, yd, zd)
        updateVelocity()
        if (removed) return

        xd *= 0.98F
        yd *= 0.98F
        zd *= 0.98F
    }

    open fun onDiedByAge() {

    }

    open fun updateVelocity() {

    }

    class Dripping(level: ClientLevel, x: Double, y: Double, z: Double, spriteProvider: SpriteSet, private val particleEffect: ParticleOptions) : SapParticle(level, x, y, z) {
        init {
            pickSprite(spriteProvider)
            gravity *= 0.01F
            lifetime = 100
        }

        override fun onDiedByAge() {
            level.addParticle(particleEffect, x, y, z, xd, yd, zd)
        }

        override fun updateVelocity() {
            xd *= 0.02
            yd *= 0.02
            zd *= 0.02
        }
    }

    class Falling(level: ClientLevel, x: Double, y: Double, z: Double, spriteProvider: SpriteSet, private val particleEffect: ParticleOptions) : SapParticle(level, x, y, z) {
        init {
            pickSprite(spriteProvider)
            gravity = 0.01F
            lifetime = (64.0 / (level.random.nextDouble() * 0.8 + 0.2)).toInt()
        }

        override fun updateVelocity() {
            if (onGround) {
                remove()
                level.addParticle(particleEffect, x, y, z, 0.0, 0.0, 0.0)
                level.playLocalSound(x, y, z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, 0.3F + 0.7F * level.random.nextFloat(), 1.0F, false)
            }
        }
    }

    class Landing(level: ClientLevel, x: Double, y: Double, z: Double, spriteProvider: SpriteSet) : SapParticle(level, x, y, z) {
        init {
            pickSprite(spriteProvider)
            lifetime = (128.0 / (level.random.nextDouble() * 0.8 + 0.2)).toInt()
        }
    }
}
