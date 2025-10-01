package miragefairy2024.client.mod.recipeviewer

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.HarvestNotation
import miragefairy2024.mod.RecipeEvents
import miragefairy2024.mod.harvestNotations
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.EmiEvents
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import net.minecraft.resources.ResourceLocation

context(ModContext)
fun initEmiClientSupport() {
    EmiEvents.onRegister {
        RecipeEvents.informationEntries.forEach { informationEntry ->
            it.addRecipe(
                EmiInfoRecipe(
                    listOf(EmiIngredient.of(informationEntry.input())),
                    listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents,
                    informationEntry.id,
                )
            )
        }

        HarvestEmiCard.init(it)
    }
}

object HarvestEmiCard {

    val CATEGORY = EmiRecipeCategory(MirageFairy2024.identifier("harvest"), EmiStack.of(MaterialCard.VEROPEDA_BERRIES.item().createItemStack()))

    class Recipe(private val id: ResourceLocation, private val harvestNotation: HarvestNotation) : EmiRecipe {
        override fun getId() = id
        override fun getCategory() = CATEGORY
        override fun getInputs(): List<EmiIngredient> = listOf(EmiStack.of(harvestNotation.seed))
        override fun getOutputs(): List<EmiStack> = harvestNotation.crops.map { EmiStack.of(it) }
        override fun getDisplayWidth() = 1 + 18 + 4 + 18 * harvestNotation.crops.size + 1
        override fun getDisplayHeight() = 1 + 18 + 1
        override fun addWidgets(widgets: WidgetHolder) {
            widgets.addSlot(EmiStack.of(harvestNotation.seed), 1, 1)
            harvestNotation.crops.forEachIndexed { index, itemStack ->
                widgets.addSlot(EmiStack.of(itemStack), 1 + 18 + 4 + 18 * index, 1).recipeContext(this)
            }
        }
    }

    fun init(registry: EmiRegistry) {
        registry.addCategory(CATEGORY)
        harvestNotations.withIndex().forEach { (index, harvestNotation) ->
            registry.addRecipe(Recipe(MirageFairy2024.identifier("/harvest/$index"), harvestNotation))
        }
    }

}
