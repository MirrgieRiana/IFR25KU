package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.widget.Bounds
import dev.emi.emi.api.widget.FillingArrowWidget
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.api.widget.TextureWidget
import dev.emi.emi.api.widget.Widget
import dev.emi.emi.api.widget.WidgetHolder
import io.wispforest.owo.ui.container.Containers
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.util.OwoComponent
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.Remover
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack

context(ModContext)
fun initEmiViewPlacers() {
    EMI_VIEW_PLACER_REGISTRY.register { context, view: InputSlotView, bounds ->
        context.containerWidget place SlotWidget(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: CatalystSlotView, bounds ->
        context.containerWidget place SlotWidget(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .catalyst(true)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: OutputSlotView, bounds ->
        context.widgets += SlotWidget(view.itemStack.toEmiStack(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .recipeContext(context.emiRecipe)
            .drawBack(view.drawBackground)
        Remover { throw UnsupportedOperationException("Cannot remove OutputSlotWidget from EMI") }
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds ->
        context.containerWidget place EmiTextWidget(bounds.offset, view)
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds ->
        context.containerWidget place TextureWidget(
            view.texture.id,
            bounds.x,
            bounds.y,
            view.texture.bounds.sizeX,
            view.texture.bounds.sizeY,
            view.texture.bounds.x,
            view.texture.bounds.y,
            view.texture.bounds.sizeX,
            view.texture.bounds.sizeY,
            view.texture.size.x,
            view.texture.size.y,
        )
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: ArrowView, bounds ->
        if (view.durationMilliSeconds != null) {
            context.containerWidget place FillingArrowWidget(bounds.x, bounds.y, view.durationMilliSeconds!!)
        } else {
            val emiTexture = EmiTexture.EMPTY_ARROW
            context.containerWidget place TextureWidget(
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
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.containerWidget place EmiViewRendererWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.containerWidget place EmiUIAdapter(Bounds(bounds.x, bounds.y, view.actualSize.x, view.actualSize.y), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: PlaceableView, size: IntPoint): OwoComponent = adapter.wrap(run {
                            val containerWidget = EmiContainerWidget()
                            val context2 = EmiViewPlacerContext(context.widgets, containerWidget, context.emiRecipe)
                            EMI_VIEW_PLACER_REGISTRY.place(context2, view, IntPoint.ZERO.sized(size))
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

private infix fun WidgetHolder.place(widget: Widget) = this.add(widget)
