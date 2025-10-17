package miragefairy2024.client.fabric

import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import miragefairy2024.client.mod.recipeviewer.emi.EmiClientEvents
import mirrg.kotlin.helium.Single

@Suppress("unused")
class MirageFairy2024FabricEmiPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        EmiClientEvents.onRegister.fire { it(Single(registry)) }
    }
}
