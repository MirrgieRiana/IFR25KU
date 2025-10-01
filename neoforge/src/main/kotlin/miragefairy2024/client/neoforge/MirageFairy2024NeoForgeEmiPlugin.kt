package miragefairy2024.client.neoforge

import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import miragefairy2024.mod.recipeviewer.EmiEvents

@EmiEntrypoint
@Suppress("unused")
class MirageFairy2024NeoForgeEmiPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        EmiEvents.onRegister.fire { it(registry) }
    }
}
