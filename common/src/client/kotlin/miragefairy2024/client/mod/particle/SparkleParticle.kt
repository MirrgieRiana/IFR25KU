package miragefairy2024.client.mod.particle

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.core.particles.SimpleParticleType

private const val LIFETIME_TICKS = 120

private const val RISE_ACCELERATION = 0.00014

/** 寿命のうち、この割合を過ぎるまでに、表示サイズが 0 倍から 1 倍まで大きくなるのだ～🌱 */
private const val GROW_END_RATE = 0.05F

/** 寿命のうち、この割合を過ぎてから薄くなり始めるのだ～🌱 */
private const val FADE_START_RATE = 0.7F

private const val TWINKLE_INTERVAL_TICKS = 3

/**
 * [net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT] の合成を、加算合成へ置き換えたものなのだ～🌱
 * バニラの [net.minecraft.client.particle.ParticleRenderType] には、加算合成をするものが 1 個も無いのだ～🌱
 */
private val ADDITIVE_PARTICLE_SHEET = object : ParticleRenderType {
    override fun begin(tesselator: Tesselator, textureManager: TextureManager): BufferBuilder {
        RenderSystem.depthMask(true)
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
    }

    override fun toString() = "MIRAGEFAIRY2024_ADDITIVE_PARTICLE_SHEET"
}

fun createSparkleParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<SimpleParticleType> { _, level, x, y, z, _, _, _ ->
        object : TextureSheetParticle(level, x, y, z) {

            init {
                quadSize *= 0.4F + Math.random().toFloat() * 0.2F
                lifetime = LIFETIME_TICKS
                setSprite(spriteProvider.get(0, 7))
            }

            override fun getRenderType(): ParticleRenderType = ADDITIVE_PARTICLE_SHEET

            /**
             * [net.minecraft.client.particle.GlowParticle.getLightColor] と同じく、周りの明るさに関わらず自分で光るのだ～🌱
             */
            override fun getLightColor(partialTick: Float) = 240 or (super.getLightColor(partialTick) shr 16 and 0xFF shl 16)

            override fun getQuadSize(scaleFactor: Float): Float {
                val lifeRate = (age.toFloat() + scaleFactor) / lifetime.toFloat()
                return super.getQuadSize(scaleFactor) * (lifeRate / GROW_END_RATE atMost 1.0F)
            }

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

                yd += RISE_ACCELERATION
                move(xd, yd, zd)
                // ブロックの下面に当たったら、そこで昇るのをやめるのだ～🌱
                if (this.y == yo) yd = 0.0

                val lifeRate = age.toFloat() / lifetime.toFloat()
                setAlpha((1.0F - lifeRate) / (1.0F - FADE_START_RATE) atLeast 0.0F atMost 1.0F)
            }
        }
    }
}
