package miragefairy2024.client.mod.recipeviewer

import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiInfoRecipe
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.FillingArrowWidget
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.api.widget.TextWidget
import dev.emi.emi.api.widget.TextureWidget
import dev.emi.emi.api.widget.TooltipWidget
import dev.emi.emi.api.widget.Widget
import dev.emi.emi.api.widget.WidgetHolder
import io.wispforest.owo.ui.container.Containers
import miragefairy2024.ModContext
import miragefairy2024.ReusableInitializationEventRegistry
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.ArrowView
import miragefairy2024.mod.recipeviewer.CatalystSlotView
import miragefairy2024.mod.recipeviewer.ImageView
import miragefairy2024.mod.recipeviewer.InputSlotView
import miragefairy2024.mod.recipeviewer.IntPoint
import miragefairy2024.mod.recipeviewer.IntRectangle
import miragefairy2024.mod.recipeviewer.NinePatchImageView
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.Remover
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.register
import miragefairy2024.mod.recipeviewer.sized
import miragefairy2024.util.invoke
import miragefairy2024.util.pathString
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toEmiBounds
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack
import mirrg.kotlin.helium.Single
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import java.util.Objects
import io.wispforest.owo.ui.core.Component as OwoComponent

object EmiClientEvents {
    val onRegister = ReusableInitializationEventRegistry<(EmiRegistry) -> Unit>()
}

val EMI_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<Pair<EmiContainerWidget, EmiRecipe>>()

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

    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: InputSlotView, bounds ->
        widgets place SlotWidget(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: CatalystSlotView, bounds ->
        widgets place SlotWidget(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .catalyst(true)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, emiRecipe), view: OutputSlotView, bounds ->
        widgets place SlotWidget(view.itemStack.toEmiStack(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .recipeContext(emiRecipe)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: TextView, bounds ->
        val removers = mutableListOf<Remover>()

        val textWidget = TextWidget(view.text.visualOrderText, bounds.x, bounds.y, view.color?.lightModeArgb ?: 0xFFFFFFFF.toInt(), view.shadow)
            .let {
                when (view.xAlignment) {
                    Alignment.START -> it.horizontalAlign(TextWidget.Alignment.START)
                    Alignment.CENTER -> it.horizontalAlign(TextWidget.Alignment.CENTER)
                    Alignment.END -> it.horizontalAlign(TextWidget.Alignment.END)
                    null -> it
                }
            }
            .also { removers += widgets place it }
        if (view.tooltip != null) {
            val bound = textWidget.bounds
            TooltipWidget({ _, _ ->
                view.tooltip!!.map { ClientTooltipComponent.create(it.visualOrderText) }
            }, bound.x, bound.y, bound.width, bound.height)
                .also { removers += widgets place it }
        }

        Remover {
            removers.forEach {
                it.remove()
            }
        }
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ImageView, bounds ->
        widgets place TextureWidget(
            view.textureId,
            bounds.x,
            bounds.y,
            view.bound.xSize,
            view.bound.ySize,
            view.bound.x,
            view.bound.y,
            view.bound.xSize,
            view.bound.ySize,
            view.textureSize.x,
            view.textureSize.y,
        )
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: NinePatchImageView, bounds ->
        widgets place ViewRendererEmiWidget(NinePatchImageViewRenderer, view, bounds)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ArrowView, bounds ->
        if (view.durationMilliSeconds != null) {
            widgets place FillingArrowWidget(bounds.x, bounds.y, view.durationMilliSeconds!!)
        } else {
            val emiTexture = EmiTexture.EMPTY_ARROW
            widgets place TextureWidget(
                emiTexture.texture,
                bounds.x,
                bounds.y,
                emiTexture.width,
                emiTexture.height,
                emiTexture.u,
                emiTexture.v,
                emiTexture.regionWidth,
                emiTexture.regionHeight,
                emiTexture.textureWidth,
                emiTexture.textureHeight,
            )
        }
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewRendererRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, _), view, bounds ->
                widgets place ViewRendererEmiWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, emiRecipe), view, bounds ->
                widgets place EmiUIAdapter(bounds.toEmiBounds(), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: View, size: IntPoint): OwoComponent = adapter.wrap(run {
                            val containerWidget = EmiContainerWidget()
                            EMI_VIEW_PLACER_REGISTRY.place(Pair(containerWidget, emiRecipe), view, IntPoint.ZERO.sized(size))
                            containerWidget.widgets.single()
                        })
                    }
                    adapter.rootComponent().child(entry.viewOwoAdapter.createOwoComponent(view, context))
                    adapter.prepare()
                }
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

        // TODO
        val MAX_SIZE = IntPoint(150, 126)
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

    val viewWithSize = run {
        val view = support.card.createView(recipeEntry)
        val viewWithMinSize = view.calculateMinSize(rendererProxy)
        viewWithMinSize.calculateSize(EmiClientSupport.MAX_SIZE)
    }

    override fun getDisplayWidth() = 1 + viewWithSize.size.x + 1
    override fun getDisplayHeight() = 1 + viewWithSize.size.y + 1
    override fun addWidgets(widgets: WidgetHolder) {
        val containerWidget = EmiContainerWidget()
        viewWithSize.assemble(IntPoint(1, 1)) { view2, bounds ->
            EMI_VIEW_PLACER_REGISTRY.place(Pair(containerWidget, this), view2, bounds)
        }
        widgets.add(containerWidget)
    }
}

class EmiContainerWidget : Widget() {
    private class WidgetSlot(val widget: Widget)

    private val widgetSlots = mutableListOf<WidgetSlot>()
    var widgets = listOf<Widget>()

    fun add(widget: Widget): Remover {
        val widgetSlot = WidgetSlot(widget)
        widgetSlots += widgetSlot
        widgets = widgetSlots.map { it.widget }
        return Remover {
            widgetSlots -= widgetSlot
            widgets = widgetSlots.map { it.widget }
        }
    }

    override fun getBounds(): Bounds {
        if (widgets.isEmpty()) return Bounds.EMPTY
        val xMin = widgets.minOfOrNull { it.bounds.x }!!
        val yMin = widgets.minOfOrNull { it.bounds.y }!!
        val xMax = widgets.maxOfOrNull { it.bounds.x + it.bounds.width }!!
        val yMax = widgets.maxOfOrNull { it.bounds.y + it.bounds.height }!!
        return Bounds(xMin, yMin, xMax - xMin, yMax - yMin)
    }

    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = widgets.forEach { it.render(draw, mouseX, mouseY, delta) }
    override fun getTooltip(mouseX: Int, mouseY: Int) = widgets.flatMap { it.getTooltip(mouseX, mouseY) }
    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) = widgets.toList().any { it.mouseClicked(mouseX, mouseY, button) }
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = widgets.toList().any { it.keyPressed(keyCode, scanCode, modifiers) }
}

infix fun EmiContainerWidget.place(widget: Widget) = this.add(widget)

class ViewRendererEmiWidget<V : View>(private val renderer: ViewRenderer<V>, private val view: V, bounds: IntRectangle) : Widget() {
    private val bounds2 = bounds
    private val emiBounds = bounds.toEmiBounds()
    override fun getBounds() = emiBounds
    override fun render(draw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = renderer.render(view, bounds2, draw, IntPoint(mouseX, mouseY), delta)
}
