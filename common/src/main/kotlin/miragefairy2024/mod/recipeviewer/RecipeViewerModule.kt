package miragefairy2024.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.util.FreezableRegistry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient

object RecipeViewerEvents {
    val informationEntries = FreezableRegistry<InformationEntry>()
    val recipeViewerCategoryCards = FreezableRegistry<RecipeViewerCategoryCard<*>>()
}

class InformationEntry(val input: () -> Ingredient, val title: Component, val contents: List<Component>, val id: ResourceLocation)

context(ModContext)
fun initRecipeViewerModule() {
    initReiSupport()
    initEmiSupport()
}
