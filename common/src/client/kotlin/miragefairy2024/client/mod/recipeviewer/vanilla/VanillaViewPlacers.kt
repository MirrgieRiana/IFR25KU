package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.OutputSlotViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TooltipViewRenderer
import miragefairy2024.client.util.LayeredImageButton
import miragefairy2024.client.util.LayeredImageToggleButton
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.util.Remover
import miragefairy2024.util.fire
import miragefairy2024.util.plus
import miragefairy2024.util.register
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

private infix fun VanillaViewPlacerContext.place(widget: AbstractWidget): ViewController {
    val remover = addRenderableWidget(widget)
    return object : ViewController {
        override fun setOffset(offset: IntPoint) {
            widget.setPosition(offset.x, offset.y)
        }

        override fun detach() = remover.remove()
    }
}

private infix fun VanillaViewPlacerContext.placeRenderable(widget: AbstractWidget): ViewController {
    val remover = addRenderableOnly(widget)
    return object : ViewController {
        override fun setOffset(offset: IntPoint) {
            widget.setPosition(offset.x, offset.y)
        }

        override fun detach() = remover.remove()
    }
}

context(ModContext)
fun initVanillaViewPlacers() {
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: MenuSlotView, bounds ->
        if (context is ScrollViewPlacerContext) {
            val controller = context placeRenderable MenuSlotWidget(view, bounds, context.origin, context.scrollViewWidget)
            return@register object : ViewController by controller {
                override fun detach() = throw UnsupportedOperationException("Cannot detach MenuSlotView from the vanilla screen")
            }
        }
        // バニラはSlotの座標へoriginを描画とホバー判定の際に加算するため、絶対座標を書き込むと二重に加算されるのだ～🌱
        (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(bounds.x - context.origin.x + view.margin.x, bounds.y - context.origin.y + view.margin.y)
        object : ViewController {
            override fun setOffset(offset: IntPoint) {
                (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(offset.x - context.origin.x + view.margin.x, offset.y - context.origin.y + view.margin.y)
            }

            override fun detach() = throw UnsupportedOperationException("Cannot detach MenuSlotView from the vanilla screen")
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextFieldView, bounds ->
        val editBox = EditBox(Minecraft.getInstance().font, bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty())
        editBox.setMaxLength(Int.MAX_VALUE)
        // 初期化による変更通知を拾わないよう、初期値は responder の登録前に設定するのだ～🌱
        editBox.value = view.text.value
        val hint = view.hint
        if (hint != null) editBox.setHint(hint)
        editBox.setResponder {
            view.text.value = it
        }
        val textRemover = view.text.register { _, text ->
            if (editBox.value != text) editBox.value = text
        }
        val controller = context place editBox
        object : ViewController by controller {
            override fun dispose() {
                textRemover.remove()
                controller.dispose()
            }
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds ->
        context place VanillaViewRendererWidget(TextViewRenderer, view, bounds)
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds ->
        context place VanillaViewRendererWidget(ImageViewRenderer, view, bounds)
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TooltipView, bounds ->
        context placeRenderable VanillaViewRendererWidget(TooltipViewRenderer, view, bounds)
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: ScrollView, bounds ->
        val widget = ScrollViewWidget(view, bounds)
        val childContext = object : ScrollViewPlacerContext {
            override val origin = context.origin
            override val scrollViewWidget = widget
            override fun addRenderableWidget(widget: AbstractWidget) = this.scrollViewWidget.addWidget(widget)
            override fun addRenderableOnly(widget: AbstractWidget) = this.scrollViewWidget.addWidget(widget)
        }
        val childController = view.childView.attachTo(IntPoint(bounds.x, bounds.y)) { childView, childBounds ->
            VANILLA_VIEW_PLACER_REGISTRY.place(childContext, childView, childBounds)
        }
        val controller = context place widget
        object : ViewController by controller {
            override fun setOffset(offset: IntPoint) {
                controller.setOffset(offset)
                childController.setOffset(offset)
            }

            override fun detach() {
                childController.detach()
                controller.detach()
            }

            override fun dispose() {
                childController.dispose()
                controller.dispose()
            }
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextButtonView, bounds ->
        val button = Button.builder(view.message) { view.onPress.fire { it() } }
            .bounds(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY)
            .build()
        context place button
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: LayeredImageButtonView, bounds ->
        val removerList = mutableListOf<Remover>()
        val button = if (view.toggle) {
            LayeredImageToggleButton(bounds.sizeX, bounds.sizeY, view.backgroundSprites, view.foregroundSprites).also { toggleButton ->
                toggleButton.value.value = view.value.value
                removerList += view.value.register { _, newValue -> toggleButton.value.value = newValue }
                removerList += toggleButton.value.register { _, newValue -> view.value.value = newValue }
                removerList += toggleButton.onClick.register { view.onPress.fire { it() } }
            }
        } else {
            LayeredImageButton(bounds.sizeX, bounds.sizeY, view.backgroundSprites, view.foregroundSprites).also { imageButton ->
                removerList += imageButton.onClick.register { view.onPress.fire { it() } }
            }
        }
        button.setPosition(bounds.x, bounds.y)
        val controller = context place button
        object : ViewController by controller {
            override fun dispose() {
                removerList.forEach {
                    it.remove()
                }
                controller.dispose()
            }
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: OutputSlotView, bounds ->
        context place VanillaViewRendererWidget(OutputSlotViewRenderer, view, bounds)
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
