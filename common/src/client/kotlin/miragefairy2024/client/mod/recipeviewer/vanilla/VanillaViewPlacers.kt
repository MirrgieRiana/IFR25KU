package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TooltipViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.observeAndInitialize
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
    fun placeScrollView(context: VanillaViewPlacerContext, widget: VanillaScrollViewWidget): ViewController {
        val controller = context place widget
        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                controller.setOffset(offset)
            }

            override fun detach() {
                widget.detach()
                controller.detach()
            }

            override fun dispose() {
                widget.dispose()
                controller.dispose()
            }
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: XScrollView, bounds ->
        placeScrollView(context, VanillaXScrollViewWidget(view, bounds, context, VANILLA_VIEW_PLACER_REGISTRY))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: YScrollView, bounds ->
        placeScrollView(context, VanillaYScrollViewWidget(view, bounds, context, VANILLA_VIEW_PLACER_REGISTRY))
    }
    fun placeScrollbarView(context: VanillaViewPlacerContext, widget: VanillaScrollbarViewWidget): ViewController {
        val controller = context place widget
        return object : ViewController {
            override fun setOffset(offset: IntPoint) = controller.setOffset(offset)
            override fun detach() = controller.detach()
            override fun dispose() {
                widget.dispose()
                controller.dispose()
            }
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: XScrollbarView, bounds ->
        placeScrollbarView(context, VanillaXScrollbarViewWidget(view, bounds))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: YScrollbarView, bounds ->
        placeScrollbarView(context, VanillaYScrollbarViewWidget(view, bounds))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: MenuSlotView, bounds ->
        if (!context.canPlaceMenuSlots) throw UnsupportedOperationException("MenuSlotView cannot be placed in this context")
        // バニラはSlotの座標へoriginを描画とホバー判定の際に加算するため、絶対座標を書き込むと二重に加算されるのだ～🌱
        (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(bounds.x - context.origin.x + view.margin.x, bounds.y - context.origin.y + view.margin.y)
        object : ViewController {
            override fun setOffset(offset: IntPoint) {
                (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(offset.x - context.origin.x + view.margin.x, offset.y - context.origin.y + view.margin.y)
            }

            override fun detach() = throw UnsupportedOperationException("Cannot detach MenuSlotView from the vanilla screen")
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextButtonView, bounds ->
        // TODO onClick の戻り値によるイベントの消費判定と、未消費をバニラの Button の入力処理へ反映する方法は検討が必要なのだ～🌱
        class TextButtonWidget : Button(
            bounds.x,
            bounds.y,
            bounds.sizeX,
            bounds.sizeY,
            view.message,
            { view.onClick.fire { it() } },
            DEFAULT_NARRATION,
        ) {
            override fun getMessage() = view.message
        }
        context place TextButtonWidget()
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextFieldView, bounds ->
        val onClosed = EventRegistry<() -> Unit>()
        val editBox = EditBox(Minecraft.getInstance().font, bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty())
        editBox.setMaxLength(Int.MAX_VALUE)
        // 初期化による変更通知を拾わないよう、初期値は responder の登録前に設定するのだ～🌱
        view.text.observeAndInitialize(onClosed) { _, text ->
            if (editBox.value != text) editBox.value = text
        }
        val hint = view.hint
        if (hint != null) editBox.setHint(hint)
        editBox.setResponder {
            view.text.value = it
        }
        val controller = context place editBox
        object : ViewController {
            override fun setOffset(offset: IntPoint): Unit {
                controller.setOffset(offset)
            }

            override fun detach() {
                controller.detach()
            }

            override fun dispose() {
                onClosed.fire()
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
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView> f(entry: ViewRendererRegistry.Entry<V>) {
            VANILLA_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                context place VanillaViewRendererWidget(entry.viewRenderer, view, bounds)
            }
        }
        f(entry)
    }
}
