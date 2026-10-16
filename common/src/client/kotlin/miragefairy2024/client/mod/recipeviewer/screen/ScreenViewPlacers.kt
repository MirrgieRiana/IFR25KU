package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.minus
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.Remover
import net.minecraft.client.gui.components.AbstractWidget

/**
 * Viewを、バニラの画面の子のウィジェットとして置く先なのだ～🌱
 * [miragefairy2024.client.mod.recipeviewer.screen.ScreenViewPlacerContext.slotOrigin]は、バニラがメニューのスロットの座標の原点として使う点なのだ～🌱
 */
class ScreenViewPlacerContext(val widgets: MutableList<AbstractWidget>, val slotOrigin: IntPoint)

val SCREEN_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<ScreenViewPlacerContext>()

private fun ScreenViewPlacerContext.add(widget: AbstractWidget): Remover {
    widgets += widget
    return Remover { widgets -= widget }
}

private fun <V : PlaceableView> ScreenViewPlacerContext.place(renderer: ViewRenderer<V>, view: V, bounds: IntRectangle) = this.add(ScreenViewRendererWidget(renderer, view, bounds))

context(ModContext)
fun initScreenViewPlacers() {
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: MenuSlotView, bounds ->
        // スクロールの中へ置かないため、位置が動かず、1度書き込めば足りるのだ～🌱
        view.apply(bounds.offset - context.slotOrigin, null)
        Remover.NONE
    }

    // EMIとREIは、これらのViewをレシピビューワーのウィジェットへ置き換えるため、バニラの画面だけがここの描画を使うのだ～🌱
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds -> context.place(TextViewRenderer, view, bounds) }
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds -> context.place(ImageViewRenderer, view, bounds) }

    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> register(entry: ViewRendererRegistry.Entry<V>) {
            SCREEN_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.place(entry.viewRenderer, view, bounds)
            }
        }
        register(entry)
    }
}
