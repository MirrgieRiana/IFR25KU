package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.mod.recipeviewer.RenderOverflow
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.ViewRendererRegistry
import miragefairy2024.client.mod.recipeviewer.common.ImageViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TextViewRenderer
import miragefairy2024.client.mod.recipeviewer.common.TooltipViewRenderer
import miragefairy2024.client.util.LayeredImageButton
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.BeforeRenderView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.LayeredImageButtonView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.TextFieldView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TickView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.mod.recipeviewer.views.XFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.XInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.XScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire
import miragefairy2024.util.observe
import miragefairy2024.util.observeAndInitialize
import miragefairy2024.util.register
import miragefairy2024.util.removeOn
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.network.chat.Component

private infix fun VanillaViewPlacerContext.place(registration: WidgetRegistration): ViewController {
    val onDetached = EventRegistry<() -> Unit>()
    this.addRenderable(registration).removeOn(onDetached)
    return object : ViewController {
        override fun setOffset(offset: IntPoint) {
            registration.widget.setPosition(offset.x, offset.y)
        }

        override fun detach() = onDetached.fire()
    }
}

context(_: ModContext)
fun initVanillaViewPlacers() {
    fun placeScrollContainerView(context: VanillaViewPlacerContext, widget: VanillaScrollContainerViewWidget): ViewController {
        val controller = context place WidgetRegistration(widget, true) { RenderOverflow.NONE }
        // 子の差し替えだけで全長が変わった場合も、描画前に寸法を通知するのだ～🌱
        val boundsRemover = context.screenEventSource.onBeforeRender.register(widget::updateScrollBounds)
        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                controller.setOffset(offset)
            }

            override fun detach() {
                boundsRemover.remove()
                widget.detach()
                controller.detach()
            }

            override fun dispose() {
                boundsRemover.remove()
                widget.dispose()
                controller.dispose()
            }
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: XScrollContainerView, bounds ->
        placeScrollContainerView(context, VanillaXScrollContainerViewWidget(view, bounds, context, VANILLA_VIEW_PLACER_REGISTRY))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: YScrollContainerView, bounds ->
        placeScrollContainerView(context, VanillaYScrollContainerViewWidget(view, bounds, context, VANILLA_VIEW_PLACER_REGISTRY))
    }
    fun placeScrollbarView(context: VanillaViewPlacerContext, widget: VanillaScrollbarViewWidget<*>): ViewController {
        val controller = context place WidgetRegistration(widget, true) { RenderOverflow.NONE }
        return object : ViewController {
            override fun setOffset(offset: IntPoint) {
                controller.setOffset(offset)
            }

            override fun detach() {
                controller.detach()
            }

            override fun dispose() {
                widget.dispose()
                controller.dispose()
            }
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: XFlatScrollbarView, bounds ->
        placeScrollbarView(context, VanillaXFlatScrollbarViewWidget(view, bounds))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: YFlatScrollbarView, bounds ->
        placeScrollbarView(context, VanillaYFlatScrollbarViewWidget(view, bounds))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: XInventoryScrollbarView, bounds ->
        placeScrollbarView(context, VanillaXInventoryScrollbarViewWidget(view, bounds))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: YInventoryScrollbarView, bounds ->
        placeScrollbarView(context, VanillaYInventoryScrollbarViewWidget(view, bounds))
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: MenuSlotView, bounds ->
        if (!context.canPlaceMenuSlots) throw UnsupportedOperationException("MenuSlotView cannot be placed in this context")
        fun setPosition(offset: IntPoint) {
            // バニラはSlotの座標へoriginを描画とホバー判定の際に加算するため、絶対座標を書き込むと二重に加算されるのだ～🌱
            (view.slot as SlotPositionSetter).`mirageFairy2024$setPosition`(offset.x - context.origin.x + view.margin.x, offset.y - context.origin.y + view.margin.y)
        }
        setPosition(bounds.offset)
        val remover = context.registerMenuSlotPlacement(view.slot.index) { null }
        object : ViewController {
            override fun setOffset(offset: IntPoint) = setPosition(offset)
            override fun detach() = remover.remove()
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: BeforeRenderView, _ ->
        val remover = context.screenEventSource.onBeforeRender.register {
            view.onBeforeRender.fire()
        }
        object : ViewController {
            override fun setOffset(offset: IntPoint) = Unit
            override fun detach() = Unit
            override fun dispose() = remover.remove()
        }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TickView, _ ->
        val remover = context.screenEventSource.onTick.register {
            view.onTick.fire()
        }
        object : ViewController {
            override fun setOffset(offset: IntPoint) = Unit
            override fun detach() = Unit
            override fun dispose() = remover.remove()
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
        context place WidgetRegistration(TextButtonWidget(), true) { RenderOverflow.UNBOUNDED }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: LayeredImageButtonView, bounds ->
        fun LayeredImageButtonView.Sprites.toWidgetSprites() = WidgetSprites(this.selected, this.unselected, this.selectedFocused, this.unselectedFocused)

        val widget = LayeredImageButton(bounds.sizeX, bounds.sizeY, view.backgroundSprites?.toWidgetSprites(), view.foregroundSprites?.toWidgetSprites())
        widget.setPosition(bounds.x, bounds.y)
        val onClosed = EventRegistry<() -> Unit>()
        view.selected.observeAndInitialize(onClosed) { _, selected ->
            widget.selected = selected
        }
        view.tooltip.observeAndInitialize(onClosed) { _, tooltip ->
            widget.tooltip = if (tooltip != null) Tooltip.create(tooltip) else null
        }
        widget.onClick.observe(onClosed) {
            view.onClick.fire()
        }
        val controller = context place WidgetRegistration(widget, true) { RenderOverflow.NONE }
        object : ViewController {
            override fun setOffset(offset: IntPoint) {
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
        val controller = context place WidgetRegistration(editBox, true) { RenderOverflow.UNBOUNDED }
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
    fun <V : PlaceableView, S> placeRendererView(context: VanillaViewPlacerContext, renderer: ViewRenderer<V, S>, view: V, bounds: IntRectangle, interactive: Boolean): ViewController {
        val widget = VanillaViewRendererWidget(renderer, view, bounds)
        return context place WidgetRegistration(widget, interactive) { widget.getRenderOverflow() }
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TextView, bounds ->
        placeRendererView(context, TextViewRenderer, view, bounds, true)
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: ImageView, bounds ->
        placeRendererView(context, ImageViewRenderer, view, bounds, true)
    }
    VANILLA_VIEW_PLACER_REGISTRY.register { context, view: TooltipView, bounds ->
        placeRendererView(context, TooltipViewRenderer, view, bounds, false)
    }
    ViewRendererRegistry.registry.subscribe { entry ->
        fun <V : PlaceableView, S> f(entry: ViewRendererRegistry.Entry<V, S>) {
            VANILLA_VIEW_PLACER_REGISTRY.register(entry.viewClass) { context, view, bounds ->
                placeRendererView(context, entry.viewRenderer, view, bounds, true)
            }
        }
        f(entry)
    }
}
