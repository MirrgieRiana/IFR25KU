package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import net.minecraft.client.gui.components.AbstractWidget

private infix fun VanillaViewPlacerContext.place(widget: AbstractWidget): ViewController {
    val remover = addRenderableWidget(widget)
    return object : ViewController {
        override fun setOffset(offset: IntPoint) {
            widget.setPosition(offset.x, offset.y)
        }

        override fun remove() = remover.remove()
    }
}

context(ModContext)
fun initVanillaViewPlacers() {
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: MenuSlotView, bounds ->
        // バニラはSlotの座標へoriginを描画とホバー判定の際に加算するため、絶対座標を書き込むと二重に加算されるのだ～🌱
        (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(bounds.x - context.origin.x + view.margin.x, bounds.y - context.origin.y + view.margin.y)
        object : ViewController {
            override fun setOffset(offset: IntPoint) {
                (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(offset.x - context.origin.x + view.margin.x, offset.y - context.origin.y + view.margin.y)
            }

            override fun remove() = throw UnsupportedOperationException("Cannot remove MenuSlotView from the vanilla screen")
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds ->
        context place VanillaViewRendererWidget(TextViewRenderer, view, bounds)
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds ->
        context place VanillaViewRendererWidget(ImageViewRenderer, view, bounds)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            VANILLA_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context place VanillaViewRendererWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
}
