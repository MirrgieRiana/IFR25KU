package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.mod.SoundEventChannel
import miragefairy2024.util.registerClientPacketReceiver
import net.minecraft.client.Minecraft

context(ModContext)
fun initSoundEventClientModule() {
    SoundEventChannel.registerClientPacketReceiver { packet ->
        val client = Minecraft.getInstance() ?: return@registerClientPacketReceiver
        val level = client.level ?: return@registerClientPacketReceiver
        level.playLocalSound(
            packet.pos,
            packet.soundEvent,
            packet.category,
            packet.volume,
            packet.pitch,
            packet.useDistance,
        )
    }
}
