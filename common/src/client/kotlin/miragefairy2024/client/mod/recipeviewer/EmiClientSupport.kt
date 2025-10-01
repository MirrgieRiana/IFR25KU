package miragefairy2024.client.mod.recipeviewer

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.EmiEvents
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.WidgetProxy
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import mirrg.kotlin.helium.Single
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

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
    }

    EmiEvents.onRegister {
        RecipeViewerEvents.recipeViewerCategoryCards.forEach { card ->
            EmiClientSupport.get(card).register(it)
        }
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

    val emiRecipeCategory: Single<EmiRecipeCategory> by lazy { // 非ロード環境用のSingle
        Single(object : EmiRecipeCategory(card.getId(), EmiStack.of(card.getIcon())) {
            override fun getName() = card.displayName
        })
    }

    fun register(registry: EmiRegistry) {
        registry.addCategory(emiRecipeCategory.first)
        card.recipeEntries.forEach {
            registry.addRecipe(SupportedEmiRecipe(this, it))
        }
    }

}

class SupportedEmiRecipe<R>(val support: EmiClientSupport<R>, val recipeEntry: RecipeViewerCategoryCard.RecipeEntry<R>) : EmiRecipe {
    override fun getCategory() = support.emiRecipeCategory.first
    override fun getId() = recipeEntry.id
    override fun getInputs(): List<EmiIngredient> = support.card.getInputs(recipeEntry).filter { !it.isCatalyst }.map { EmiIngredient.of(it.ingredient) }
    override fun getCatalysts(): List<EmiIngredient> = support.card.getInputs(recipeEntry).filter { it.isCatalyst }.map { EmiIngredient.of(it.ingredient) }
    override fun getOutputs(): List<EmiStack> = support.card.getOutputs(recipeEntry).map { EmiStack.of(it) }
    override fun getDisplayWidth() = 1 + 18 + 4 + 18 * support.card.getOutputs(recipeEntry).size + 1
    override fun getDisplayHeight() = 1 + 18 + 1
    override fun addWidgets(widgets: WidgetHolder) {
        widgets.addSlot(EmiIngredient.of(support.card.getInputs(recipeEntry).single().ingredient), 1, 1)
        support.card.getOutputs(recipeEntry).forEachIndexed { index, itemStack ->
            widgets.addSlot(EmiStack.of(itemStack), 1 + 18 + 4 + 18 * index, 1).recipeContext(this)
        }
    }
}

private fun getEmiWidgetProxy(widgets: WidgetHolder, emiRecipe: EmiRecipe): WidgetProxy {
    return object : WidgetProxy {
        override fun addInputSlotWidget(ingredient: Ingredient, x: Int, y: Int) {
            widgets.addSlot(EmiIngredient.of(ingredient), x, y)
        }

        override fun addCatalystSlotWidget(ingredient: Ingredient, x: Int, y: Int) {
            widgets.addSlot(EmiIngredient.of(ingredient), x, y).catalyst(true)
        }

        override fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int) {
            widgets.addSlot(EmiStack.of(itemStack), x, y).recipeContext(emiRecipe)
        }
    }
}
