package miragefairy2024.client.fabric

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.client.mod.recipeviewer.rei.ReiClientEvents
import miragefairy2024.mod.recipeviewer.rei.ReiEvents

@Suppress("unused")
class MirageFairy2024FabricReiClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        ReiClientEvents.onRegisterCategories.fire { it(registry) }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        ReiClientEvents.onRegisterDisplays.fire { it(registry) }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        ReiEvents.onRegisterItemComparators.fire { it(registry) }
    }

    override fun registerScreens(registry: ScreenRegistry) {
        ReiClientEvents.onRegisterScreens.fire { it(registry) }
    }
}
