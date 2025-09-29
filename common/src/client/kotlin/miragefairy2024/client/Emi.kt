package miragefairy2024.client

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.HarvestNotation
import miragefairy2024.mod.RecipeEvents
import miragefairy2024.mod.harvestNotations
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import net.minecraft.resources.ResourceLocation

fun registerEmi(registry: EmiRegistry) {
    RecipeEvents.informationEntries.forEach {
        registry.addRecipe(
            EmiInfoRecipe(
                listOf(EmiIngredient.of(it.input())),
                listOf(text { "== "() + it.title + " =="() }) + it.contents,
                it.id,
            )
        )
    }

    HarvestEmiCard.init(registry)
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
