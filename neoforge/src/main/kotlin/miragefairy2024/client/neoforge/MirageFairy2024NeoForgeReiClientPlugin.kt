package miragefairy2024.client.neoforge

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.forge.REIPluginClient
import miragefairy2024.client.mod.recipeviewer.rei.ReiClientEvents
import miragefairy2024.mod.recipeviewer.rei.ReiEvents
import mirrg.kotlin.helium.Single

@REIPluginClient
@Suppress("unused")
class MirageFairy2024NeoForgeReiClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        ReiClientEvents.onRegisterCategories.fire { it(Single(registry)) }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        ReiClientEvents.onRegisterDisplays.fire { it(Single(registry)) }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        ReiEvents.onRegisterItemComparators.fire { it(Single(registry)) }
    }

    override fun registerScreens(registry: ScreenRegistry) {
        ReiClientEvents.onRegisterScreens.fire { it(Single(registry)) }
    }
}
