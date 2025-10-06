package miragefairy2024.client.mod.recipeviewer

import io.wispforest.owo.compat.rei.ReiUIAdapter
import io.wispforest.owo.ui.container.Containers
import me.shedaniel.math.Point
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
import miragefairy2024.client.mod.rei.ClientReiCategoryCard
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.ArrowView
import miragefairy2024.mod.recipeviewer.CatalystSlotView
import miragefairy2024.mod.recipeviewer.ImageView
import miragefairy2024.mod.recipeviewer.InputSlotView
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.ReiSupport
import miragefairy2024.mod.recipeviewer.SupportedDisplay
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.register
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import mirrg.kotlin.helium.max
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
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

    ReiClientEvents.onRegisterCategories {
        ClientReiCategoryCard.entries.forEach { card ->
            val category = card.createCategory()
            it.add(category)
            it.addWorkstations(category.categoryIdentifier, *card.getWorkstations().toTypedArray())
        }
    }
    ReiClientEvents.onRegisterDisplays {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerDisplays(it)
        }
    }
    ReiClientEvents.onRegisterScreens {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerScreens(it)
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

    REI_VIEW_PLACER_REGISTRY.register { widgets, view: InputSlotView, x, y ->
        widgets += Widgets.createSlot(Point(x + view.margin, y + view.margin))
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: CatalystSlotView, x, y ->
        widgets += Widgets.createSlot(Point(x + view.margin, y + view.margin))
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: OutputSlotView, x, y ->
        widgets += Widgets.createSlot(Point(x + view.margin, y + view.margin))
            .entries(view.itemStack.toEntryIngredient())
            .markOutput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: TextView, x, y ->
        widgets += Widgets.createLabel(Point(x, y), view.text)
            .let { if (view.color != null) it.color(view.color!!.lightModeArgb, view.color!!.darkModeArgb) else it }
            .shadow(view.shadow)
            .let {
                when (view.horizontalAlignment) {
                    Alignment.START -> it.leftAligned()
                    Alignment.CENTER -> it.centered()
                    Alignment.END -> it.rightAligned()
                    null -> it.leftAligned()
                }
            }
            .let { if (view.tooltip != null) it.tooltip(*view.tooltip!!.toTypedArray()) else it }
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ImageView, x, y ->
        widgets += Widgets.createTexturedWidget(
            view.textureId,
            Rectangle(x, y, view.bound.width, view.bound.height),
            view.bound.x.toFloat(),
            view.bound.y.toFloat(),
        )
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ArrowView, x, y ->
        widgets += Widgets.createArrow(Point(x, y))
            .animationDurationMS(view.durationMilliSeconds?.toDouble() ?: -1.0)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewRendererRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, x, y ->
                widgets += ViewRendererReiWidget(entry.viewRenderer, view, x, y)
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, x, y ->
                widgets += ReiUIAdapter(Rectangle(x, y, view.getWidth(), view.getHeight()), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: View): OwoComponent = adapter.wrap(run {
                            val widgets = mutableListOf<Widget>()
                            REI_VIEW_PLACER_REGISTRY.place(widgets, view, 0, 0)
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
    }

    private var heightCache = 0

    val displayCategory = object : DisplayCategory<SupportedDisplay<R>> {
        override fun getCategoryIdentifier() = ReiSupport.get(card).categoryIdentifier.first
        override fun getTitle(): Component = card.displayName
        override fun getIcon(): Renderer = card.getIcon().toEntryStack()
        override fun getDisplayWidth(display: SupportedDisplay<R>) = 5 + card.getView(rendererProxy, display.recipeEntry).getWidth() + 5
        override fun getDisplayHeight() = 5 + heightCache + 5
        override fun setupDisplay(display: SupportedDisplay<R>, bounds: Rectangle): List<Widget> {
            val widgets = mutableListOf<Widget>()
            widgets += Widgets.createRecipeBase(bounds)
            val view = card.getView(rendererProxy, display.recipeEntry)
            view.assemble(5 + bounds.x, 5 + bounds.y) { view2, x, y ->
                REI_VIEW_PLACER_REGISTRY.place(widgets, view2, x, y)
            }
            return widgets
        }
    }

    fun registerCategories(registry: CategoryRegistry) {
        registry.add(displayCategory)
        registry.addWorkstations(displayCategory.categoryIdentifier, *card.getWorkstations().map { it.toEntryStack() }.toTypedArray())
    }

    fun registerDisplays(registry: DisplayRegistry) {
        val recipeEntries = card.createRecipeEntries()

        // 高さの事前計算
        heightCache = 0
        recipeEntries.forEach {
            heightCache = heightCache max card.getView(rendererProxy, it).getHeight()
        }

        // レシピ登録
        recipeEntries.forEach {
            registry.add(SupportedDisplay(ReiSupport.get(card), it))
        }
    }

    fun registerScreens(registry: ScreenRegistry) {
        card.getScreenClickAreas().forEach {
            fun <C : AbstractContainerMenu, T : AbstractContainerScreen<C>> f(get: ScreenClassRegistry.ScreenClass<C, T>) {
                val rectangle = Rectangle(it.second.x, it.second.y, it.second.width - 1, it.second.height - 1)
                registry.registerContainerClickArea(rectangle, get.clazz, ReiSupport.get(card).categoryIdentifier.first)
            }
            f(ScreenClassRegistry.get(it.first))
        }
    }

}

class ViewRendererReiWidget<V : View>(private val renderer: ViewRenderer<V>, private val view: V, x: Int, y: Int) : WidgetWithBounds() {
    private val boundsCache by lazy { Rectangle(x, y, view.getWidth(), view.getHeight()) }
    override fun children() = listOf<GuiEventListener>()
    override fun getBounds() = boundsCache
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderer.render(view, boundsCache.x, boundsCache.y, context, mouseX, mouseY, delta)
    }
}
