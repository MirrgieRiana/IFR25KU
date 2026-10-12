package miragefairy2024.client.mod.particle

import com.mojang.blaze3d.vertex.VertexConsumer
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.client.Camera
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.client.renderer.LightTexture
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.util.Mth
import org.joml.Quaternionf

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

/** 瞬きで暗くなったときの、不透明度の倍率なのだ～🌱 */
private const val TWINKLE_MIN_ALPHA = 0.7F

/** 周りの明るさに関わらず自分で光るための、ブロック光のレベルなのだ～🌱 */
private const val SELF_LIGHT_LEVEL = 15

/** 内側のレイヤーの、表示サイズの倍率なのだ～🌱 */
private const val INNER_LAYER_SCALE = 0.65F

/** 外側のレイヤーの、表示サイズの倍率なのだ～🌱 */
private const val OUTER_LAYER_SCALE = 1.35F

/** 内側のレイヤーが 1 回転するのにかかる tick 数なのだ～🌱 */
private const val INNER_LAYER_SPIN_TICKS = 18.0F

/**
 * 外側のレイヤーが 1 回転するのにかかる tick 数なのだ～🌱
 * 負の値にすると、内側とは逆向きに回るのだ～🌱
 */
private const val OUTER_LAYER_SPIN_TICKS = -54.0F

/**
 * 瞬きの位相から、[minRate] 倍と 1 倍の間を正弦で往復する倍率を求めるのだ～🌱
 * 表示サイズと不透明度で同じ位相を使うから、縮むときに一緒に暗くなるのだ～🌱
 */
private fun getTwinkleRate(ticks: Float, minRate: Float) = Mth.lerp((Mth.sin(ticks / TWINKLE_PERIOD_TICKS * Mth.TWO_PI) + 1.0F) / 2.0F, minRate, 1.0F)

fun createSparkleParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<SimpleParticleType> { _, level, x, y, z, _, _, _ ->
        object : TextureSheetParticle(level, x, y, z) {

            /** 描いている最中のレイヤーの、表示サイズの倍率なのだ～🌱 */
            private var layerScale = 1.0F

            init {
                quadSize *= 0.4F + Math.random().toFloat() * 0.2F
                lifetime = LIFETIME_TICKS
                // SpriteSet.get(age, lifetime) は lifetime で割るから、0 を渡すとゼロ除算になるのだ～🌱
                pickSprite(spriteProvider)
            }

            override fun getRenderType(): ParticleRenderType = AdditiveParticleRenderType

            /**
             * [net.minecraft.client.particle.GlowParticle.getLightColor] と同じく、周りの明るさに関わらず自分で光るのだ～🌱
             */
            override fun getLightColor(partialTick: Float) = LightTexture.pack(SELF_LIGHT_LEVEL, LightTexture.sky(super.getLightColor(partialTick)))

            override fun getQuadSize(scaleFactor: Float): Float {
                val lifeRate = (age.toFloat() + scaleFactor) / lifetime.toFloat()
                // 瞬きは、1 枚のテクスチャの表示サイズを伸び縮みさせることで表すのだ～🌱
                return super.getQuadSize(scaleFactor) * (lifeRate / GROW_END_RATE atMost 1.0F) * getTwinkleRate(age.toFloat() + scaleFactor, TWINKLE_MIN_SCALE) * layerScale
            }

            /**
             * 同じテクスチャを、大きさと回転の向きだけを変えて 2 枚重ねるのだ～🌱
             * [net.minecraft.client.particle.SingleQuadParticle.render] は 1 枚しか描かないから、レイヤーごとに向きを組み立てて描くのだ～🌱
             */
            override fun render(buffer: VertexConsumer, camera: Camera, partialTicks: Float) {
                renderLayer(buffer, camera, partialTicks, INNER_LAYER_SCALE, INNER_LAYER_SPIN_TICKS)
                renderLayer(buffer, camera, partialTicks, OUTER_LAYER_SCALE, OUTER_LAYER_SPIN_TICKS)
            }

            private fun renderLayer(buffer: VertexConsumer, camera: Camera, partialTicks: Float, scale: Float, spinTicks: Float) {
                // 表示サイズは getQuadSize が返すから、どのレイヤーを描いている最中かをそこへ伝えるのだ～🌱
                layerScale = scale
                val quaternion = Quaternionf()
                facingCameraMode.setRotation(quaternion, camera, partialTicks)
                quaternion.rotateZ((age.toFloat() + partialTicks) / spinTicks * Mth.TWO_PI)
                renderRotatedQuad(buffer, camera, quaternion, partialTicks)
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
                val fadeAlpha = (1.0F - lifeRate) / (1.0F - FADE_START_RATE) atLeast 0.0F atMost 1.0F
                setAlpha(fadeAlpha * getTwinkleRate(age.toFloat(), TWINKLE_MIN_ALPHA))
            }
        }
    }
}
