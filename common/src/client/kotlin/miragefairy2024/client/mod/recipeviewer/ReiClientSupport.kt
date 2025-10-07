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
import miragefairy2024.mod.recipeviewer.ImageButtonView
import miragefairy2024.mod.recipeviewer.ImageView
import miragefairy2024.mod.recipeviewer.InputSlotView
import miragefairy2024.mod.recipeviewer.IntPoint
import miragefairy2024.mod.recipeviewer.IntRectangle
import miragefairy2024.mod.recipeviewer.NinePatchImageView
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.PlaceableView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCardRecipeManagerBridge
import miragefairy2024.mod.recipeviewer.RecipeViewerEvents
import miragefairy2024.mod.recipeviewer.ReiSupport
import miragefairy2024.mod.recipeviewer.Remover
import miragefairy2024.mod.recipeviewer.SupportedDisplay
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.ViewPlacerRegistry
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

val REI_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<ReiContainerWidget>()

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
        widgets place Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: CatalystSlotView, bounds ->
        widgets place Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: OutputSlotView, bounds ->
        widgets place Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(view.itemStack.toEntryIngredient())
            .markOutput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: TextView, bounds ->
        widgets place Widgets.createLabel(bounds.offset.toReiPoint(), view.text)
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
        widgets place Widgets.createTexturedWidget(
            view.texture.id,
            bounds.offset.sized(view.texture.bounds.size).toReiRectangle(),
            view.texture.bounds.x.toFloat(),
            view.texture.bounds.y.toFloat(),
            view.texture.bounds.xSize,
            view.texture.bounds.ySize,
            view.texture.size.x,
            view.texture.size.y,
        )
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: NinePatchImageView, bounds ->
        widgets place ViewRendererReiWidget(NinePatchImageViewRenderer, view, bounds)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ImageButtonView, bounds ->
        TODO
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ArrowView, bounds ->
        widgets place Widgets.createArrow(bounds.offset.toReiPoint())
            .animationDurationMS(view.durationMilliSeconds?.toDouble() ?: -1.0)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets place ViewRendererReiWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets place ReiUIAdapter(bounds.toReiRectangle(), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: PlaceableView, size: IntPoint): OwoComponent = adapter.wrap(run {
                            val containerWidget = ReiContainerWidget()
                            REI_VIEW_PLACER_REGISTRY.place(containerWidget, view, IntPoint.ZERO.sized(size))
                            containerWidget.widgets.single() as WidgetWithBounds
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
    private var sizeCache = mutableMapOf<RecipeViewerCategoryCard.RecipeEntry<R>, IntPoint>()

    private fun resetCache() {
        heightCache = 0
        sizeCache.clear()
    }

    private fun getSize(recipeEntry: RecipeViewerCategoryCard.RecipeEntry<R>): IntPoint {
        return sizeCache.getOrPut(recipeEntry) {
            val view = card.createView(recipeEntry)
            val viewWithMinSize = view.calculateMinSize(rendererProxy)
            val viewWithSize = viewWithMinSize.calculateSize(MAX_SIZE)
            viewWithSize.size
        }
    }

    val displayCategory = object : DisplayCategory<SupportedDisplay<R>> {
        override fun getCategoryIdentifier() = ReiSupport.get(card).categoryIdentifier.first
        override fun getTitle(): Component = card.displayName
        override fun getIcon(): Renderer = card.getIcon().toEntryStack()
        override fun getDisplayWidth(display: SupportedDisplay<R>) = 5 + getSize(display.recipeEntry).x + 5
        override fun getDisplayHeight() = 5 + heightCache + 5
        override fun setupDisplay(display: SupportedDisplay<R>, bounds: Rectangle): List<Widget> {
            val containerWidget = ReiContainerWidget()
            val view = card.createView(display.recipeEntry)
            val viewWithMinSize = view.calculateMinSize(rendererProxy)
            val viewWithSize = viewWithMinSize.calculateSize(MAX_SIZE)
            viewWithSize.assemble(IntPoint(bounds.x + 5, bounds.y + 5)) { view2, bounds ->
                REI_VIEW_PLACER_REGISTRY.place(containerWidget, view2, bounds)
            }
            return listOf(
                Widgets.createRecipeBase(bounds),
                containerWidget,
            )
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
            heightCache = heightCache max getSize(it).y
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

// me.shedaniel.rei.impl.client.gui.widget.MergedWidget
class ReiContainerWidget : Widget() {
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

    override fun children() = widgets

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean = widgets.toList().any { it.mouseScrolled(mouseX, mouseY, scrollX, scrollY) }
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = widgets.toList().any { it.keyPressed(keyCode, scanCode, modifiers) }
    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = widgets.toList().any { it.keyReleased(keyCode, scanCode, modifiers) }
    override fun charTyped(codePoint: Char, modifiers: Int): Boolean = widgets.toList().any { it.charTyped(codePoint, modifiers) }
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double) = widgets.toList().any { it.mouseDragged(mouseX, mouseY, button, dragX, dragY) }
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) = widgets.toList().any { it.mouseReleased(mouseX, mouseY, button) }

    override fun containsMouse(mouseX: Double, mouseY: Double) = widgets.any { it.containsMouse(mouseX, mouseY) }
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = widgets.forEach { it.render(guiGraphics, mouseX, mouseY, partialTick) }
    override fun getZRenderingPriority() = widgets.maxOfOrNull { it.zRenderingPriority } ?: 0.0
}

infix fun ReiContainerWidget.place(widget: Widget) = this.add(widget)

class ViewRendererReiWidget<V : PlaceableView>(private val renderer: ViewRenderer<V>, private val view: V, bounds: IntRectangle) : WidgetWithBounds() {
    private val bounds2 = bounds
    private val reiBounds = bounds.toReiRectangle()
    override fun children() = listOf<GuiEventListener>()
    override fun getBounds() = reiBounds
    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = renderer.render(view, bounds2, context, IntPoint(mouseX, mouseY), delta)
}
