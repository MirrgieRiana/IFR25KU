package miragefairy2024.neoforge

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.Modules
import miragefairy2024.client.ClientProxyImpl
import miragefairy2024.client.initClientModules
import miragefairy2024.clientProxy
import miragefairy2024.platformProxy
import miragefairy2024.util.Registration
import miragefairy2024.util.RegistryEvents
import net.minecraft.core.Registry
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.registries.RegisterEvent

@Mod(MirageFairy2024.MOD_ID)
class MirageFairy2024NeoForgeMod(modEventBus: IEventBus, modContainer: ModContainer) {
    init {
        with(ModContext()) {
            platformProxy = NeoForgePlatformProxy()
            Modules.init()
            initNeoForgeModule()
            if (FMLEnvironment.dist == Dist.CLIENT) initClientModules()
        }

        ModEvents.onConstruction.fire { it() }

        modEventBus.addListener(RegisterEvent::class.java) { event ->
            RegistryEvents.registrations.forEach { registration ->
                fun <T : Any, U : T> f(registration: Registration<T, U>) {
                    if (event.registry == registration.registry) {
                        val value = runBlocking {
                            val deferred = CompletableDeferred<U>()

                            launch {
                                deferred.complete(registration.creator())
                            }
                            yield()

                            if (deferred.isActive) throw IllegalStateException("Illegal suspend for initialization: ${registration.identifier}")
                            deferred.await()
                        }
                        registration.complete(value, Registry.registerForHolder(registration.registry, registration.identifier, value))
                    }
                }
                f(registration)
            }
        }
        modEventBus.addListener(FMLCommonSetupEvent::class.java) {
            ModEvents.onInitialize.fire { it() }
            it.enqueueWork {
                ModEvents.onTerraBlenderInitialized.fire { it() }
            }
        }
        modEventBus.addListener(FMLClientSetupEvent::class.java) {
            clientProxy = ClientProxyImpl()
            ModEvents.onClientInit.fire { it() }
        }
    }
}
