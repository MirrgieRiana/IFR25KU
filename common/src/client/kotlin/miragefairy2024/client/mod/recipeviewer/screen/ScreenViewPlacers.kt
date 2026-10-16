package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget

/** Viewを、バニラの画面の子のウィジェットとして置く先なのだ～🌱 */
class ScreenViewPlacerContext(val widgets: MutableList<AbstractWidget>)

val SCREEN_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<ScreenViewPlacerContext>()

context(ModContext)
fun initScreenViewPlacers() {
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: ScrollView, bounds ->
        val widget = ScrollViewWidget(view, bounds)
        // 子は、このウィジェットが自分で描くため、画面ではなくこのウィジェットの中へ置くのだ～🌱
        val childContext = ScreenViewPlacerContext(widget.childWidgets)
        view.childView.attachTo(bounds.offset) { childView, childBounds ->
            SCREEN_VIEW_PLACER_REGISTRY.place(childContext, childView, childBounds)
        }
        context.widgets += widget
        Remover { context.widgets -= widget }
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> register(entry: ViewRendererRegistry.Entry<V>) {
            SCREEN_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                val widget = ScreenViewRendererWidget(entry.viewRenderer, view, bounds)
                context.widgets += widget
                Remover { context.widgets -= widget }
            }
        }
        register(entry)
    }
}
