package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.TextView

context(ModContext)
fun initVanillaViewPlacers() {
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds ->
        context.addRenderableWidget(VanillaViewRendererWidget(TextViewRenderer, view, bounds))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds ->
        context.addRenderableWidget(VanillaViewRendererWidget(ImageViewRenderer, view, bounds))
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            VANILLA_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.addRenderableWidget(VanillaViewRendererWidget(entry.viewRenderer, view, bounds))
            }
        }
        f(entry)
    }
}
