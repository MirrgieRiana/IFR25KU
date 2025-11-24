package miragefairy2024.fabric

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.fabric.MirageFairy2024FabricMod.initializeRegistrationsIfNeeded
import miragefairy2024.platformProxy
import terrablender.api.TerraBlenderApi

class MirageFairy2024FabricTerraBlenderEntryPoint : TerraBlenderApi {
    override fun onTerraBlenderInitialized() {
        with(ModContext()) {
            platformProxy = FabricPlatformProxy()
            Modules.init()
            initFabricModule()
        }

        initializeRegistrationsIfNeeded()

        ModEvents.onTerraBlenderInitialized.fire { it() }
    }
}
