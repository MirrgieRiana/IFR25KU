package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import miragefairy2024.ModContext
import miragefairy2024.ReusableInitializationEventRegistry
import miragefairy2024.client.mod.recipeviewer.renderingProxy
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.util.invoke
import miragefairy2024.util.pathString
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack
import mirrg.kotlin.helium.Single
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import java.util.Objects

object EmiClientEvents {
    val onRegister = ReusableInitializationEventRegistry<(EmiRegistry) -> Unit>()

    val onRegisterDefaultComparison = ReusableInitializationEventRegistry<(EmiRegistry) -> Unit>()
    val onRegisterGeneral = ReusableInitializationEventRegistry<(EmiRegistry) -> Unit>()
}

class EmiViewPlacerContext(val widgets: WidgetHolder, val emiRecipe: EmiRecipe)

val EMI_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<EmiViewPlacerContext>()

context(ModContext)
fun initEmiClientSupport() {
    EmiClientEvents.onRegister { register ->
        EmiClientEvents.onRegisterDefaultComparison.fire { it(register) } // 先に登録しないとEmiIngredientで妖精の種類がマージされてしまう
        EmiClientEvents.onRegisterGeneral.fire { it(register) }
    }

    RecipeViewerEvents.informationEntries.subscribe { informationEntry ->
        EmiClientEvents.onRegisterGeneral { registry ->
            registry.addRecipe(
                EmiInfoRecipe(
                    listOf(informationEntry.input().toEmiIngredient()),
                    listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents,
                    informationEntry.id,
                )
            )
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCards.subscribe { card ->
        EmiClientEvents.onRegisterGeneral { registry ->
            EmiClientSupport.get(card).register(registry)
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges.subscribe { bridge ->
        EmiClientEvents.onRegisterGeneral { registry ->
            fun <I : RecipeInput, R : Recipe<I>> f(bridge: RecipeViewerCategoryCardRecipeManagerBridge<I, R>) {
                val support = EmiClientSupport.get(bridge.card)
                registry.recipeManager.getAllRecipesFor(bridge.recipeType).forEach { holder ->
                    if (bridge.recipeClass.isInstance(holder.value())) {
                        val recipeEntry = RecipeViewerCategoryCard.RecipeEntry(holder.id(), holder.value(), false)
                        registry.addRecipe(SupportedEmiRecipe(support, recipeEntry))
                    }
                }
            }
            f(bridge)
        }
    }

    RecipeViewerEvents.itemIdentificationDataComponentTypesList.subscribe { (item, dataComponentTypes) ->
        EmiClientEvents.onRegisterDefaultComparison { registry ->
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
        Single(object : EmiRecipeCategory(card.getId(), card.getIcon().toEmiStack()) {
            override fun getName() = card.displayName
        })
    }

    fun register(registry: EmiRegistry) {
        registry.addCategory(emiRecipeCategory.first)
        card.getWorkstations().forEach {
            registry.addWorkstation(emiRecipeCategory.first, it.toEmiStack())
        }
        card.createRecipeEntries().forEach {
            registry.addRecipe(SupportedEmiRecipe(this, it))
        }
    }

}

class SupportedEmiRecipe<R>(val support: EmiClientSupport<R>, val recipeEntry: RecipeViewerCategoryCard.RecipeEntry<R>) : EmiRecipe {
    override fun getCategory() = support.emiRecipeCategory.first
    override fun getId() = if (recipeEntry.isSynthetic) "/${support.card.getId().pathString}/" * recipeEntry.id else recipeEntry.id
    override fun getInputs(): List<EmiIngredient> = support.card.getInputs(recipeEntry).filter { !it.isCatalyst }.map { it.ingredientStack.toEmiIngredient() }
    override fun getCatalysts(): List<EmiIngredient> = support.card.getInputs(recipeEntry).filter { it.isCatalyst }.map { it.ingredientStack.toEmiIngredient() }
    override fun getOutputs(): List<EmiStack> = support.card.getOutputs(recipeEntry).map { EmiStack.of(it) }

    val sizeCache = run {
        val view = support.card.createView(recipeEntry)
        view.calculateContentSize(renderingProxy)
        view.calculateActualSize()
        view.actualSize
    }

    override fun getDisplayWidth() = 1 + sizeCache.x + 1
    override fun getDisplayHeight() = 1 + sizeCache.y + 1
    override fun addWidgets(widgets: WidgetHolder) {
        val view = support.card.createView(recipeEntry)
        view.calculateContentSize(renderingProxy)
        view.calculateActualSize()
        view.attachTo(IntPoint(1, 1)) { view2, bounds ->
            EMI_VIEW_PLACER_REGISTRY.place(EmiViewPlacerContext(widgets, this), view2, bounds)
        }
    }
}
