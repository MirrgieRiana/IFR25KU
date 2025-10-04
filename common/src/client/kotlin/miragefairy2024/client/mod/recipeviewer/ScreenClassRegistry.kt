package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.util.FreezableRegistry
import miragefairy2024.util.set
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.resources.ResourceKey
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

object ScreenClassRegistry {
    private val registry = FreezableRegistry<ResourceKey<MenuType<*>>, ScreenClass<*, *>>()

    class ScreenClass<M : AbstractContainerMenu, S : AbstractContainerScreen<M>>(val clazz: Class<S>)

    fun <M : AbstractContainerMenu, S : AbstractContainerScreen<M>> register(menuTypeKey: ResourceKey<MenuType<*>>, clazz: Class<S>) {
        registry[menuTypeKey] = ScreenClass(clazz)
    }

    fun get(menuTypeKey: ResourceKey<MenuType<*>>): ScreenClass<*, *> {
        return registry.freezeAndGet()[menuTypeKey] ?: error("Unknown screen id: $menuTypeKey")
    }
}
