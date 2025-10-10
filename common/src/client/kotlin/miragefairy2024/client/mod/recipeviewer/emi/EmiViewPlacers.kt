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
import miragefairy2024.mod.recipeviewer.view.View
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
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: InputSlotView, x, y ->
        widgets.addSlot(view.ingredientStack.toEmiIngredient(), x - 1 + view.margin, y - 1 + view.margin)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: CatalystSlotView, x, y ->
        widgets.addSlot(view.ingredientStack.toEmiIngredient(), x - 1 + view.margin, y - 1 + view.margin)
            .catalyst(true)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, emiRecipe), view: OutputSlotView, x, y ->
        widgets.addSlot(view.itemStack.toEmiStack(), x - 1 + view.margin, y - 1 + view.margin)
            .recipeContext(emiRecipe)
            .drawBack(view.drawBackground)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: TextView, x, y ->
        val widget = widgets.addText(view.text, x, y, view.color?.lightModeArgb ?: 0xFFFFFFFF.toInt(), view.shadow)
            .let {
                when (view.horizontalAlignment) {
                    Alignment.START -> it.horizontalAlign(TextWidget.Alignment.START)
                    Alignment.CENTER -> it.horizontalAlign(TextWidget.Alignment.CENTER)
                    Alignment.END -> it.horizontalAlign(TextWidget.Alignment.END)
                    null -> it
                }
            }
        val bound = widget.bounds
        if (view.tooltip != null) widgets.addTooltipText(view.tooltip!!, bound.x, bound.y, bound.width, bound.height)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ImageView, x, y ->
        widgets.addTexture(view.textureId, x, y, view.bound.width, view.bound.height, view.bound.x, view.bound.y)
    }
    EMI_VIEW_PLACER_REGISTRY.register { (widgets, _), view: ArrowView, x, y ->
        if (view.durationMilliSeconds != null) {
            widgets.addFillingArrow(x, y, view.durationMilliSeconds!!)
        } else {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, x, y)
        }
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewRendererRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, _), view, x, y ->
                widgets.add(EmiViewRendererWidget(entry.viewRenderer, view, x, y))
            }
        }
        f(entry)
    }
    ViewOwoAdapterRegistry.registry.subscribe { entry ->
        fun <V : View> f(entry: ViewOwoAdapterRegistry.Entry<V>) {
            EMI_VIEW_PLACER_REGISTRY.register(entry.viewClass) { (widgets, emiRecipe), view, x, y ->
                widgets.add(EmiUIAdapter(Bounds(x, y, view.getWidth(), view.getHeight()), Containers::stack).also { adapter ->
                    //adapter.rootComponent().allowOverflow(true)
                    val context = object : ViewOwoAdapterContext {
                        override fun prepare() = adapter.prepare()
                        override fun wrap(view: View): OwoComponent = adapter.wrap(run {
                            val widgets = object : WidgetHolder {
                                val list = mutableListOf<Widget>()
                                override fun getWidth() = view.getWidth()
                                override fun getHeight() = view.getHeight()
                                override fun <T : Widget> add(widget: T): T {
                                    list += widget
                                    return widget
                                }
                            }
                            EMI_VIEW_PLACER_REGISTRY.place(Pair(widgets, emiRecipe), view, 0, 0)
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
