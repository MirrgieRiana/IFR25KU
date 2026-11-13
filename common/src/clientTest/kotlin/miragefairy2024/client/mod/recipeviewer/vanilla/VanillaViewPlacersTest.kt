package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.connectScrollbar
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.ListView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import miragefairy2024.util.empty
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.inventory.Slot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private class PlacerTestView : FixedWidgetView(IntPoint(100, 100)) {
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
                override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
                override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
            }
            val remover = context.addRenderableWidget(widget)
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
    fun `配置を許可しないコンテキストでは Slot の座標を書き換える前に拒否するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = false
            override fun addRenderableWidget(widget: AbstractWidget) = throw AssertionError("No widget should be registered")
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("No widget should be registered")
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
    fun `配置を許可するコンテキストでは Slot の初期配置と移動を中継するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(5, 10)
            override val canPlaceMenuSlots = true
            override fun addRenderableWidget(widget: AbstractWidget) = throw AssertionError("Slots must not register widgets")
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Slots must not register widgets")
        }
        val positions = mutableListOf<IntPoint>()
        val slot: Slot = object : Slot(null, 0, 0, 0), SlotPositionSetter {
            override fun `mirageFairy2024$setPosition`(x: Int, y: Int) {
                positions += IntPoint(x, y)
            }
        }
        val view = MenuSlotView(slot)
        view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
        val controller = view.attachTo(IntPoint(20, 30)) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
        controller.setOffset(IntPoint(35, 50))

        Assertions.assertEquals(listOf(IntPoint(16, 21), IntPoint(31, 41)), positions)
        controller.dispose()
    }

    @Test
    fun `親で Slot を配置できても縦横スクロールと入れ子の内側では拒否するのだ`() {
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint.ZERO
            override val canPlaceMenuSlots = true
            override fun addRenderableWidget(widget: AbstractWidget) = throw AssertionError("Placement must fail before widget registration")
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Placement must fail before widget registration")
        }
        listOf(false, true).forEach { horizontal ->
            listOf(false, true).forEach { nested ->
                val slot = Slot(null, 0, 11, 22)
                val view = if (horizontal) XScrollView() else YScrollView()
                view += YListView().also { list ->
                    if (nested) {
                        list += YScrollView().also {
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
            override fun addRenderableWidget(widget: AbstractWidget): Remover {
                widgets += widget
                return Remover { widgets.remove(widget) }
            }
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Buttons must accept input")
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
    fun `本番の縦横スクロール登録が子の配置と移動と破棄と解除を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val screenWidgets = mutableListOf<AbstractWidget>()
            val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                override val origin = IntPoint(10, 20)
                override val canPlaceMenuSlots = true
                override fun addRenderableWidget(widget: AbstractWidget): Remover {
                    screenWidgets += widget
                    return Remover { screenWidgets.remove(widget) }
                }
                override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Scroll containers must accept input")
            }
            val leaf = PlacerTestView()
            val view = if (horizontal) XScrollView() else YScrollView()
            view += leaf
            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 50 }, { 50 })
            val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val widget = screenWidgets.single() as VanillaScrollViewWidget
            Assertions.assertEquals(if (horizontal) VanillaXScrollViewWidget::class.java else VanillaYScrollViewWidget::class.java, widget.javaClass)
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

    @Test
    fun `文字ボタンは配置後の文言変更とクリック通知と移動と解除を反映するのだ`() {
        val widgets = mutableListOf<AbstractWidget>()
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(10, 20)
            override val canPlaceMenuSlots = false
            override fun addRenderableWidget(widget: AbstractWidget): Remover {
                widgets += widget
                return Remover { widgets.remove(widget) }
            }
            override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Buttons must accept input")
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
    fun `本番の縦横シンプルスクロール登録が子の配置と移動と破棄と解除を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val screenWidgets = mutableListOf<AbstractWidget>()
            val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                override val origin = IntPoint(10, 20)
                override val canPlaceMenuSlots = true
                override fun addRenderableWidget(widget: AbstractWidget): Remover {
                    screenWidgets += widget
                    return Remover { screenWidgets.remove(widget) }
                }
                override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Scroll containers must accept input")
            }
            val leaf = PlacerTestView()
            val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
            view += leaf
            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 50 }, { 50 })
            val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val widget = screenWidgets.single() as VanillaSimpleScrollViewWidget
            Assertions.assertEquals(if (horizontal) VanillaXSimpleScrollViewWidget::class.java else VanillaYSimpleScrollViewWidget::class.java, widget.javaClass)
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

    @Test
    fun `本番のバー登録が単体の配置と移動と破棄と解除を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val widgets = mutableListOf<AbstractWidget>()
            val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                override val origin = IntPoint(10, 20)
                override val canPlaceMenuSlots = false
                override fun addRenderableWidget(widget: AbstractWidget): Remover {
                    widgets += widget
                    return Remover { widgets.remove(widget) }
                }
                override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Scrollbars must accept input")
            }
            val view = if (horizontal) XScrollbarView() else YScrollbarView()
            view.contentLength = 300
            view.viewportLength = 100
            view.initSizeCalculation(PLACER_RENDERING_PROXY, { 100 }, { 100 })
            val controller = view.attachTo(context.origin) { child, bounds -> VANILLA_VIEW_PLACER_REGISTRY.place(context, child, bounds) }
            val widget = widgets.single() as VanillaScrollbarViewWidget
            try {
                Assertions.assertEquals(if (horizontal) VanillaXScrollbarViewWidget::class.java else VanillaYScrollbarViewWidget::class.java, widget.javaClass)
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
    fun `List と Stack に配置した表示領域とバーを本番の登録で接続して再配置するのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(false, true).forEach { overlay ->
                GuiTestEnvironment().use { environment ->
                    val widgets = mutableListOf<AbstractWidget>()
                    val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
                        override val origin = IntPoint(10, 20)
                        override val canPlaceMenuSlots = false
                        override fun addRenderableWidget(widget: AbstractWidget): Remover {
                            widgets += widget
                            return Remover { widgets.remove(widget) }
                        }
                        override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Unexpected render-only widget")
                    }
                    val leaf = PlacerTestView()
                    val scroll = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                    val bar = if (horizontal) XScrollbarView() else YScrollbarView()
                    val scrollOffset = if (horizontal) (scroll as XSimpleScrollView).scrollOffsetX else (scroll as YSimpleScrollView).scrollOffsetY
                    val barOffset = if (horizontal) (bar as XScrollbarView).scrollOffsetX else (bar as YScrollbarView).scrollOffsetY
                    scrollOffset.value = 25.25
                    scroll += leaf
                    val connection = connectScrollbar(scroll, scrollOffset, bar, barOffset)
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
                            val viewport = widgets.filterIsInstance<VanillaSimpleScrollViewWidget>().single()
                            val scrollbar = widgets.filterIsInstance<VanillaScrollbarViewWidget>().single()
                            val child = viewport.children().single() as AbstractWidget
                            try {
                                Assertions.assertEquals(100, bar.contentLength)
                                Assertions.assertEquals(if (horizontal) 50 else viewportLength, bar.viewportLength)
                                Assertions.assertEquals(scrollOffset.value, barOffset.value)
                                Assertions.assertEquals(if (horizontal) context.origin.x - scrollOffset.value.coerceIn(0.0, 50.0).toInt() else context.origin.x, child.x)
                                barOffset.value = 10.0
                                Assertions.assertEquals(if (horizontal) IntPoint(0, 20) else IntPoint(10, 10), IntPoint(child.x, child.y))
                                Assertions.assertTrue(viewport.mouseScrolled(25.0, 30.0, 0.0, -1.0))
                                Assertions.assertEquals(25.0, barOffset.value)
                                viewport.render(environment.graphics, 25, 30, 0.0F)
                                scrollbar.render(environment.graphics, 25, 30, 0.0F)
                                Assertions.assertTrue(environment.fills.isNotEmpty())
                                Assertions.assertTrue(scrollbar.mouseClicked(scrollbar.x + 1.0, scrollbar.y + 1.0, 0))
                                Assertions.assertTrue(scrollbar.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
                                Assertions.assertEquals(if (horizontal) IntPoint(-40, 20) else IntPoint(10, 20 - (100 - viewportLength)), IntPoint(child.x, child.y))
                                Assertions.assertTrue(scrollbar.mouseReleased(500.0, 500.0, 0))
                                controller.setOffset(IntPoint(30, 40))
                                Assertions.assertEquals(IntPoint(30, 40), IntPoint(viewport.x, viewport.y))
                            } finally {
                                controller.dispose()
                                controller.detach()
                            }
                            Assertions.assertTrue(widgets.isEmpty())
                            Assertions.assertEquals(1, scrollOffset.listeners.size)
                        }
                    } finally {
                        connection.remove()
                    }
                    Assertions.assertTrue(scrollOffset.listeners.isEmpty())
                    Assertions.assertTrue(barOffset.listeners.isEmpty())
                    Assertions.assertTrue(scroll.onScrollBoundsChanged.listeners.isEmpty())
                    Assertions.assertEquals(2, leaf.disposals)
                    Assertions.assertEquals(2, leaf.detaches)
                }
            }
        }
    }

}
