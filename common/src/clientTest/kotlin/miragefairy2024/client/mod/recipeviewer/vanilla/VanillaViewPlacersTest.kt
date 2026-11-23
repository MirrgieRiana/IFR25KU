package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.util.ViewToast
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.AbsoluteView
import miragefairy2024.mod.recipeviewer.views.BeforeRenderView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.DynamicView
import miragefairy2024.mod.recipeviewer.views.EmptyView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.TickView
import miragefairy2024.mod.recipeviewer.views.XFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.XInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YInventoryScrollbarView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.Remover
import miragefairy2024.util.empty
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.Util
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.toasts.Toast
import net.minecraft.client.gui.components.toasts.ToastComponent
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.lwjgl.glfw.GLFW
import org.mockito.Mockito
import kotlin.math.roundToInt

private class PlacerTestView : FixedWidgetView(IntPoint(100, 100)) {
    var renders = 0
    var disposals = 0
    var detaches = 0
}

private val PLACER_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VanillaViewPlacersTest {
    @BeforeAll
    fun initRegistry() {
        with(ModContext()) {
            initVanillaViewPlacers()
        }
        VANILLA_VIEW_PLACER_REGISTRY.register { context, view: PlacerTestView, bounds ->
            val widget: AbstractWidget = object : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
                override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
                    view.renders += 1
                }
                override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
            }
            val remover = context.addRenderable(WidgetRegistration(widget, true))
            object : ViewController {
                override fun setOffset(offset: IntPoint) = widget.setPosition(offset.x, offset.y)
                override fun detach() {
                    view.detaches += 1
                    remover.remove()
                }
                override fun dispose() {
                    view.disposals += 1
                }
            }
        }
    }

    @Test
    fun `スクロール領域内の View もルートの描画前通知を受けて配置解除で登録を外すのだ`() {
        val calls = mutableListOf<String>()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = false
            override val onBeforeRender = EventRegistry<() -> Unit>()
            override val onTick = EventRegistry<() -> Unit>()
            override fun addRenderable(registration: WidgetRegistration) = Remover.NONE
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
        }
        val scrollContent = YListView()
        scrollContent += PlacerTestView()
        scrollContent += BeforeRenderView { calls += "child" }
        val view = AbsoluteView(IntPoint(100, 100))
        view += BeforeRenderView { calls += "root" }
        view += YScrollContainerView().also { it += scrollContent }
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
        Assertions.assertEquals(100, scrollContent.evaluateContentSizeY())
        val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }

        context.onBeforeRender.fire()
        controller.dispose()
        controller.detach()
        context.onBeforeRender.fire()

        Assertions.assertEquals(listOf("root", "child"), calls)
    }

    @Test
    fun `tick 中に差し替えた View は次の tick から通知を受けるのだ`() {
        val calls = mutableListOf<String>()
        val context = createScrollbarPlacerContext(mutableListOf())
        val dynamicView = DynamicView(TickView { calls += "old" })
        val scrollContent = YListView()
        scrollContent += PlacerTestView()
        scrollContent += dynamicView

        var replaced = false
        val view = AbsoluteView(IntPoint(100, 100))
        view += TickView {
            calls += "root"
            if (!replaced) {
                replaced = true
                dynamicView.content.value = TickView { calls += "new" }
            }
        }
        view += YScrollContainerView().also { it += scrollContent }
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
        val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }

        context.onTick.fire()
        Assertions.assertEquals(listOf("root"), calls)

        context.onTick.fire()
        Assertions.assertEquals(listOf("root", "root", "new"), calls)

        controller.dispose()
        context.onTick.fire()
        Assertions.assertEquals(listOf("root", "root", "new"), calls)
        controller.detach()
    }

    @Test
    fun `配置を許可しないコンテキストでは Slot の座標を書き換える前に拒否するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = false
            override val onBeforeRender = EventRegistry<() -> Unit>()
            override val onTick = EventRegistry<() -> Unit>()
            override fun addRenderable(registration: WidgetRegistration) = throw AssertionError("No widget should be registered")
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
        }
        val slot = Slot(null, 0, 11, 22)
        val view = MenuSlotView(slot)
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })

        val exception = Assertions.assertThrows(UnsupportedOperationException::class.java) {
            view.attachTo(IntPoint(30, 40)) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        }

        Assertions.assertEquals("MenuSlotView cannot be placed in this context", exception.message)
        Assertions.assertEquals(IntPoint(11, 22), IntPoint(slot.x, slot.y))
    }

    @Test
    fun `動的な Slot の初期配置と移動と差し替えを中継するのだ`() {
        val placements = mutableSetOf<Int>()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(5, 10)
            override val canPlaceMenuSlots = true
            override val onBeforeRender = EventRegistry<() -> Unit>()
            override val onTick = EventRegistry<() -> Unit>()
            override fun addRenderable(registration: WidgetRegistration) = throw AssertionError("Slots must not register widgets")
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover {
                Assertions.assertNull(clipBounds())
                Assertions.assertTrue(placements.add(slotId))
                return Remover { Assertions.assertTrue(placements.remove(slotId)) }
            }
        }
        val positions = mutableListOf<IntPoint>()
        val slot: Slot = object : Slot(Mockito.mock(Container::class.java), 0, 0, 0), SlotPositionSetter {
            override fun `mirageFairy2024$setPosition`(x: Int, y: Int) {
                positions += IntPoint(x, y)
            }
        }
        slot.index = 7
        val menuSlotView = MenuSlotView(slot)
        val view = DynamicView(menuSlotView)
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
        val controller = view.attachTo(IntPoint(20, 30)) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        try {
            Assertions.assertEquals(setOf(7), placements)
            controller.setOffset(IntPoint(35, 50))
            Assertions.assertEquals(setOf(7), placements)
            Assertions.assertEquals(listOf(IntPoint(16, 21), IntPoint(31, 41)), positions)

            view.content.value = EmptyView()
            Assertions.assertTrue(placements.isEmpty())

            view.content.value = menuSlotView
            Assertions.assertEquals(setOf(7), placements)
            Assertions.assertEquals(listOf(IntPoint(16, 21), IntPoint(31, 41), IntPoint(31, 41)), positions)
        } finally {
            controller.dispose()
            controller.detach()
        }
        Assertions.assertTrue(placements.isEmpty())
    }

    @Test
    fun `親がスロット配置を許可しない場合は縦横スクロールと入れ子の内側でも拒否するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = false
            override val onBeforeRender = EventRegistry<() -> Unit>()
            override val onTick = EventRegistry<() -> Unit>()
            override fun addRenderable(registration: WidgetRegistration) = throw AssertionError("Placement must fail before widget registration")
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
        }
        listOf(false, true).forEach { horizontal ->
            listOf(false, true).forEach { nested ->
                val slot = Slot(null, 0, 11, 22)
                val view = if (horizontal) XScrollContainerView() else YScrollContainerView()
                view += YListView().also { list ->
                    if (nested) {
                        list += YScrollContainerView().also {
                            it += MenuSlotView(slot)
                        }
                    } else {
                        list += MenuSlotView(slot)
                    }
                }
                view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })

                val exception = Assertions.assertThrows(UnsupportedOperationException::class.java) {
                    view.attachTo(IntPoint.ZERO) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
                }

                Assertions.assertEquals("MenuSlotView cannot be placed in this context", exception.message)
                Assertions.assertEquals(IntPoint(11, 22), IntPoint(slot.x, slot.y))
            }
        }
    }

    @Test
    fun `文字ボタンをバニラへ登録して押下と移動と解除を中継するのだ`() {
        val widgets = mutableListOf<AbstractWidget>()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = false
            override val onBeforeRender = EventRegistry<() -> Unit>()
            override val onTick = EventRegistry<() -> Unit>()
            override fun addRenderable(registration: WidgetRegistration): Remover {
                if (!registration.interactive) throw AssertionError("Buttons must accept input")
                val widget = registration.widget
                widgets += widget
                return Remover { widgets.remove(widget) }
            }
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
        }
        var presses = 0
        val view = TextButtonView(IntPoint(80, 20)).also { it.message = text { "back"() } }
        view.onClick.register {
            presses += 1
            true
        }
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 400 }, { 240 })
        val controller = view.attachTo(IntPoint(10, 30)) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        val button = widgets.single() as Button
        Assertions.assertEquals(IntPoint(10, 30), IntPoint(button.x, button.y))
        Assertions.assertEquals(IntPoint(80, 20), IntPoint(button.width, button.height))
        Assertions.assertEquals(view.message, button.message)

        button.onPress()
        Assertions.assertEquals(1, presses)
        controller.setOffset(IntPoint(40, 50))
        Assertions.assertEquals(IntPoint(40, 50), IntPoint(button.x, button.y))
        controller.dispose()
        Assertions.assertSame(button, widgets.single())
        controller.detach()
        Assertions.assertTrue(widgets.isEmpty())
    }

    @Test
    fun `文字ボタンは配置後の文言変更とクリック通知と移動と解除を反映するのだ`() {
        val widgets = mutableListOf<AbstractWidget>()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(10, 20)
            override val canPlaceMenuSlots = false
            override val onBeforeRender = EventRegistry<() -> Unit>()
            override val onTick = EventRegistry<() -> Unit>()
            override fun addRenderable(registration: WidgetRegistration): Remover {
                if (!registration.interactive) throw AssertionError("Buttons must accept input")
                val widget = registration.widget
                widgets += widget
                return Remover { widgets.remove(widget) }
            }
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
        }
        val initialMessage = text { "initial"() }
        val view = TextButtonView(IntPoint(80, 20)).also { it.message = initialMessage }
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
        val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        val button = widgets.single() as Button
        var clicks = 0
        val remover = view.onClick.register {
            clicks += 1
            true
        }
        try {
            Assertions.assertSame(initialMessage, button.message)
            Assertions.assertEquals(IntPoint(80, 20), IntPoint(button.width, button.height))
            val newMessage = text { "changed"() }
            view.message = newMessage
            Assertions.assertSame(newMessage, button.message)
            button.onPress()
            Assertions.assertEquals(1, clicks)
            remover.remove()
            button.onPress()
            Assertions.assertEquals(1, clicks)
            controller.setOffset(IntPoint(40, 60))
            Assertions.assertEquals(IntPoint(40, 60), IntPoint(button.x, button.y))
        } finally {
            remover.remove()
            controller.dispose()
            controller.detach()
        }
        Assertions.assertTrue(widgets.isEmpty())
    }

    @Test
    fun `本番の縦横スクロールコンテナ登録が子の配置と移動と破棄と解除を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val screenWidgets = mutableListOf<AbstractWidget>()
            val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                override val origin = IntPoint(10, 20)
                override val canPlaceMenuSlots = true
                override val onBeforeRender = EventRegistry<() -> Unit>()
                override val onTick = EventRegistry<() -> Unit>()
                override fun addRenderable(registration: WidgetRegistration): Remover {
                    if (!registration.interactive) throw AssertionError("Scroll containers must accept input")
                    val widget = registration.widget
                    screenWidgets += widget
                    return Remover { screenWidgets.remove(widget) }
                }
                override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
            }
            val leaf = PlacerTestView()
            val view = if (horizontal) XScrollContainerView() else YScrollContainerView()
            view += leaf
            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 50 }, { 50 })
            val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val widget = screenWidgets.single() as VanillaScrollContainerViewWidget
            Assertions.assertEquals(if (horizontal) VanillaXScrollContainerViewWidget::class.java else VanillaYScrollContainerViewWidget::class.java, widget.javaClass)
            val child = widget.children().single() as AbstractWidget
            Assertions.assertEquals(IntPoint(10, 20), IntPoint(child.x, child.y))
            try {
                controller.setOffset(IntPoint(40, 60))
                Assertions.assertEquals(IntPoint(40, 60), IntPoint(widget.x, widget.y))
                Assertions.assertEquals(IntPoint(40, 60), IntPoint(child.x, child.y))
            } finally {
                controller.dispose()
                controller.detach()
            }
            Assertions.assertEquals(1, leaf.disposals)
            Assertions.assertEquals(1, leaf.detaches)
            Assertions.assertFalse(widget.isActive)
            Assertions.assertTrue(widget.children().isEmpty())
            Assertions.assertTrue(screenWidgets.isEmpty())
        }
    }

    private fun createScrollbarPlacerContext(widgets: MutableList<AbstractWidget>): VanillaViewPlacerContext {
        return object : VanillaViewPlacerContext {
            override val origin = IntPoint(10, 20)
            override val canPlaceMenuSlots = false
            override val onBeforeRender = EventRegistry<() -> Unit>()
            override val onTick = EventRegistry<() -> Unit>()
            override fun addRenderable(registration: WidgetRegistration): Remover {
                if (!registration.interactive) throw AssertionError("Scrollbars must accept input")
                val widget = registration.widget
                widgets += widget
                return Remover { widgets.remove(widget) }
            }
            override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?): Remover = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
        }
    }

    @Test
    fun `本番のバー登録が単体の配置と移動と破棄と解除を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val widgets = mutableListOf<AbstractWidget>()
            val context = createScrollbarPlacerContext(widgets)
            val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
            view.contentLength = 300
            view.viewportLength = 100
            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
            val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val widget = widgets.single() as VanillaFlatScrollbarViewWidget
            try {
                Assertions.assertEquals(if (horizontal) VanillaXFlatScrollbarViewWidget::class.java else VanillaYFlatScrollbarViewWidget::class.java, widget.javaClass)
                Assertions.assertEquals(if (horizontal) IntPoint(100, 5) else IntPoint(5, 100), IntPoint(widget.width, widget.height))
                controller.setOffset(IntPoint(40, 60))
                Assertions.assertEquals(IntPoint(40, 60), IntPoint(widget.x, widget.y))
                Assertions.assertTrue(widget.mouseClicked(42.0, 62.0, 0))
                Assertions.assertTrue(widget.hasMouseCapture(0))
            } finally {
                controller.dispose()
                controller.detach()
            }
            Assertions.assertFalse(widget.isActive)
            Assertions.assertFalse(widget.hasMouseCapture(0))
            Assertions.assertTrue(widgets.isEmpty())
        }
    }

    @Test
    fun `本番のインベントリ風スクロールバー登録が配置と解除を中継し再配置で位置を保持するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val widgets = mutableListOf<AbstractWidget>()
            val context = createScrollbarPlacerContext(widgets)
            val view = if (horizontal) XInventoryScrollbarView() else YInventoryScrollbarView()
            view.contentLength = 300
            view.viewportLength = 100
            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
            val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val widget = widgets.single() as VanillaInventoryScrollbarViewWidget
            try {
                Assertions.assertEquals(if (horizontal) VanillaXInventoryScrollbarViewWidget::class.java else VanillaYInventoryScrollbarViewWidget::class.java, widget.javaClass)
                Assertions.assertEquals(if (horizontal) IntPoint(100, 18) else IntPoint(18, 100), IntPoint(widget.width, widget.height))
                controller.setOffset(IntPoint(40, 60))
                Assertions.assertEquals(IntPoint(40, 60), IntPoint(widget.x, widget.y))
                Assertions.assertTrue(widget.mouseClicked(42.0, 62.0, 0))
                Assertions.assertTrue(widget.hasMouseCapture(0))
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
            } finally {
                controller.dispose()
                controller.detach()
            }
            Assertions.assertFalse(widget.isActive)
            Assertions.assertFalse(widget.hasMouseCapture(0))
            Assertions.assertTrue(widgets.isEmpty())

            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 120 }, { 120 })
            val newController = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val newWidget = widgets.single() as VanillaInventoryScrollbarViewWidget
            try {
                newWidget.isFocused = true
                Assertions.assertFalse(newWidget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                Assertions.assertTrue(newWidget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
            } finally {
                newController.dispose()
                newController.detach()
            }
        }
    }

    @Test
    fun `List と Stack に配置した表示領域とバーを本番の登録で接続して再配置するのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(false, true).forEach { overlay ->
                GuiTestEnvironment().use { environment ->
                    val widgets = mutableListOf<AbstractWidget>()
                    val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                        override val origin = IntPoint(10, 20)
                        override val canPlaceMenuSlots = false
                        override val onBeforeRender = EventRegistry<() -> Unit>()
                        override val onTick = EventRegistry<() -> Unit>()
                        override fun addRenderable(registration: WidgetRegistration): Remover {
                            if (!registration.interactive) throw AssertionError("Unexpected render-only widget")
                            val widget = registration.widget
                            widgets += widget
                            return Remover { widgets.remove(widget) }
                        }
                        override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
                    }
                    val leaf = PlacerTestView()
                    val scroll = if (horizontal) XScrollContainerView() else YScrollContainerView()
                    val bar = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                    val scrollOffset = if (horizontal) (scroll as XScrollContainerView).scrollOffsetX else (scroll as YScrollContainerView).scrollOffsetY
                    val barOffset = bar.scrollOffset
                    scrollOffset.value = 25.25
                    scroll += leaf
                    val connection = if (horizontal) connectScrollbar(scroll as XScrollContainerView, bar as XFlatScrollbarView) else connectScrollbar(scroll as YScrollContainerView, bar as YFlatScrollbarView)
                    val root = if (overlay) {
                        StackView().also {
                            it.sizingX = Sizing.FILL
                            it.sizingY = Sizing.FILL
                            it += scroll
                            it += bar
                        }
                    } else {
                        val list = if (horizontal) YListView() else XListView()
                        list.sizingX = Sizing.FILL
                        list.sizingY = Sizing.FILL
                        list.add(Child(ListView.Position().also { it.weight = 1.0 }, scroll))
                        list += bar
                        list
                    }
                    try {
                        listOf(50, 40).forEach { viewportLength ->
                            root.initSizeCalculation(PLACER_RENDERING_PROXY, { if (horizontal || overlay) 50 else 55 }, { if (horizontal && !overlay) viewportLength + 5 else viewportLength })
                            val controller = root.attachTo(context.origin) { view, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds) }
                            val viewport = widgets.filterIsInstance<VanillaScrollContainerViewWidget>().single()
                            val scrollbar = widgets.filterIsInstance<VanillaFlatScrollbarViewWidget>().single()
                            val child = viewport.children().single() as AbstractWidget
                            try {
                                Assertions.assertEquals(100, bar.contentLength)
                                Assertions.assertEquals(if (horizontal) 50 else viewportLength, bar.viewportLength)
                                Assertions.assertEquals(scrollOffset.value, barOffset.value)
                                Assertions.assertEquals(if (horizontal) context.origin.x - scrollOffset.value.roundToInt() else context.origin.x, child.x)
                                barOffset.value = 10.0
                                Assertions.assertEquals(if (horizontal) IntPoint(0, 20) else IntPoint(10, 10), IntPoint(child.x, child.y))
                                Assertions.assertTrue(viewport.mouseScrolled(25.0, 30.0, 0.0, -1.0))
                                Assertions.assertEquals(25.0, barOffset.value)
                                viewport.render(environment.graphics, 25, 30, 0.0F)
                                scrollbar.render(environment.graphics, 25, 30, 0.0F)
                                Assertions.assertTrue(environment.fills.isNotEmpty())
                                val mouseX = scrollbar.x + 1.0
                                val mouseY = scrollbar.y + 1.0
                                Assertions.assertTrue(scrollbar.mouseClicked(mouseX, mouseY, 0))
                                Assertions.assertTrue(scrollbar.mouseDragged(500.0, 500.0, 0, 500.0 - mouseX, 500.0 - mouseY))
                                Assertions.assertEquals(if (horizontal) IntPoint(-40, 20) else IntPoint(10, 20 - (100 - viewportLength)), IntPoint(child.x, child.y))
                                Assertions.assertTrue(scrollbar.mouseReleased(500.0, 500.0, 0))
                                controller.setOffset(IntPoint(30, 40))
                                Assertions.assertEquals(IntPoint(30, 40), IntPoint(viewport.x, viewport.y))
                            } finally {
                                controller.dispose()
                                controller.detach()
                            }
                            Assertions.assertTrue(widgets.isEmpty())
                            Assertions.assertTrue(context.onBeforeRender.toList().isEmpty())
                            Assertions.assertEquals(1, scrollOffset.toList().size)
                        }
                    } finally {
                        connection.remove()
                    }
                    Assertions.assertTrue(scrollOffset.toList().isEmpty())
                    Assertions.assertTrue(barOffset.toList().isEmpty())
                    Assertions.assertTrue(scroll.scrollBounds.toList().isEmpty())
                    Assertions.assertEquals(2, leaf.disposals)
                    Assertions.assertEquals(2, leaf.detaches)
                }
            }
        }
    }

    @Test
    fun `子の差し替えは描画前に寸法だけをバーへ同期するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val widgets = mutableListOf<AbstractWidget>()
                val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                    override val origin = IntPoint(10, 20)
                    override val canPlaceMenuSlots = false
                    override val onBeforeRender = EventRegistry<() -> Unit>()
                    override val onTick = EventRegistry<() -> Unit>()
                    override fun addRenderable(registration: WidgetRegistration): Remover {
                        if (!registration.interactive) throw AssertionError("Unexpected render-only widget")
                        val widget = registration.widget
                        widgets += widget
                        return Remover { widgets.remove(widget) }
                    }
                    override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
                }
                val leaf = PlacerTestView()
                val content = DynamicView(leaf)
                val scroll = if (horizontal) XScrollContainerView() else YScrollContainerView()
                val bar = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                val scrollOffset = if (horizontal) (scroll as XScrollContainerView).scrollOffsetX else (scroll as YScrollContainerView).scrollOffsetY
                val barOffset = bar.scrollOffset
                scroll += content
                scrollOffset.value = 75.25
                val connection = if (horizontal) connectScrollbar(scroll as XScrollContainerView, bar as XFlatScrollbarView) else connectScrollbar(scroll as YScrollContainerView, bar as YFlatScrollbarView)
                scroll.initSizeCalculation(PLACER_RENDERING_PROXY, { 50 }, { 50 })
                val controller = scroll.attachTo(context.origin) { view, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds) }
                val widget = widgets.single() as VanillaScrollContainerViewWidget
                try {
                    Assertions.assertEquals(100, bar.contentLength)
                    content.content.value = SpaceView(IntPoint(25, 25))
                    widget.render(environment.graphics, 20, 30, 0.0F)
                    Assertions.assertEquals(100, bar.contentLength)
                    context.onBeforeRender.fire()
                    Assertions.assertEquals(25, bar.contentLength)
                    Assertions.assertEquals(50, bar.viewportLength)
                    Assertions.assertEquals(75.25, scrollOffset.value)
                    Assertions.assertEquals(75.25, barOffset.value)
                    content.content.value = leaf
                    context.onBeforeRender.fire()
                    Assertions.assertEquals(100, bar.contentLength)
                    val child = widget.children().single() as AbstractWidget
                    Assertions.assertEquals(if (horizontal) IntPoint(-65, 20) else IntPoint(10, -55), IntPoint(child.x, child.y))
                } finally {
                    controller.dispose()
                    controller.detach()
                    connection.remove()
                }
                Assertions.assertTrue(context.onBeforeRender.toList().isEmpty())
            }
        }
    }

    @Test
    fun `領域のスクロール操作で接続先のフラットバーを再表示し破棄時に購読を解除するのだ`() {
        GuiTestEnvironment().use {
            Mockito.mockStatic(Util::class.java, Mockito.CALLS_REAL_METHODS).use { clock ->
                var now = 1000L
                clock.`when`<Long> { Util.getMillis() }.thenAnswer { now }
                val widgets = mutableListOf<AbstractWidget>()
                val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                    override val origin = IntPoint.ZERO
                    override val canPlaceMenuSlots = false
                    override val onBeforeRender = EventRegistry<() -> Unit>()
                    override val onTick = EventRegistry<() -> Unit>()
                    override fun addRenderable(registration: WidgetRegistration): Remover {
                        if (!registration.interactive) throw AssertionError("Unexpected render-only widget")
                        val widget = registration.widget
                        widgets += widget
                        return Remover { widgets.remove(widget) }
                    }
                    override fun registerMenuSlotPlacement(slotId: Int, clipBounds: () -> IntRectangle?) = throw UnsupportedOperationException("Menu slots cannot be registered in this context")
                }
                val scrollView = YScrollContainerView()
                scrollView += SpaceView(IntPoint(100, 300))
                scrollView.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
                val scrollbarView = YFlatScrollbarView()
                scrollbarView.scrollbarColor = 0xA0FFFFFF.toInt()
                val connection = connectScrollbar(scrollView, scrollbarView)
                val scrollController = scrollView.attachTo(context.origin) { view, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, view, bounds) }
                val scrollbarController = VANILLA_VIEW_PLACER_REGISTRY.place(context, scrollbarView, IntRectangle(200, 20, 5, 100))
                val scrollWidget = widgets.filterIsInstance<VanillaYScrollContainerViewWidget>().single()
                val scrollbarWidget = widgets.filterIsInstance<VanillaYFlatScrollbarViewWidget>().single()
                val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)

                fun assertColor(at: Long, color: Int, thumbY: Int) {
                    now = at
                    Mockito.clearInvocations(graphics)
                    scrollbarWidget.render(graphics, 50, 25, 0.0F)
                    Mockito.verify(graphics).fill(200, thumbY, 205, thumbY + 33, color)
                }

                try {
                    Assertions.assertEquals(1, scrollbarView.onScrollInput.toList().size)
                    assertColor(1000L, 0x00FFFFFF, 20)
                    Assertions.assertFalse(scrollWidget.mouseScrolled(50.0, 25.0, 0.0, 1.0))
                    assertColor(1500L, scrollbarView.scrollbarColor, 20)
                    assertColor(1875L, 0x50FFFFFF, 20)
                    assertColor(2250L, 0x00FFFFFF, 20)

                    scrollWidget.isFocused = true
                    Assertions.assertTrue(scrollWidget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                    assertColor(2250L, scrollbarView.scrollbarColor, 87)
                    assertColor(3500L, 0x00FFFFFF, 87)
                    Assertions.assertTrue(scrollWidget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                    assertColor(3500L, scrollbarView.scrollbarColor, 20)
                } finally {
                    scrollbarController.dispose()
                    scrollbarController.detach()
                    scrollController.dispose()
                    scrollController.detach()
                    connection.remove()
                }
                Assertions.assertTrue(scrollbarView.onScrollInput.toList().isEmpty())
            }
        }
    }

    @Test
    fun `トーストは非表示アニメーション中も View を描画し破棄しないのだ`() {
        val minecraft = Mockito.mock(Minecraft::class.java)
        Mockito.`when`(minecraft.timer).thenReturn(DeltaTracker.ZERO)
        val toastComponent = Mockito.mock(ToastComponent::class.java)
        Mockito.`when`(toastComponent.notificationDisplayTimeMultiplier).thenReturn(1.0)
        val graphics = Mockito.mock(GuiGraphics::class.java)
        val view = PlacerTestView()

        Mockito.mockStatic(Minecraft::class.java).use { mock ->
            mock.`when`<Minecraft> { Minecraft.getInstance() }.thenReturn(minecraft)
            val toast = ViewToast(view)

            Assertions.assertEquals(Toast.Visibility.SHOW, toast.render(graphics, toastComponent, 4999L))
            Assertions.assertEquals(Toast.Visibility.HIDE, toast.render(graphics, toastComponent, 5000L))
            Assertions.assertEquals(Toast.Visibility.HIDE, toast.render(graphics, toastComponent, 5001L))
        }

        Assertions.assertEquals(3, view.renders)
        Assertions.assertEquals(0, view.disposals)
    }

}
