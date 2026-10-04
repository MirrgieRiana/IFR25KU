package miragefairy2024.client.mod.particle

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexConsumer
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.client.Camera
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.util.Mth

private const val LIFETIME_TICKS = 120

private const val RISE_ACCELERATION = 0.00014

/** 寿命のうち、この割合を過ぎるまでに、表示サイズが 0 倍から 1 倍まで大きくなるのだ～🌱 */
private const val GROW_END_RATE = 0.05F

/** 寿命のうち、この割合を過ぎてから薄くなり始めるのだ～🌱 */
private const val FADE_START_RATE = 0.7F

/** 瞬きが 1 往復するのにかかる tick 数なのだ～🌱 */
private const val TWINKLE_PERIOD_TICKS = 42

/** 瞬きで縮んだときの、表示サイズの倍率なのだ～🌱 */
private const val TWINKLE_MIN_SCALE = 0.55F

fun createSparkleParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<SimpleParticleType> { _, level, x, y, z, _, _, _ ->
        object : TextureSheetParticle(level, x, y, z) {

            init {
                quadSize *= 0.4F + Math.random().toFloat() * 0.2F
                lifetime = LIFETIME_TICKS
                setSprite(spriteProvider.get(0, 0))
            }

            /**
             * Fabric 側の [net.minecraft.client.particle.ParticleEngine] は固定の一覧のみを描画するから、独自のものを足しても描かれないのだ～🌱
             * [net.minecraft.client.particle.ParticleRenderType.CUSTOM] は描画の設定を各パーティクルへ委ねるものだから、そちらを選ぶのだ～🌱
             */
            override fun getRenderType(): ParticleRenderType = ParticleRenderType.CUSTOM

            /**
             * [net.minecraft.client.particle.ParticleRenderType.CUSTOM] は、シェーダーとテクスチャと合成の設定を 1 個も行わないのだ～🌱
             * 描画の呼び出しは同じ一覧の全頂点を積み終えた後に 1 回だけ行われるから、ここで設定した状態がそのまま効くのだ～🌱
             */
            override fun render(buffer: VertexConsumer, camera: Camera, partialTick: Float) {
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
                RenderSystem.enableBlend()
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)
                super.render(buffer, camera, partialTick)
            }

            /**
             * [net.minecraft.client.particle.GlowParticle.getLightColor] と同じく、周りの明るさに関わらず自分で光るのだ～🌱
             */
            override fun getLightColor(partialTick: Float) = 240 or (super.getLightColor(partialTick) shr 16 and 0xFF shl 16)

            override fun getQuadSize(scaleFactor: Float): Float {
                val lifeRate = (age.toFloat() + scaleFactor) / lifetime.toFloat()
                // 瞬きは、1 枚のテクスチャの表示サイズを正弦で伸び縮みさせることで表すのだ～🌱
                val twinkleRate = Mth.sin((age.toFloat() + scaleFactor) / TWINKLE_PERIOD_TICKS * Mth.TWO_PI)
                val twinkleScale = Mth.lerp((twinkleRate + 1.0F) / 2.0F, TWINKLE_MIN_SCALE, 1.0F)
                return super.getQuadSize(scaleFactor) * (lifeRate / GROW_END_RATE atMost 1.0F) * twinkleScale
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
