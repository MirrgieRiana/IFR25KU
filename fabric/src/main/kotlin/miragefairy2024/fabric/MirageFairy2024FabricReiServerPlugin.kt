package miragefairy2024.fabric

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import miragefairy2024.mod.recipeviewer.ReiEvents

@Suppress("unused")
class MirageFairy2024FabricReiServerPlugin : REIServerPlugin {
    override fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        ReiEvents.onRegisterDisplaySerializer.fire { it(registry) }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        ReiEvents.onRegisterItemComparators.fire { it(registry) }
    }
}
