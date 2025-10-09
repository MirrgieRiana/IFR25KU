package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.widget.FillingArrowWidget
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.api.widget.TextureWidget
import io.wispforest.owo.ui.container.Containers
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterContext
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.NinePatchImageViewRenderer
import miragefairy2024.client.util.OwoComponent
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.fire
import miragefairy2024.util.register
import miragefairy2024.util.toEmiBounds
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack

context(ModContext)
fun initEmiViewPlacers() {
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
        widgets place EmiTextWidget(bounds.offset, view)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ImageView, bounds ->
        widgets place TextureWidget(
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
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: NinePatchImageView, bounds ->
        widgets place EmiViewRendererWidget(NinePatchImageViewRenderer, view, bounds)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ImageButtonView, bounds ->
        widgets place EmiImageButtonWidget(bounds.offset, view)
            .also { it.onClick.register { view.onClick.fire() } }
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
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, _), view, bounds ->
                widgets place EmiViewRendererWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, emiRecipe), view, bounds ->
                widgets place EmiUIAdapter(bounds.toEmiBounds(), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: PlaceableView, size: IntPoint): OwoComponent = adapter.wrap(run {
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
