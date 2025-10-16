package miragefairy2024.client.mod.recipeviewer.rei

import io.wispforest.owo.compat.rei.ReiUIAdapter
import io.wispforest.owo.ui.container.Containers
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.util.OwoComponent
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.view.size
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toReiPoint
import miragefairy2024.util.toReiRectangle

context(ModContext)
fun initReiViewPlacers() {
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
        widgets place ReiTextWidget(bounds.offset, view)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ImageView, bounds ->
        widgets place Widgets.createTexturedWidget(
            view.texture.id,
            bounds.offset.sized(view.texture.bounds.size).toReiRectangle(),
            view.texture.bounds.x.toFloat(),
            view.texture.bounds.y.toFloat(),
            view.texture.bounds.sizeX,
            view.texture.bounds.sizeY,
            view.texture.size.x,
            view.texture.size.y,
        )
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: ArrowView, bounds ->
        widgets place Widgets.createArrow(bounds.offset.toReiPoint())
            .animationDurationMS(view.durationMilliSeconds?.toDouble() ?: -1.0)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets place ReiViewRendererWidget(entry.viewRenderer, view, bounds)
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
