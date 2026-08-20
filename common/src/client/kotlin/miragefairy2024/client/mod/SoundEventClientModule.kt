package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.SoundEventChannel
import net.minecraft.client.Minecraft

context(ModContext)
fun initSoundEventClientModule() {
    SoundEventChannel.registerClientPacketReceiver { packet ->
        val client = Minecraft.getInstance() ?: return@registerClientPacketReceiver
        val world = client.level ?: return@registerClientPacketReceiver
        world.playLocalSound(
            packet.pos,
            packet.soundEvent,
            packet.category,
            packet.volume,
            packet.pitch,
            packet.useDistance,
        )
    }
}
