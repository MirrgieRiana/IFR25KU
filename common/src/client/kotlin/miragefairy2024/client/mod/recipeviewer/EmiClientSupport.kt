package miragefairy2024.client.mod.recipeviewer

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.WidgetProxy
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toEmiIngredient
import mirrg.kotlin.helium.Single
import net.minecraft.world.item.ItemStack
import java.util.Objects

object EmiClientEvents {
    val onRegister = InitializationEventRegistry<(EmiRegistry) -> Unit>()
}

context(ModContext)
fun initEmiClientSupport() {
    EmiClientEvents.onRegister {
        RecipeViewerEvents.informationEntries.freezeAndGet().forEach { informationEntry ->
            it.addRecipe(
                EmiInfoRecipe(
                    listOf(EmiIngredient.of(informationEntry.input())),
                    listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents,
                    informationEntry.id,
                )
            )
        }
    }

    EmiClientEvents.onRegister {
        RecipeViewerEvents.recipeViewerCategoryCards.freezeAndGet().forEach { card ->
            EmiClientSupport.get(card).register(it)
        }
    }

    EmiClientEvents.onRegister { registry ->
        RecipeViewerEvents.itemIdentificationDataComponentTypesList.freezeAndGet().forEach { (item, dataComponentTypes) ->
            registry.setDefaultComparison(
                item(),
                Comparison.of(
                    { a, b -> dataComponentTypes().all { a[it] == b[it] } },
                    { itemStack -> Objects.hash(*dataComponentTypes().map { itemStack[it] }.toTypedArray()) }
                ),
            )
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
        card.getWorkstations().forEach {
            registry.addWorkstation(emiRecipeCategory.first, EmiStack.of(it))
        }
        card.recipeEntries.forEach {
            registry.addRecipe(SupportedEmiRecipe(this, it))
        }
    }

}

class SupportedEmiRecipe<R>(val support: EmiClientSupport<R>, val recipeEntry: RecipeViewerCategoryCard.RecipeEntry<R>) : EmiRecipe {
    override fun getCategory() = support.emiRecipeCategory.first
    override fun getId() = if (recipeEntry.isSynthetic) "/" * recipeEntry.id else recipeEntry.id
    override fun getInputs(): List<EmiIngredient> = support.card.getInputs(recipeEntry).filter { !it.isCatalyst }.map { it.ingredientStack.toEmiIngredient() }
    override fun getCatalysts(): List<EmiIngredient> = support.card.getInputs(recipeEntry).filter { it.isCatalyst }.map { it.ingredientStack.toEmiIngredient() }
    override fun getOutputs(): List<EmiStack> = support.card.getOutputs(recipeEntry).map { EmiStack.of(it) }
    val view = support.card.getView(rendererProxy, recipeEntry)
    override fun getDisplayWidth() = 1 + view.getWidth() + 1
    override fun getDisplayHeight() = 1 + view.getHeight() + 1
    override fun addWidgets(widgets: WidgetHolder) {
        view.addWidgets(getEmiWidgetProxy(widgets, this), 1, 1)
    }
}

private fun getEmiWidgetProxy(widgets: WidgetHolder, emiRecipe: EmiRecipe): WidgetProxy {
    return object : WidgetProxy {
        override fun addInputSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int, drawBackground: Boolean) {
            widgets.addSlot(ingredientStack.toEmiIngredient(), x, y)
                .drawBack(drawBackground)
        }

        override fun addCatalystSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int, drawBackground: Boolean) {
            widgets.addSlot(ingredientStack.toEmiIngredient(), x, y)
                .catalyst(true)
                .drawBack(drawBackground)
        }

        override fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int, drawBackground: Boolean) {
            widgets.addSlot(EmiStack.of(itemStack), x, y)
                .recipeContext(emiRecipe)
                .drawBack(drawBackground)
        }
    }
}
