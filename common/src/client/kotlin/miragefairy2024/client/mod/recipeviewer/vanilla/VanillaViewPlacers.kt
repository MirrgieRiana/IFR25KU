package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.minus
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.util.Remover

context(ModContext)
fun initVanillaViewPlacers() {
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: MenuSlotView, bounds ->
        view.apply(bounds.offset - context.origin)
        Remover { throw UnsupportedOperationException("Cannot remove MenuSlotView from the vanilla screen") }
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
