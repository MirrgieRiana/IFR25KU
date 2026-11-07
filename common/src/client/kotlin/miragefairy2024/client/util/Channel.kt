package miragefairy2024.client.util

import dev.architectury.networking.NetworkManager
import miragefairy2024.util.Channel

fun <P> Channel<P>.sendToServer(packet: P) {
    NetworkManager.sendToServer(Channel.Payload(this, packet))
}
