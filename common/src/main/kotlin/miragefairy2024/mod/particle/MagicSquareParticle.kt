package miragefairy2024.mod.particle

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Channel
import mirrg.kotlin.helium.unit
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3

class MagicSquareParticleType(alwaysSpawn: Boolean) : ParticleType<MagicSquareParticleEffect>(alwaysSpawn) {
    companion object {
        private val VEC3_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Vec3> = object : StreamCodec<RegistryFriendlyByteBuf, Vec3> {
            override fun decode(byteBuf: RegistryFriendlyByteBuf) = byteBuf.readVec3()
            override fun encode(byteBuf: RegistryFriendlyByteBuf, vec3: Vec3) = unit { byteBuf.writeVec3(vec3) }
        }

        val CODEC: MapCodec<MagicSquareParticleEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("layer").forGetter(MagicSquareParticleEffect::layer),
                Vec3.CODEC.fieldOf("targetPosition").forGetter(MagicSquareParticleEffect::targetPosition),
                Codec.FLOAT.fieldOf("delay").forGetter(MagicSquareParticleEffect::delay),
            ).apply(instance, ::MagicSquareParticleEffect)
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, MagicSquareParticleEffect> = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            { it.layer },
            VEC3_STREAM_CODEC,
            { it.targetPosition },
            ByteBufCodecs.FLOAT,
            { it.delay },
            ::MagicSquareParticleEffect,
        )
    }

    override fun codec() = CODEC
    override fun streamCodec() = STREAM_CODEC
}

class MagicSquareParticleEffect(val layer: Int, val targetPosition: Vec3, val delay: Float) : ParticleOptions {
    override fun getType() = ParticleTypeCard.MAGIC_SQUARE.particleType
}

object MagicSquareParticleChannel : Channel<MagicSquareParticlePacket>(MirageFairy2024.identifier("magic_square_particle")) {
    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: MagicSquareParticlePacket) {
        buf.writeDouble(packet.position.x)
        buf.writeDouble(packet.position.y)
        buf.writeDouble(packet.position.z)
        buf.writeDouble(packet.targetPosition.x)
        buf.writeDouble(packet.targetPosition.y)
        buf.writeDouble(packet.targetPosition.z)
    }

    override fun readFromBuf(buf: RegistryFriendlyByteBuf): MagicSquareParticlePacket {
        val positionX = buf.readDouble()
        val positionY = buf.readDouble()
        val positionZ = buf.readDouble()
        val targetPositionX = buf.readDouble()
        val targetPositionY = buf.readDouble()
        val targetPositionZ = buf.readDouble()
        return MagicSquareParticlePacket(Vec3(positionX, positionY, positionZ), Vec3(targetPositionX, targetPositionY, targetPositionZ))
    }
}

class MagicSquareParticlePacket(val position: Vec3, val targetPosition: Vec3)
