package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW

private val ADAPTER_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class TestView(val interactive: Boolean) : FixedWidgetView(IntPoint(10, 20))

private class RecordingWidget(bounds: IntRectangle) : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty()) {
    var onClick: () -> Unit = { }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isMouseOver(mouseX, mouseY) || button != 0) return false
        onClick()
        return true
    }
}

private class AdapterFixture(vararg views: TestView) {
    val widgets = mutableListOf<RecordingWidget>()
    val interactiveWidgets = mutableListOf<AbstractWidget>()
    val renderableOnlyWidgets = mutableListOf<AbstractWidget>()
    val removers = mutableListOf<Remover>()
    var disposals = 0
    var removals = 0
    val screen: Screen = object : Screen(Component.empty()) {
        override fun children() = interactiveWidgets
    }.also {
        it.width = 100
        it.height = 100
    }
    private var imageWidth = 0
    private var imageHeight = 0
    val initializedSizes = mutableListOf<IntPoint>()
    val disposalsBeforeCreate = mutableListOf<Int>()
    var onDispose: () -> Unit = { }
    val host: ViewAdapterHost = object : ViewAdapterHost {
        override fun getScreen() = this@AdapterFixture.screen
        override fun getRegionSizeX() = screen.width
        override fun getRegionSizeY() = screen.height
        override fun getUiOffset() = IntPoint(30, 40)
        override fun setUiSize(uiSize: IntPoint) {
            this@AdapterFixture.imageWidth = uiSize.x
            this@AdapterFixture.imageHeight = uiSize.y
        }

        override fun getRenderingProxy() = ADAPTER_RENDERING_PROXY
        private val viewPlacerRegistry = ViewPlacerRegistry<VanillaViewPlacerContext>().also {
            it.register(TestView::class.java) { context, view, bounds ->
                val widget = RecordingWidget(bounds)
                widgets += widget
                val remover = if (view.interactive) context.addRenderableWidget(widget) else context.addRenderableOnly(widget)
                removers += remover
                object : ViewController {
                    override fun setOffset(offset: IntPoint) = widget.setPosition(offset.x, offset.y)
                    override fun detach() = throw UnsupportedOperationException()
                    override fun dispose() {
                        disposals += 1
                        onDispose()
                    }
                }
            }
        }
        override fun getViewPlacerRegistry() = viewPlacerRegistry

        override fun createRootView(): View {
            disposalsBeforeCreate += disposals
            return XListView().also { root ->
                views.forEach {
                    root += it
                }
            }
        }

        override fun init() {
            initializedSizes += IntPoint(imageWidth, imageHeight)
            interactiveWidgets.clear()
            renderableOnlyWidgets.clear()
            screen.focused = null
            screen.isDragging = false
        }

        override fun addRenderableWidget(widget: AbstractWidget): Remover {
            interactiveWidgets += widget
            return Remover {
                removals += 1
                interactiveWidgets.remove(widget)
                adapter.removeWidget(widget)
            }
        }

        override fun addRenderableOnly(widget: AbstractWidget): Remover {
            renderableOnlyWidgets += widget
            return Remover {
                removals += 1
                renderableOnlyWidgets.remove(widget)
                adapter.removeWidget(widget)
            }
        }
    }
    val adapter = ViewAdapter(host)

    init {
        init()
    }

    fun init() = adapter.init()
}

class ViewAdapterTest {
    @Test
    fun `Screen なしでホストの領域を使って配置と移動を行うのだ`() {
        val widgets = mutableListOf<RecordingWidget>()
        val renderableWidgets = mutableListOf<AbstractWidget>()
        var size = IntPoint.ZERO
        var disposals = 0
        val registry = ViewPlacerRegistry<VanillaViewPlacerContext>().also {
            it.register(TestView::class.java) { context, view, bounds ->
                val widget = RecordingWidget(bounds)
                widgets += widget
                if (view.interactive) context.addRenderableWidget(widget) else context.addRenderableOnly(widget)
                object : ViewController {
                    override fun setOffset(offset: IntPoint) = widget.setPosition(offset.x, offset.y)
                    override fun detach() = throw UnsupportedOperationException()
                    override fun dispose() {
                        disposals += 1
                    }
                }
            }
        }
        val rootView = XListView().also {
            it.sizingX = Sizing.FILL
            it.sizingY = Sizing.FILL
            it += TestView(true)
            it += TestView(false)
        }
        val adapter = ViewAdapter(object : ViewAdapterHost {
            override fun getRenderingProxy() = ADAPTER_RENDERING_PROXY
            override fun getViewPlacerRegistry() = registry
            override fun getScreen() = null
            override fun getRegionSizeX() = 300
            override fun getRegionSizeY() = 80
            override fun createRootView() = rootView
            override fun getUiOffset() = IntPoint.ZERO
            override fun setUiSize(uiSize: IntPoint) = run { size = uiSize }
            override fun init() {
                renderableWidgets.clear()
            }
            override fun addRenderableWidget(widget: AbstractWidget): Remover {
                renderableWidgets += widget
                return Remover { renderableWidgets.remove(widget) }
            }
            override fun addRenderableOnly(widget: AbstractWidget) = addRenderableWidget(widget)
        })
        adapter.init()
        assertEquals(IntPoint(300, 80), size)
        assertEquals(listOf(0, 10), widgets.map { it.x })
        assertFalse(adapter.keyPressed(GLFW.GLFW_KEY_E, 0, 0))
        assertFalse(adapter.mouseDragged(0.0, 0.0, 0, 1.0, 1.0))
        assertFalse(adapter.mouseReleased(0.0, 0.0, 0))
        adapter.mouseClicked()
        adapter.removeWidget(widgets[0])

        adapter.setOffset(IntPoint(30, 40))

        assertEquals(listOf(30, 40), widgets.map { it.x })
        assertEquals(listOf(40, 40), widgets.map { it.y })
        adapter.removed()
        adapter.removed()
        assertEquals(2, disposals)
        assertThrows(IllegalStateException::class.java) { adapter.setOffset(IntPoint.ZERO) }
    }

    @Test
    fun `再初期化では旧配置を破棄してから新しい View を生成するのだ`() {
        val fixture = AdapterFixture(TestView(true))
        assertEquals(listOf(0), fixture.disposalsBeforeCreate)

        fixture.init()

        assertEquals(listOf(0, 1), fixture.disposalsBeforeCreate)
        assertEquals(listOf(IntPoint(10, 20), IntPoint(10, 20)), fixture.initializedSizes)
        assertEquals(listOf(fixture.widgets[1]), fixture.interactiveWidgets)
        assertEquals(IntPoint(30, 40), IntPoint(fixture.widgets[1].x, fixture.widgets[1].y))
        fixture.adapter.removed()
    }

    @Test
    fun `破棄の後始末から再入しても入力を消費しないのだ`() {
        val fixture = AdapterFixture(TestView(true))
        fixture.onDispose = {
            assertFalse(fixture.adapter.keyPressed(GLFW.GLFW_KEY_E, 0, 0))
            assertFalse(fixture.adapter.mouseDragged(10.0, 20.0, 0, 1.0, 2.0))
            assertFalse(fixture.adapter.mouseReleased(10.0, 20.0, 0))
            assertThrows(IllegalStateException::class.java) { fixture.adapter.setOffset(IntPoint.ZERO) }
            fixture.adapter.removed()
        }

        fixture.adapter.removed()

        assertEquals(1, fixture.disposals)
    }

    @Test
    fun `ホストの Screen に配置し寸法と登録を維持して移動するのだ`() {
        val fixture = AdapterFixture(TestView(true), TestView(true))
        val adapter = fixture.adapter
        assertEquals(IntPoint(20, 20), fixture.initializedSizes.single())
        assertEquals(listOf(30, 40), fixture.widgets.map { it.x })

        adapter.setOffset(IntPoint(100, 200))

        assertEquals(listOf(100, 110), fixture.widgets.map { it.x })
        assertEquals(listOf(200, 200), fixture.widgets.map { it.y })
        assertEquals(fixture.widgets, fixture.interactiveWidgets)
        assertEquals(0, fixture.disposals)
        adapter.removed()
    }

    @Test
    fun `描画専用の子と入力を受け付ける子を指定された登録先へ渡すのだ`() {
        val fixture = AdapterFixture(TestView(false), TestView(true))
        assertEquals(listOf(fixture.widgets[0]), fixture.renderableOnlyWidgets)
        assertEquals(listOf(fixture.widgets[1]), fixture.interactiveWidgets)

        fixture.removers[0].remove()

        assertTrue(fixture.renderableOnlyWidgets.isEmpty())
        assertEquals(listOf(fixture.widgets[1]), fixture.interactiveWidgets)

        fixture.removers[1].remove()

        assertTrue(fixture.interactiveWidgets.isEmpty())
        assertEquals(2, fixture.removals)
        fixture.adapter.removed()
    }

    @Test
    fun `登録解除の呼び出しをホストの解除処理へ中継するのだ`() {
        val fixture = AdapterFixture(TestView(true))
        val remover = fixture.removers.single()
        assertEquals(1, fixture.widgets.size)
        assertEquals(0, fixture.removals)

        remover.remove()

        assertEquals(1, fixture.removals)

        remover.remove()

        assertEquals(2, fixture.removals)
        assertTrue(fixture.interactiveWidgets.isEmpty())
        fixture.adapter.removed()
    }

    @Test
    fun `削除された子のフォーカスとドラッグ状態をホストから解除するのだ`() {
        val fixture = AdapterFixture(TestView(true), TestView(true))
        fixture.screen.focused = fixture.widgets[0]
        fixture.screen.isDragging = true

        fixture.removers[0].remove()

        assertNull(fixture.screen.focused)
        assertFalse(fixture.screen.isDragging)
        assertEquals(listOf(fixture.widgets[1]), fixture.screen.children())
        fixture.adapter.removed()
    }

    @Test
    fun `クリック中に削除された子への再フォーカスをホストから解除するのだ`() {
        val fixture = AdapterFixture(TestView(true))
        fixture.widgets[0].onClick = { fixture.removers[0].remove() }

        assertTrue(fixture.screen.mouseClicked(31.0, 41.0, 0))
        fixture.adapter.mouseClicked()

        assertNull(fixture.screen.focused)
        assertFalse(fixture.screen.isDragging)
        fixture.adapter.removed()
    }

    @Test
    fun `ホストの文字入力中は Escape 以外の未処理キーを消費するのだ`() {
        val fixture = AdapterFixture()
        val editBox: EditBox = object : EditBox(null, 10, 20, Component.empty()) {
            private var focused2 = false
            override fun setFocused(focused: Boolean) {
                focused2 = focused
            }
            override fun isFocused() = focused2
            override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = false
        }
        fixture.interactiveWidgets += editBox
        fixture.screen.focused = editBox

        assertTrue(fixture.adapter.keyPressed(GLFW.GLFW_KEY_E, 0, 0))
        assertFalse(fixture.adapter.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0))
        editBox.setEditable(false)
        assertFalse(fixture.adapter.keyPressed(GLFW.GLFW_KEY_E, 0, 0))
        fixture.adapter.removed()
    }

    @Test
    fun `破棄は一度だけ購読を解除しメニュースロットと同様に detach を要求しないのだ`() {
        val fixture = AdapterFixture(TestView(true))
        val adapter = fixture.adapter

        adapter.removed()
        adapter.removed()
        fixture.removers[0].remove()

        assertEquals(1, fixture.disposals)
        assertThrows(IllegalStateException::class.java) { adapter.setOffset(IntPoint.ZERO) }
    }
}

