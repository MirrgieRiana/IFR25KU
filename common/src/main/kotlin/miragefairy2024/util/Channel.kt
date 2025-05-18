package miragefairy2024.util

import dev.architectury.networking.NetworkManager
import dev.architectury.platform.Platform
import net.fabricmc.api.EnvType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.world.phys.Vec3 as Vec3d

abstract class Channel<P>(packetId: ResourceLocation) {
    val streamCodec = object : StreamCodec<RegistryFriendlyByteBuf, Payload<P>> {
        override fun encode(`object`: RegistryFriendlyByteBuf, object2: Payload<P>) {
            writeToBuf(`object`, object2.data)
        }

        override fun decode(`object`: RegistryFriendlyByteBuf): Payload<P> {
            return Payload(this@Channel, readFromBuf(`object`))
        }
    }
    val type = CustomPacketPayload.Type<Payload<P>>(packetId)

    abstract fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: P)
    abstract fun readFromBuf(buf: RegistryFriendlyByteBuf): P

    class Payload<P>(val channel: Channel<P>, val data: P) : CustomPacketPayload {
        override fun type() = channel.type
    }
}

fun <P> Channel<P>.registerServerToClientPayloadType() {
    if (Platform.getEnv() == EnvType.SERVER) NetworkManager.registerS2CPayloadType(this.type, this.streamCodec)
}

fun <P> Channel<P>.sendToClient(player: ServerPlayerEntity, packet: P) {
    NetworkManager.sendToPlayer(player, Channel.Payload(this, packet))
}

fun <P> Channel<P>.sendToAround(world: ServerWorld, pos: Vec3d, distance: Double, packet: P) {
    val players = world.players()
        .filter { it.level().dimension() == world.dimension() }
        .filter { pos.distanceToSqr(it.position()) <= distance * distance }
    NetworkManager.sendToPlayers(players, Channel.Payload(this, packet))
}

fun <P> Channel<P>.registerServerPacketReceiver(handler: (ServerPlayerEntity, P) -> Unit) {
    NetworkManager.registerReceiver(NetworkManager.Side.C2S, this.type, this.streamCodec) { buf, context ->
        handler(context.player as ServerPlayer, buf.data)
    }
}
