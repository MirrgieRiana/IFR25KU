package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.TextWidget
import dev.emi.emi.api.widget.Widget
import dev.emi.emi.api.widget.WidgetHolder
import io.wispforest.owo.ui.container.Containers
import miragefairy2024.ModContext
import miragefairy2024.ReusableInitializationEventRegistry
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.rendererProxy
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.invoke
import miragefairy2024.util.pathString
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack
import mirrg.kotlin.helium.Single
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import java.util.Objects
import io.wispforest.owo.ui.core.Component as OwoComponent

object EmiClientEvents {
    val onRegister = ReusableInitializationEventRegistry<(EmiRegistry) -> Unit>()
}

val EMI_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<Pair<WidgetHolder, EmiRecipe>>()

context(ModContext)
fun initEmiClientSupport() {
    RecipeViewerEvents.informationEntries.subscribe { informationEntry ->
        EmiClientEvents.onRegister {
            it.addRecipe(
                EmiInfoRecipe(
                    listOf(informationEntry.input().toEmiIngredient()),
                    listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents,
                    informationEntry.id,
                )
            )
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCards.subscribe { card ->
        EmiClientEvents.onRegister {
            EmiClientSupport.get(card).register(it)
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges.subscribe { bridge ->
        EmiClientEvents.onRegister { registry ->
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
        EmiClientEvents.onRegister { registry ->
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
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewRendererRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, _), view, x, y ->
                widgets.add(ViewRendererEmiWidget(entry.viewRenderer, view, x, y))
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, emiRecipe), view, x, y ->
                widgets.add(EmiUIAdapter(Bounds(x, y, view.getWidth(), view.getHeight()), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: View): OwoComponent = adapter.wrap(run {
                            val widgets = object : WidgetHolder {
                                val list = mutableListOf<Widget>()
                                override fun getWidth() = view.getWidth()
                                override fun getHeight() = view.getHeight()
                                override fun <T : Widget> add(widget: T): T {
                                    list += widget
                                    return widget
                                }
                            }
                            EMI_VIEW_PLACER_REGISTRY.place(Pair(widgets, emiRecipe), view, 0, 0)
                            widgets.list.single()
                        })
                    }
                    adapter.rootComponent().child(entry.viewOwoAdapter.createOwoComponent(view, context))
                    adapter.prepare()
                })
            }
        }
        f(entry)
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
    val view = support.card.getView(rendererProxy, recipeEntry)
    override fun getDisplayWidth() = 1 + view.getWidth() + 1
    override fun getDisplayHeight() = 1 + view.getHeight() + 1
    override fun addWidgets(widgets: WidgetHolder) {
        view.assemble(1, 1) { view2, x, y ->
            EMI_VIEW_PLACER_REGISTRY.place(Pair(widgets, this), view2, x, y)
        }
    }
}

class ViewRendererEmiWidget<V : View>(private val renderer: ViewRenderer<V>, private val view: V, x: Int, y: Int) : Widget() {
    private val boundsCache by lazy { Bounds(x, y, view.getWidth(), view.getHeight()) }
    override fun getBounds() = boundsCache
    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderer.render(view, boundsCache.x, boundsCache.y, draw, mouseX, mouseY, delta)
    }
}
