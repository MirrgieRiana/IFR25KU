package miragefairy2024.client.mod.particle

import com.mojang.blaze3d.vertex.VertexConsumer
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.particle.MagicSquareParticleChannel
import miragefairy2024.mod.particle.MagicSquareParticleEffect
import mirrg.kotlin.helium.max
import mirrg.kotlin.hydrogen.blueOfRgb
import mirrg.kotlin.hydrogen.greenOfRgb
import mirrg.kotlin.hydrogen.redOfRgb
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.client.renderer.LightTexture
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

fun initMagicSquareParticle() {
    MagicSquareParticleChannel.registerClientPacketReceiver { packet ->
        val particleManager = Minecraft.getInstance().particleEngine ?: return@registerClientPacketReceiver
        particleManager.createParticle(
            MagicSquareParticleEffect(packet.targetPosition),
            packet.position.x,
            packet.position.y,
            packet.position.z,
            0.0,
            0.0,
            0.0,
        )
    }
}

fun createMagicSquareParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<MagicSquareParticleEffect> { parameters, level, x, y, z, _, _, _ ->
        MagicSquareParticle(level, x, y, z, parameters.targetPosition, spriteProvider)
    }
}

class MagicSquareParticle(level: ClientLevel, x: Double, y: Double, z: Double, private val targetPosition: Vec3, spriteProvider: SpriteSet) : TextureSheetParticle(level, x, y, z) {

    private val sprites = (spriteProvider as FabricSpriteProvider).sprites

    var color1 = 0xFFF4D3
    var color2 = 0xFFB82C
    var color3 = 0x341B0E

    init {
        lifetime = 80
        quadSize = 0.5F
    }

    /** 層ごとに、現れ始める時刻をずらすのだ～🌱 */
    private fun getLayerDelay(layer: Int) = 30F * (layer.toFloat() / (LAYER_COUNT - 1).toFloat())

    /** 層ごとの、現れてから消えるまでの 4 個の節目なのだ～🌱 */
    private fun getLayerTicks(layer: Int): Array<Float> {
        val delay = getLayerDelay(layer)
        return arrayOf(delay, delay + 20F, 70F, 80F)
    }

    //       0     1         2     3
    // 　　　　　／￣￣￣￣￣＼
    // 　　　　／　　　　　　　＼
    // ＿＿＿／　　　　　　　　　＼

    private fun getValue(tickDelta: Float, ticks: Array<Float>): Float {
        val a = age.toFloat() + tickDelta
        return when {
            a < ticks[0] -> 0F
            a < ticks[1] -> 0F + (a - ticks[0]) / (ticks[1] - ticks[0])
            a < ticks[2] -> 1F
            a < ticks[3] -> 1F - (a - ticks[2]) / (ticks[3] - ticks[2])
            else -> 0F
        }
    }

    private fun setColor(color1: Int, color2: Int, delta: Float) {
        val r1 = color1.redOfRgb.toFloat() / 255F
        val g1 = color1.greenOfRgb.toFloat() / 255F
        val b1 = color1.blueOfRgb.toFloat() / 255F
        val r2 = color2.redOfRgb.toFloat() / 255F
        val g2 = color2.greenOfRgb.toFloat() / 255F
        val b2 = color2.blueOfRgb.toFloat() / 255F
        rCol = r1 + (r2 - r1) * delta
        gCol = g1 + (g2 - g1) * delta
        bCol = b1 + (b2 - b1) * delta
    }

    private fun setColor(color: Int) {
        rCol = color.redOfRgb.toFloat() / 255F
        gCol = color.greenOfRgb.toFloat() / 255F
        bCol = color.blueOfRgb.toFloat() / 255F
    }

    override fun render(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        (0 until LAYER_COUNT).forEach { layer ->
            val ticks = getLayerTicks(layer)
            val value = getValue(tickDelta, ticks)
            // まだ現れていない層と、消え切った層は、頂点を 1 個も積まないのだ～🌱
            if (value <= 0F) return@forEach

            setSprite(sprites[layer.coerceIn(sprites.indices)])

            val a = age.toFloat() + tickDelta
            when {
                a < ticks[1] -> setColor(color1)
                a < ticks[2] -> setColor(color1, color2, (a - ticks[1]) / (ticks[2] - ticks[1]))
                a < ticks[3] -> setColor(color2, color3, (a - ticks[2]) / (ticks[3] - ticks[2]))
                else -> setColor(color3)
            }
            alpha = value

            val brightness = getLayerLightColor(tickDelta, ticks)
            buildGeometry(vertexConsumer, camera, tickDelta, false, brightness)
            buildGeometry(vertexConsumer, camera, tickDelta, true, brightness)
        }
    }

    private fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float, flip: Boolean, brightness: Int) {

        val offsetX = (targetPosition.x - x).toFloat()
        val offsetY = (targetPosition.y - y).toFloat()
        val offsetZ = (targetPosition.z - z).toFloat()

        val yaw = Mth.atan2(offsetX.toDouble(), offsetZ.toDouble()).toFloat()
        val pitch = Mth.atan2(-offsetY.toDouble(), Mth.sqrt(offsetX * offsetX + offsetZ * offsetZ).toDouble()).toFloat()

        val quaternionf = Quaternionf().rotationYXZ(yaw, pitch, Mth.lerp(tickDelta, oRoll, roll))
        if (flip) quaternionf.rotateY(-Mth.PI)

        val size = getQuadSize(tickDelta)

        val cameraPos = camera.position
        val translateX = (Mth.lerp(tickDelta.toDouble(), xo, x) - cameraPos.x()).toFloat()
        val translateY = (Mth.lerp(tickDelta.toDouble(), yo, y) - cameraPos.y()).toFloat()
        val translateZ = (Mth.lerp(tickDelta.toDouble(), zo, z) - cameraPos.z()).toFloat()

        val vector3fs = arrayOf(
            Vector3f(-1.0F, -1.0F, 0.0F),
            Vector3f(-1.0F, 1.0F, 0.0F),
            Vector3f(1.0F, 1.0F, 0.0F),
            Vector3f(1.0F, -1.0F, 0.0F),
        )
        vector3fs.forEach { vector3f ->
            vector3f.rotate(quaternionf)
            vector3f.mul(size)
            vector3f.add(translateX, translateY, translateZ)
        }

        vertex(vertexConsumer, vector3fs[0], u1, v1, brightness)
        vertex(vertexConsumer, vector3fs[1], u1, v0, brightness)
        vertex(vertexConsumer, vector3fs[2], u0, v0, brightness)
        vertex(vertexConsumer, vector3fs[3], u0, v1, brightness)
    }

    private fun vertex(vertexConsumer: VertexConsumer, pos: Vector3f, u: Float, v: Float, light: Int) {
        vertexConsumer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setLight(light)
    }

    override fun tick() {
        age++
        if (age >= lifetime) remove()

        oRoll = roll
        roll += Mth.TWO_PI / 60F

        // TODO 魔方陣の周りのパーティクル
    }

    override fun getQuadSize(tickDelta: Float) = quadSize * (1F - 0.2F * Mth.cos((age.toFloat() + tickDelta) / 80F * Mth.TWO_PI))

    private fun getLayerLightColor(tint: Float, ticks: Array<Float>): Int {
        val brightness = super.getLightColor(tint)
        val oldSkyLight = LightTexture.sky(brightness)
        val oldBlockLight = LightTexture.block(brightness)
        val skyLight = oldSkyLight
        val blockLight = oldBlockLight max (15F * getValue(tint, ticks)).roundToInt()
        return LightTexture.pack(blockLight, skyLight)
    }

    override fun getRenderType(): ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT

    companion object {
        /** 重ねる魔法陣の枚数なのだ～🌱 */
        private const val LAYER_COUNT = 7
    }

}
