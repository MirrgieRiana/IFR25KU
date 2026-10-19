package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.mod.recipeviewer.view.PlaceableView

context(ModContext)
fun initVanillaViewPlacers() {
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            VANILLA_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.addRenderableWidget(VanillaViewRendererWidget(entry.viewRenderer, view, bounds))
            }
        }
        f(entry)
    }
}
