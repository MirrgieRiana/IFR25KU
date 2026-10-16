package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.ModContext
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.ProgressViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.GhostItemViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.OutputSlotViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TooltipViewRenderer
import miragefairy2024.client.util.LayeredImageButton
import miragefairy2024.client.util.LayeredImageToggleButton
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ButtonView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.ProgressView
import miragefairy2024.mod.recipeviewer.views.GhostItemView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.TextBoxView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.util.Remover
import miragefairy2024.util.emit
import miragefairy2024.util.fire
import miragefairy2024.util.plus
import miragefairy2024.util.register
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

/**
 * Viewを、バニラの画面の子のウィジェットとして置く先なのだ～🌱
 * [miragefairy2024.client.mod.recipeviewer.screen.ScreenViewPlacerContext.scrollViewWidget]は、スクロールの中へ置く場合のみ、そのスクロールなのだ～🌱
 */
class ScreenViewPlacerContext(val widgets: MutableList<AbstractWidget>, val scrollViewWidget: ScrollViewWidget? = null)

val SCREEN_VIEW_PLACER_REGISTRY = ViewPlacerRegistry<ScreenViewPlacerContext>()

private fun ScreenViewPlacerContext.add(widget: AbstractWidget): Remover {
    widgets += widget
    return Remover { widgets -= widget }
}

private fun <V : PlaceableView> ScreenViewPlacerContext.place(renderer: ViewRenderer<V>, view: V, bounds: IntRectangle) = this.add(ScreenViewRendererWidget(renderer, view, bounds))

context(ModContext)
fun initScreenViewPlacers() {
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: ScrollView, bounds ->
        val widget = ScrollViewWidget(view, bounds)
        // 子は、このウィジェットが自分で描くため、画面ではなくこのウィジェットの中へ置くのだ～🌱
        val childContext = ScreenViewPlacerContext(widget.childWidgets, widget)
        val childRemover = view.childView.attachTo(bounds.offset) { childView, childBounds ->
            SCREEN_VIEW_PLACER_REGISTRY.place(childContext, childView, childBounds)
        }
        context.add(widget) + childRemover
    }

    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: MenuSlotView, bounds ->
        val scrollViewWidget = context.scrollViewWidget
        if (scrollViewWidget == null) {
            // スクロールの外は位置が動かないため、1度書き込めば足りるのだ～🌱
            view.apply(bounds.offset, null)
            Remover.NONE
        } else {
            context.add(MenuSlotWidget(view, bounds, scrollViewWidget))
        }
    }

    // EMIとREIは、これらのViewをレシピビューワーのウィジェットへ置き換えるため、バニラの画面だけがここの描画を使うのだ～🌱
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds -> context.place(TextViewRenderer, view, bounds) }
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds -> context.place(ImageViewRenderer, view, bounds) }
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: ProgressView, bounds -> context.place(ProgressViewRenderer, view, bounds) }
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: OutputSlotView, bounds -> context.place(OutputSlotViewRenderer, view, bounds) }
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: GhostItemView, bounds -> context.place(GhostItemViewRenderer, view, bounds) }
    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: TooltipView, bounds -> context.place(TooltipViewRenderer, view, bounds) }

    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: ButtonView, bounds ->
        val button = Button.builder(view.message) { view.onPress.fire { it() } }
            .bounds(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY)
            .build()
        context.add(button)
    }

    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: LayeredImageButtonView, bounds ->
        val button = if (view.toggle) {
            LayeredImageToggleButton(bounds.sizeX, bounds.sizeY, view.backgroundSprites, view.foregroundSprites).also { toggleButton ->
                // 状態は、Viewとウィジェットの双方へ伝えるのだ～🌱
                toggleButton.value.value = view.value.value
                view.value.register { _, newValue -> toggleButton.value.value = newValue }
                toggleButton.value.register { _, newValue -> view.value.value = newValue }
                toggleButton.onClick.register { view.onPress.fire { it() } }
            }
        } else {
            LayeredImageButton(bounds.sizeX, bounds.sizeY, view.backgroundSprites, view.foregroundSprites).also { imageButton ->
                imageButton.onClick.register { view.onPress.fire { it() } }
            }
        }
        button.setPosition(bounds.x, bounds.y)
        context.add(button)
    }

    SCREEN_VIEW_PLACER_REGISTRY.register { context, view: TextBoxView, bounds ->
        val editBox = EditBox(Minecraft.getInstance().font, bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty())
        val hint = view.hint
        if (hint != null) editBox.setHint(hint)
        editBox.value = view.text
        editBox.setResponder { text ->
            view.text = text
            view.onChanged.emit(text)
        }
        context.add(editBox)
    }

    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> register(entry: ViewRendererRegistry.Entry<V>) {
            SCREEN_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context.place(entry.viewRenderer, view, bounds)
            }
        }
        register(entry)
    }
}
