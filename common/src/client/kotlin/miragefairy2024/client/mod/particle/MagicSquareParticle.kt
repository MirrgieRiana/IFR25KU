package miragefairy2024.client.mod.particle

import com.mojang.blaze3d.vertex.VertexConsumer
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.particle.MagicSquareParticleChannel
import miragefairy2024.mod.particle.MagicSquareParticleEffect
import mirrg.kotlin.helium.max
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

fun initMagicSquareParticle() {
    MagicSquareParticleChannel.registerClientPacketReceiver { packet ->
        val particleManager = Minecraft.getInstance().particleEngine ?: return@registerClientPacketReceiver
        (0..6).forEach { i ->
            particleManager.createParticle(
                MagicSquareParticleEffect(i, packet.targetPosition, 30F * (i.toFloat() / 6F)),
                packet.position.x,
                packet.position.y,
                packet.position.z,
                0.0,
                0.0,
                0.0,
            )
        }
    }
}

fun createMagicSquareParticleFactory() = { spriteProvider: SpriteSet ->
    ParticleProvider<MagicSquareParticleEffect> { parameters, world, x, y, z, _, _, _ ->
        MagicSquareParticle(world, x, y, z, parameters.layer, parameters.targetPosition, spriteProvider).also {
            it.alphaTicks[0] = parameters.delay
            it.alphaTicks[1] = parameters.delay + 20F
            it.lightTicks[0] = parameters.delay
            it.lightTicks[1] = parameters.delay + 20F
        }
    }
}

class MagicSquareParticle(world: ClientLevel, x: Double, y: Double, z: Double, layer: Int, private val targetPosition: Vec3, spriteProvider: SpriteSet) : TextureSheetParticle(world, x, y, z) {

    var delay = 0
    var color1 = 0xFFF4D3
    var color2 = 0xFFB82C
    var color3 = 0x341B0E
    var alphaTicks = arrayOf(0F, 10F, 70F, 80F)
    var lightTicks = arrayOf(0F, 10F, 70F, 80F)

    init {
        val sprites = (spriteProvider as FabricSpriteProvider).sprites
        setSprite(sprites[layer.coerceIn(sprites.indices)])
        lifetime = 80
        quadSize = 0.5F
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
        val r1 = ((color1 shr 16) and 0xFF).toFloat() / 255F
        val g1 = ((color1 shr 8) and 0xFF).toFloat() / 255F
        val b1 = (color1 and 0xFF).toFloat() / 255F
        val r2 = ((color2 shr 16) and 0xFF).toFloat() / 255F
        val g2 = ((color2 shr 8) and 0xFF).toFloat() / 255F
        val b2 = (color2 and 0xFF).toFloat() / 255F
        rCol = r1 + (r2 - r1) * delta
        gCol = g1 + (g2 - g1) * delta
        bCol = b1 + (b2 - b1) * delta
    }

    private fun setColor(color: Int) {
        rCol = ((color shr 16) and 0xFF).toFloat() / 255F
        gCol = ((color shr 8) and 0xFF).toFloat() / 255F
        bCol = (color and 0xFF).toFloat() / 255F
    }

    override fun render(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        if (delay > 0) return

        val a = age.toFloat() + tickDelta
        when {
            a < alphaTicks[0] -> setColor(color1)
            a < alphaTicks[1] -> setColor(color1)
            a < alphaTicks[2] -> setColor(color1, color2, (a - alphaTicks[1]) / (alphaTicks[2] - alphaTicks[1]))
            a < alphaTicks[3] -> setColor(color2, color3, (a - alphaTicks[2]) / (alphaTicks[3] - alphaTicks[2]))
            else -> setColor(color3)
        }

        alpha = getValue(tickDelta, alphaTicks)
        buildGeometry(vertexConsumer, camera, tickDelta, false)
        buildGeometry(vertexConsumer, camera, tickDelta, true)
    }

    private fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float, flip: Boolean) {

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

        val brightness = getLightColor(tickDelta)

        vertex(vertexConsumer, vector3fs[0], u1, v1, brightness)
        vertex(vertexConsumer, vector3fs[1], u1, v0, brightness)
        vertex(vertexConsumer, vector3fs[2], u0, v0, brightness)
        vertex(vertexConsumer, vector3fs[3], u0, v1, brightness)
    }

    private fun vertex(vertexConsumer: VertexConsumer, pos: Vector3f, u: Float, v: Float, light: Int) {
        vertexConsumer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(rCol, gCol, bCol, alpha).setLight(light)
    }

    override fun tick() {
        if (delay > 0) {
            delay--
            return
        }

        age++
        if (age >= lifetime) remove()

        oRoll = roll
        roll += Mth.TWO_PI / 60F

        // TODO 魔方陣の周りのパーティクル
    }

    override fun getQuadSize(tickDelta: Float) = quadSize * (1F - 0.2F * Mth.cos((age.toFloat() + tickDelta) / 80F * Mth.TWO_PI))

    override fun getLightColor(tint: Float): Int {
        val brightness = super.getLightColor(tint)
        val oldSkyLight = (brightness shr 20) and 0xF
        val oldBlockLight = (brightness shr 4) and 0xF
        val skyLight = oldSkyLight
        val blockLight = oldBlockLight max (15F * getValue(tint, lightTicks)).roundToInt()
        return (skyLight shl 20) or (blockLight shl 4)
    }

    override fun getRenderType(): ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT

}
