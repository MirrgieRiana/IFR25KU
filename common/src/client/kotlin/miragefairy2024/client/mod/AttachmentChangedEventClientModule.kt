package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.AttachmentChangedEvent
import miragefairy2024.mod.AttachmentChangedEventChannel
import miragefairy2024.util.fire

context(ModContext)
fun initAttachmentChangedEventClientModule() {
    AttachmentChangedEventChannel.registerClientPacketReceiver { packet ->
        AttachmentChangedEvent.eventRegistry.fire { it(packet) }
    }
}
