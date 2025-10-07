package miragefairy2024.client.mod.recipeviewer

import io.wispforest.owo.compat.rei.ReiUIAdapter
import io.wispforest.owo.ui.container.Containers
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin
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
import miragefairy2024.mod.recipeviewer.ReiSupport
import miragefairy2024.mod.recipeviewer.SupportedDisplay
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.ViewWithSize
import miragefairy2024.mod.recipeviewer.offset
import miragefairy2024.mod.recipeviewer.register
import miragefairy2024.mod.recipeviewer.size
import miragefairy2024.mod.recipeviewer.sized
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toReiPoint
import miragefairy2024.util.toReiRectangle
import mirrg.kotlin.helium.max
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput
import io.wispforest.owo.ui.core.Component as OwoComponent

object ReiClientEvents {
    val onRegisterCategories = ReusableInitializationEventRegistry<(CategoryRegistry) -> Unit>()
    val onRegisterDisplays = ReusableInitializationEventRegistry<(DisplayRegistry) -> Unit>()
    val onRegisterScreens = ReusableInitializationEventRegistry<(ScreenRegistry) -> Unit>()
}

val REI_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<MutableList<Widget>>()

context(ModContext)
fun initReiClientSupport() {
    RecipeViewerEvents.informationEntries.subscribe { informationEntry ->
        ReiClientEvents.onRegisterDisplays {
            BuiltinClientPlugin.getInstance().registerInformation(
                EntryIngredients.ofIngredient(informationEntry.input()),
                informationEntry.title,
            ) { list -> list.also { list2 -> list2 += listOf(text { "== "() + informationEntry.title + " =="() }) + informationEntry.contents } }
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCards.subscribe { card ->
        ReiClientEvents.onRegisterCategories {
            ReiClientSupport.get(card).registerCategories(it)
        }
        ReiClientEvents.onRegisterDisplays {
            ReiClientSupport.get(card).registerDisplays(it)
        }
        ReiClientEvents.onRegisterScreens {
            ReiClientSupport.get(card).registerScreens(it)
        }
    }

    RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges.subscribe { bridge ->
        ReiClientEvents.onRegisterDisplays {
            fun <I : RecipeInput, R : Recipe<I>> f(bridge: RecipeViewerCategoryCardRecipeManagerBridge<I, R>) {
                val support = ReiSupport.get(bridge.card)
                it.registerRecipeFiller(bridge.recipeClass, bridge.recipeType) { holder ->
                    val recipeEntry = RecipeViewerCategoryCard.RecipeEntry(holder.id(), holder.value(), false)
                    SupportedDisplay(support, recipeEntry)
                }
            }
            f(bridge)
        }
    }

    REI_VIEW_PLACER_REGISTRY.register { widgets, view: InputSlotView, bounds ->
        widgets += Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: CatalystSlotView, bounds ->
        widgets += Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: OutputSlotView, bounds ->
        widgets += Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(view.itemStack.toEntryIngredient())
            .markOutput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: TextView, bounds ->
        widgets += Widgets.createLabel(bounds.offset.toReiPoint(), view.text)
            .let { if (view.color != null) it.color(view.color!!.lightModeArgb, view.color!!.darkModeArgb) else it }
            .shadow(view.shadow)
            .let {
                when (view.xAlignment) {
                    Alignment.START -> it.leftAligned()
                    Alignment.CENTER -> it.centered()
                    Alignment.END -> it.rightAligned()
                    null -> it.leftAligned()
                }
            }
            .let { if (view.tooltip != null) it.tooltip(*view.tooltip!!.toTypedArray()) else it }
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ImageView, bounds ->
        widgets += Widgets.createTexturedWidget(
            view.textureId,
            bounds.offset.sized(view.bound.size).toReiRectangle(),
            view.bound.x.toFloat(),
            view.bound.y.toFloat(),
            view.bound.xSize,
            view.bound.ySize,
            view.textureSize.x,
            view.textureSize.y,
        )
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: NinePatchImageView, bounds ->
        widgets += ViewRendererReiWidget(NinePatchImageViewRenderer, view, bounds)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ArrowView, bounds ->
        widgets += Widgets.createArrow(bounds.offset.toReiPoint())
            .animationDurationMS(view.durationMilliSeconds?.toDouble() ?: -1.0)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewRendererRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets += ViewRendererReiWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets += ReiUIAdapter(bounds.toReiRectangle(), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: View, size: IntPoint): OwoComponent = adapter.wrap(run {
                            val widgets = mutableListOf<Widget>()
                            REI_VIEW_PLACER_REGISTRY.place(widgets, view, IntPoint.ZERO.sized(size))
                            widgets.single() as WidgetWithBounds
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

class ReiClientSupport<R> private constructor(val card: RecipeViewerCategoryCard<R>) {
    companion object {
        private val table = mutableMapOf<RecipeViewerCategoryCard<*>, ReiClientSupport<*>>()
        fun <R> get(card: RecipeViewerCategoryCard<R>): ReiClientSupport<R> {
            @Suppress("UNCHECKED_CAST")
            return table.getOrPut(card) { ReiClientSupport(card) } as ReiClientSupport<R>
        }

        // TODO
        val MAX_SIZE = IntPoint(150, 126)
    }

    private var heightCache = 0
    private var viewWithSizeCache = mutableMapOf<RecipeViewerCategoryCard.RecipeEntry<R>, ViewWithSize>()

    private fun resetCache() {
        heightCache = 0
        viewWithSizeCache.clear()
    }

    private fun getViewWithSize(recipeEntry: RecipeViewerCategoryCard.RecipeEntry<R>): ViewWithSize {
        return viewWithSizeCache.getOrPut(recipeEntry) {
            val view = card.createView(recipeEntry)
            val viewWithMinSize = view.calculateMinSize(rendererProxy)
            viewWithMinSize.calculateSize(MAX_SIZE)
        }
    }

    val displayCategory = object : DisplayCategory<SupportedDisplay<R>> {
        override fun getCategoryIdentifier() = ReiSupport.get(card).categoryIdentifier.first
        override fun getTitle(): Component = card.displayName
        override fun getIcon(): Renderer = card.getIcon().toEntryStack()
        override fun getDisplayWidth(display: SupportedDisplay<R>) = 5 + getViewWithSize(display.recipeEntry).size.x + 5
        override fun getDisplayHeight() = 5 + heightCache + 5
        override fun setupDisplay(display: SupportedDisplay<R>, bounds: Rectangle): List<Widget> {
            val widgets = mutableListOf<Widget>()
            widgets += Widgets.createRecipeBase(bounds)
            getViewWithSize(display.recipeEntry).assemble(IntPoint(bounds.x + 5, bounds.y + 5)) { view2, bounds ->
                REI_VIEW_PLACER_REGISTRY.place(widgets, view2, bounds)
            }
            return widgets
        }
    }

    fun registerCategories(registry: CategoryRegistry) {
        registry.add(displayCategory)
        registry.addWorkstations(displayCategory.categoryIdentifier, *card.getWorkstations().map { it.toEntryStack() }.toTypedArray())
    }

    fun registerDisplays(registry: DisplayRegistry) {
        val recipeManager = registry.recipeManager
        val recipeEntries = card.createRecipeEntries()

        resetCache()

        // 高さの事前計算
        RecipeViewerEvents.recipeViewerCategoryCardRecipeManagerBridges.getAllImmediately().forEach { bridge ->
            if (bridge.card === card) {
                fun <I : RecipeInput, R : Recipe<I>> calculateMaxHeight(bridge: RecipeViewerCategoryCardRecipeManagerBridge<I, R>) {
                    recipeManager.getAllRecipesFor(bridge.recipeType).forEach {
                        val recipeEntry = RecipeViewerCategoryCard.RecipeEntry(it.id(), it.value(), false)
                        val view = bridge.card.createView(recipeEntry)
                        val viewWithMinSize = view.calculateMinSize(rendererProxy)
                        val viewWithSize = viewWithMinSize.calculateSize(MAX_SIZE)
                        heightCache = heightCache max viewWithSize.size.y
                    }
                }
                calculateMaxHeight(bridge)
            }
        }
        recipeEntries.forEach {
            heightCache = heightCache max getViewWithSize(it).size.y
        }

        // レシピ登録
        recipeEntries.forEach {
            registry.add(SupportedDisplay(ReiSupport.get(card), it))
        }
    }

    fun registerScreens(registry: ScreenRegistry) {
        card.getScreenClickAreas().forEach {
            fun <C : AbstractContainerMenu, T : AbstractContainerScreen<C>> f(get: ScreenClassRegistry.ScreenClass<C, T>) {
                val rectangle = Rectangle(it.second.x, it.second.y, it.second.xSize - 1, it.second.ySize - 1)
                registry.registerContainerClickArea(rectangle, get.clazz, ReiSupport.get(card).categoryIdentifier.first)
            }
            f(ScreenClassRegistry.get(it.first))
        }
    }

}

class ViewRendererReiWidget<V : View>(private val renderer: ViewRenderer<V>, private val view: V, bounds: IntRectangle) : WidgetWithBounds() {
    private val bounds2 = bounds
    private val reiBounds = bounds.toReiRectangle()
    override fun children() = listOf<GuiEventListener>()
    override fun getBounds() = reiBounds
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = renderer.render(view, bounds2, context, mouseX, mouseY, delta)
}
