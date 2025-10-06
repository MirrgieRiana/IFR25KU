package miragefairy2024.mod.recipeviewer

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.plusAssign
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient

object RecipeViewerEvents {
    val informationEntries = SubscribableBuffer<InformationEntry>()
    val recipeViewerCategoryCards = SubscribableBuffer<RecipeViewerCategoryCard<*>>()
    val itemIdentificationDataComponentTypesList = SubscribableBuffer<Pair<() -> Item, () -> List<DataComponentType<*>>>>()
}

class InformationEntry(val input: () -> Ingredient, val title: Component, val contents: List<Component>, val id: ResourceLocation)

val SECONDS_TRANSLATION = Translation({ MirageFairy2024.identifier("recipe_viewer").toLanguageKey("gui", "seconds").toString() }, EnJa("%s sec", "%s 秒"))

context(ModContext)
fun initRecipeViewerModule() {
    initReiSupport()

    SECONDS_TRANSLATION.enJa()
}

context(ModContext)
@JvmName("registerItemIdentificationDataComponentTypes")
fun (() -> Item).registerIdentificationDataComponentTypes(components: () -> List<DataComponentType<*>>) {
    RecipeViewerEvents.itemIdentificationDataComponentTypesList += Pair(this, components)
}
