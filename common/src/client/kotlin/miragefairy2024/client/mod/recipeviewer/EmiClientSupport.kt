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
import miragefairy2024.mod.HarvestNotationRecipeViewerCategoryCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.EmiEvents
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import net.minecraft.resources.ResourceLocation

context(ModContext)
fun initEmiClientSupport() {
    EmiEvents.onRegister {
        RecipeViewerEvents.informationEntries.forEach { informationEntry ->
            it.addRecipe(
                EmiInfoRecipe(
                    listOf(EmiIngredient.of(informationEntry.input())),
                    listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents,
                    informationEntry.id,
                )
            )
        }

        EmiClientSupport.get(HarvestNotationRecipeViewerCategoryCard).init(it)
    }
}

class EmiClientSupport<R> private constructor(val card: RecipeViewerCategoryCard<R>) {
    companion object {
        private val table = mutableMapOf<RecipeViewerCategoryCard<*>, EmiClientSupport<*>>()
        fun <R> get(card: RecipeViewerCategoryCard<R>): EmiClientSupport<R> {
            @Suppress("UNCHECKED_CAST")
            return table.getOrPut(card) { EmiClientSupport(card) } as EmiClientSupport<R>
        }
    }

    val CATEGORY = EmiRecipeCategory(MirageFairy2024.identifier("harvest"), EmiStack.of(MaterialCard.VEROPEDA_BERRIES.item().createItemStack()))

    inner class Recipe(private val id: ResourceLocation, private val harvestNotation: HarvestNotation) : EmiRecipe {
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
        HarvestNotation.getAll().forEach { (id, harvestNotation) ->
            registry.addRecipe(Recipe(MirageFairy2024.identifier("/harvest/$id"), harvestNotation))
        }
    }

}
