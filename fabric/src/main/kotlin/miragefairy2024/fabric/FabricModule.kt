package miragefairy2024.fabric

import miragefairy2024.ModContext
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

private var initialized = false

var currentServer: MinecraftServer? = null

context(_: ModContext)
fun initFabricModule() {
    if (initialized) return
    initialized = true

    ServerLifecycleEvents.SERVER_STARTING.register {
        currentServer = it
    }
    ServerLifecycleEvents.SERVER_STOPPED.register {
        currentServer = null
    }
}
