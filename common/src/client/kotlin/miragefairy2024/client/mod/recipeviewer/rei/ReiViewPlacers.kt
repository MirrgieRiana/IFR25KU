package miragefairy2024.client.mod.recipeviewer.rei

import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.contains
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
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toReiPoint
import miragefairy2024.util.toReiRectangle

context(ModContext)
fun initReiViewPlacers() {
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: InputSlotView, bounds ->
        val entryIngredient = view.ingredientStack.toEntryIngredient()
            .let {
                if (view.additionalTooltip.isNotEmpty()) {
                    EntryIngredient.of(it.map { entryStack -> entryStack.tooltip(view.additionalTooltip) })
                } else {
                    it
                }
            }
        widgets place Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(entryIngredient)
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: CatalystSlotView, bounds ->
        val entryIngredient = view.ingredientStack.toEntryIngredient()
            .let {
                if (view.additionalTooltip.isNotEmpty()) {
                    EntryIngredient.of(it.map { entryStack -> entryStack.tooltip(view.additionalTooltip) })
                } else {
                    it
                }
            }
        widgets place Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(entryIngredient)
            .markInput()
            .backgroundEnabled(view.drawBackground)
    }
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: OutputSlotView, bounds ->
        val entryIngredient = view.itemStack.toEntryIngredient()
            .let {
                if (view.additionalTooltip.isNotEmpty()) {
                    EntryIngredient.of(it.map { entryStack -> entryStack.tooltip(view.additionalTooltip) })
                } else {
                    it
                }
            }
        widgets place Widgets.createSlot(bounds.offset.offset(view.margin, view.margin).toReiPoint())
            .entries(entryIngredient)
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
    REI_VIEW_PLACER_REGISTRY.register { widgets, view: TooltipView, bounds ->
        widgets place Widgets.createTooltip { mouse ->
            if (bounds.contains(mouse.x, mouse.y)) Tooltip.create(mouse, view.tooltipProvider(mouse.x, mouse.y)) else null
        }
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            REI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { widgets, view, bounds ->
                widgets place ReiViewRendererWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
}
