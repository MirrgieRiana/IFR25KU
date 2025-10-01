package miragefairy2024.client.fabric

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin
import miragefairy2024.client.mod.rei.ClientReiCategoryCard
import miragefairy2024.fabric.MirageFairy2024FabricReiServerPlugin
import miragefairy2024.mod.RecipeEvents
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text

@Suppress("unused")
class MirageFairy2024FabricReiClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            val category = card.createCategory()
            registry.add(category)
            registry.addWorkstations(category.categoryIdentifier, *card.getWorkstations().toTypedArray())
        }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerDisplays(registry)
        }
        RecipeEvents.informationEntries.forEach {
            BuiltinClientPlugin.getInstance().registerInformation(
                EntryIngredients.ofIngredient(it.input()),
                it.title,
            ) { list -> list.also { list2 -> list2 += listOf(text { "== "() + it.title + " =="() }) + it.contents } }
        }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        MirageFairy2024FabricReiServerPlugin().registerItemComparators(registry)
    }

    override fun registerScreens(registry: ScreenRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerScreens(registry)
        }
    }
}
