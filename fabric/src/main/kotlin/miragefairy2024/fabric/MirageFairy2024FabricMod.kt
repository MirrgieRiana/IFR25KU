package miragefairy2024.fabric

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.platformProxy
import miragefairy2024.util.Registration
import miragefairy2024.util.RegistryEvents
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry

object MirageFairy2024FabricMod : ModInitializer {
    override fun onInitialize() {
        with(ModContext()) {
            platformProxy = FabricPlatformProxy()
            Modules.init()
            initFabricModule()
        }

        initializeRegistrationsIfNeeded()

        ModEvents.onInitialize.fire { it() }
    }

    private var initialized = false
    fun initializeRegistrationsIfNeeded() {
        if (initialized) return
        initialized = true
        runBlocking {
            RegistryEvents.registrations.forEach { registration ->
                launch {
                    suspend fun <T : Any, U : T> f(registration: Registration<T, U>) {
                        val value = registration.creator()
                        registration.complete(value, Registry.registerForHolder(registration.registry, registration.identifier, value))
                    }
                    f(registration)
                }
            }
        }
    }
}
