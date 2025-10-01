package miragefairy2024.client.fabric

import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import miragefairy2024.mod.recipeviewer.EmiEvents

@Suppress("unused")
class MirageFairy2024FabricEmiPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        EmiEvents.onRegister.fire { it(registry) }
    }
}
