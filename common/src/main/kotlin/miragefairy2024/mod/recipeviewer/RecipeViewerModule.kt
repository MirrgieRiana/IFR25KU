package miragefairy2024.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.util.FreezableRegistry
import miragefairy2024.util.plusAssign
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient

object RecipeViewerEvents {
    val informationEntries = FreezableRegistry<InformationEntry>()
    val recipeViewerCategoryCards = FreezableRegistry<RecipeViewerCategoryCard<*>>()
    val itemIdentificationDataComponentTypesList = FreezableRegistry<Pair<() -> Item, () -> List<DataComponentType<*>>>>()
}

class InformationEntry(val input: () -> Ingredient, val title: Component, val contents: List<Component>, val id: ResourceLocation)

context(ModContext)
fun initRecipeViewerModule() {
    initReiSupport()
}

context(ModContext)
@JvmName("registerItemIdentificationDataComponentTypes")
fun (() -> Item).registerIdentificationDataComponentTypes(components: () -> List<DataComponentType<*>>) {
    RecipeViewerEvents.itemIdentificationDataComponentTypesList += Pair(this, components)
}
