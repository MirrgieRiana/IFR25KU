package miragefairy2024.client.mod.recipeviewer.emi

import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.widget.FillingArrowWidget
import dev.emi.emi.api.widget.SlotWidget
import dev.emi.emi.api.widget.TextureWidget
import dev.emi.emi.api.widget.TooltipWidget
import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ArrowView
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.InputSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.util.toEmiIngredient
import miragefairy2024.util.toEmiStack
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

context(ModContext)
fun initEmiViewPlacers() {
    EMI_VIEW_PLACER_REGISTRY.register { context, view: InputSlotView, bounds ->
        context.containerWidget place SlotWidget(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .drawBack(view.drawBackground)
            .also { widget ->
                view.additionalTooltip.forEach {
                    widget.appendTooltip(it)
                }
            }
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: CatalystSlotView, bounds ->
        context.containerWidget place SlotWidget(view.ingredientStack.toEmiIngredient(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .catalyst(true)
            .drawBack(view.drawBackground)
            .also { widget ->
                view.additionalTooltip.forEach {
                    widget.appendTooltip(it)
                }
            }
    }
    EMI_VIEW_PLACER_REGISTRY.register { context, view: OutputSlotView, bounds ->
        context.widgets += SlotWidget(view.itemStack.toEmiStack(), bounds.x - 1 + view.margin, bounds.y - 1 + view.margin)
            .recipeContext(context.emiRecipe)
            .drawBack(view.drawBackground)
            .also { widget ->
                view.additionalTooltip.forEach {
                    widget.appendTooltip(it)
                }
            }
        object : ViewController {
            override fun setOffset(offset: IntPoint) = throw UnsupportedOperationException("EMI does not support changing view offsets")
            override fun detach() = throw UnsupportedOperationException("Cannot detach OutputSlotWidget from EMI")
        }
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
    EMI_VIEW_PLACER_REGISTRY.register { context, view: TooltipView, bounds ->
        context.containerWidget place TooltipWidget({ mouseX, mouseY ->
            view.tooltipProvider(mouseX, mouseY).map { ClientTooltipComponent.create(it.visualOrderText) }
        }, bounds.x, bounds.y, bounds.sizeX, bounds.sizeY)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.containerWidget place EmiViewRendererWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
}
