package miragefairy2024.client.mod.recipeviewer.rei

import io.wispforest.owo.compat.rei.ReiUIAdapter
import io.wispforest.owo.ui.container.Containers
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.util.OwoComponent
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.toEntryIngredient

context(ModContext)
fun initReiViewPlacers() {
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: InputSlotView, bounds ->
        widgets += Widgets.createSlot(Point(bounds.x + view.margin, bounds.y + view.margin))
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: CatalystSlotView, bounds ->
        widgets += Widgets.createSlot(Point(bounds.x + view.margin, bounds.y + view.margin))
            .entries(view.ingredientStack.toEntryIngredient())
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: OutputSlotView, bounds ->
        widgets += Widgets.createSlot(Point(bounds.x + view.margin, bounds.y + view.margin))
            .entries(view.itemStack.toEntryIngredient())
            .markOutput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: TextView, bounds ->
        widgets += Widgets.createLabel(Point(bounds.x, bounds.y), view.text)
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
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ImageView, bounds ->
        widgets += Widgets.createTexturedWidget(
            view.textureId,
            Rectangle(bounds.x, bounds.y, view.bound.sizeX, view.bound.sizeY),
            view.bound.x.toFloat(),
            view.bound.y.toFloat(),
        )
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ArrowView, bounds ->
        widgets += Widgets.createArrow(Point(bounds.x, bounds.y))
            .animationDurationMS(view.durationMilliSeconds?.toDouble() ?: -1.0)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets += ReiViewRendererWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets += ReiUIAdapter(Rectangle(bounds.x, bounds.y, view.getWidth(), view.getHeight()), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: PlaceableView): OwoComponent = adapter.wrap(run {
                            val widgets = mutableListOf<Widget>()
                            REI_VIEW_PLACER_REGISTRY.place(widgets, view, IntRectangle(0, 0, bounds.sizeX, bounds.sizeY))
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
