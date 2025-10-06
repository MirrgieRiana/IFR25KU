package miragefairy2024.client.mod.recipeviewer

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.TextWidget
import dev.emi.emi.api.widget.WidgetHolder
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.ArrowView
import miragefairy2024.mod.recipeviewer.CatalystSlotView
import miragefairy2024.mod.recipeviewer.ImageView
import miragefairy2024.mod.recipeviewer.InputSlotView
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.place
import miragefairy2024.mod.recipeviewer.register
import miragefairy2024.util.invoke
import miragefairy2024.util.pathString
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack
import mirrg.kotlin.helium.Single
import java.util.Objects

object EmiClientEvents {
    val onRegister = InitializationEventRegistry<(EmiRegistry) -> Unit>()
}

val EMI_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<Pair<WidgetHolder, EmiRecipe>>()

context(ModContext)
fun initEmiClientSupport() {
    EmiClientEvents.onRegister {
        RecipeViewerEvents.informationEntries.freezeAndGet().forEach { informationEntry ->
            it.addRecipe(
                EmiInfoRecipe(
                    listOf(informationEntry.input().toEmiIngredient()),
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

    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: InputSlotView, x, y ->
        widgets.addSlot(view.ingredientStack.toEmiIngredient(), x - 1 + view.margin, y - 1 + view.margin)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: CatalystSlotView, x, y ->
        widgets.addSlot(view.ingredientStack.toEmiIngredient(), x - 1 + view.margin, y - 1 + view.margin)
            .catalyst(true)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, emiRecipe), view: OutputSlotView, x, y ->
        widgets.addSlot(view.itemStack.toEmiStack(), x - 1 + view.margin, y - 1 + view.margin)
            .recipeContext(emiRecipe)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: TextView, x, y ->
        val widget = widgets.addText(view.text, x, y, view.color?.lightModeArgb ?: 0xFFFFFFFF.toInt(), view.shadow)
            .let {
                when (view.horizontalAlignment) {
                    Alignment.START -> it.horizontalAlign(TextWidget.Alignment.START)
                    Alignment.CENTER -> it.horizontalAlign(TextWidget.Alignment.CENTER)
                    Alignment.END -> it.horizontalAlign(TextWidget.Alignment.END)
                    null -> it
                }
            }
        val bound = widget.bounds
        if (view.tooltip != null) widgets.addTooltipText(view.tooltip!!, bound.x, bound.y, bound.width, bound.height)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ImageView, x, y ->
        widgets.addTexture(view.textureId, x, y, view.bound.width, view.bound.height, view.bound.x, view.bound.y)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ArrowView, x, y ->
        if (view.durationMilliSeconds != null) {
            widgets.addFillingArrow(x, y, view.durationMilliSeconds!!)
        } else {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, x, y)
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
        card.recipeEntries.forEach {
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
    val view = support.card.getView(rendererProxy, recipeEntry)
    override fun getDisplayWidth() = 1 + view.getWidth() + 1
    override fun getDisplayHeight() = 1 + view.getHeight() + 1
    override fun addWidgets(widgets: WidgetHolder) {
        view.assemble(1, 1) { view2, x, y ->
            EMI_VIEW_PLACER_REGISTRY.place(Pair(widgets, this), view2, x, y)
        }
    }
}
