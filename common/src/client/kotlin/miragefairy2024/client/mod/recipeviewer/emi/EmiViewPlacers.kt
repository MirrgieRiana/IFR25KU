package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.TextWidget
import dev.emi.emi.api.widget.Widget
import dev.emi.emi.api.widget.WidgetHolder
import io.wispforest.owo.ui.container.Containers
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.util.OwoComponent
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack

context(ModContext)
fun initEmiViewPlacers() {
    EMI_VIEW_PLACER_REGISTRY.register { context, view: InputSlotView, bounds ->
        context.widgets.addSlot(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: CatalystSlotView, bounds ->
        context.widgets.addSlot(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .catalyst(true)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: OutputSlotView, bounds ->
        context.widgets.addSlot(view.itemStack.toEmiStack(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .recipeContext(context.emiRecipe)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds ->
        val widget = context.widgets.addText(view.text, bounds.x, bounds.y, view.color?.lightModeArgb ?: 0xFFFFFFFF.toInt(), view.shadow)
            .let {
                when (view.horizontalAlignment) {
                    Alignment.START -> it.horizontalAlign(TextWidget.Alignment.START)
                    Alignment.CENTER -> it.horizontalAlign(TextWidget.Alignment.CENTER)
                    Alignment.END -> it.horizontalAlign(TextWidget.Alignment.END)
                    null -> it
                }
            }
        val bound = widget.bounds
        if (view.tooltip != null) context.widgets.addTooltipText(view.tooltip!!, bound.x, bound.y, bound.width, bound.height)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds ->
        context.widgets.addTexture(view.textureId, bounds.x, bounds.y, view.bound.sizeX, view.bound.sizeY, view.bound.x, view.bound.y)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: ArrowView, bounds ->
        if (view.durationMilliSeconds != null) {
            context.widgets.addFillingArrow(bounds.x, bounds.y, view.durationMilliSeconds!!)
        } else {
            context.widgets.addTexture(EmiTexture.EMPTY_ARROW, bounds.x, bounds.y)
        }
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.widgets.add(EmiViewRendererWidget(entry.viewRenderer, view, bounds))
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.widgets.add(EmiUIAdapter(Bounds(bounds.x, bounds.y, view.getWidth(), view.getHeight()), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: PlaceableView, size: IntPoint): OwoComponent = adapter.wrap(run {
                            val widgets = object : WidgetHolder {
                                val list = mutableListOf<Widget>()
                                override fun getWidth() = view.getWidth()
                                override fun getHeight() = view.getHeight()
                                override fun <T : Widget> add(widget: T): T {
                                    list += widget
                                    return widget
                                }
                            }
                            EMI_VIEW_PLACER_REGISTRY.place(EmiViewPlacerContext(widgets, context.emiRecipe), view, IntRectangle(0, 0, size.x, size.y))
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
