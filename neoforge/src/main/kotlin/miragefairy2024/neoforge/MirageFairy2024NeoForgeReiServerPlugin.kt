package miragefairy2024.neoforge

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import me.shedaniel.rei.forge.REIPluginCommon
import miragefairy2024.mod.recipeviewer.rei.ReiEvents

@REIPluginCommon
@Suppress("unused")
class MirageFairy2024NeoForgeReiServerPlugin : REIServerPlugin {
    override fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        ReiEvents.onRegisterDisplaySerializer.fire { it(registry) }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        ReiEvents.onRegisterItemComparators.fire { it(registry) }
    }
}
