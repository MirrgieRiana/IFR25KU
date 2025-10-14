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
import miragefairy2024.mod.recipeviewer.view.View
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
            Rectangle(x, y, view.bound.sizeX, view.bound.sizeY),
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
                widgets += ReiViewRendererWidget(entry.viewRenderer, view, x, y)
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
