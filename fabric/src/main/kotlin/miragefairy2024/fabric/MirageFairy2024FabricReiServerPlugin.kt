package miragefairy2024.fabric

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin as REICommonPlugin // MIGRATION2612 ReplaceImport me.shedaniel.rei.api.common.plugins.REICommonPlugin
import miragefairy2024.mod.recipeviewer.rei.ReiEvents

@Suppress("unused")
class MirageFairy2024FabricReiServerPlugin : REICommonPlugin {
    override fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        ReiEvents.onRegisterDisplaySerializer.fire { it(registry) }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        ReiEvents.onRegisterItemComparators.fire { it(registry) }
    }
}
