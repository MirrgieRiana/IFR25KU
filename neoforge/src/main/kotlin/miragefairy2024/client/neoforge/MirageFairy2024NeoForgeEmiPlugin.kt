package miragefairy2024.client.neoforge

import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import miragefairy2024.client.mod.recipeviewer.registerEmi

@EmiEntrypoint
@Suppress("unused")
class MirageFairy2024NeoForgeEmiPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) = registerEmi(registry)
}
