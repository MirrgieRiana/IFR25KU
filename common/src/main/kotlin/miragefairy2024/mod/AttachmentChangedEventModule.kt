package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Channel
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.registerServerToClientPayloadType
import miragefairy2024.util.sendToClient
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

// TODO 1.21.6 になったらこれに移行する↓
// https://modrinth.com/mod/fabric-api/version/0.123.0+1.21.6


object AttachmentChangedEventChannel : Channel<ResourceLocation>(MirageFairy2024.identifier("attachment_changed_event")) {
    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: ResourceLocation) = ResourceLocation.STREAM_CODEC.encode(buf, packet)
    override fun readFromBuf(buf: RegistryFriendlyByteBuf): ResourceLocation = ResourceLocation.STREAM_CODEC.decode(buf)
}

object AttachmentChangedEvent {
    val eventRegistry = EventRegistry<(ResourceLocation) -> Unit>()
}

fun ServerPlayer.sendAttachmentChangedEvent(attachmentType: AttachmentType<*>) = AttachmentChangedEventChannel.sendToClient(this, attachmentType.identifier())

context(ModContext)
fun initAttachmentChangedEventModule() {
    AttachmentChangedEventChannel.registerServerToClientPayloadType()
}
