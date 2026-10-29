package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW

private val SCROLL_WIDGET_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class ScrollChildView : FixedWidgetView(IntPoint(100, 100))

private class ScrollChildWidget(bounds: IntRectangle) : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, Component.empty()) {
    var clicks = 0
    var scrolls = 0
    var releases = 0
    var consumeScroll = false
    var onClick: () -> Unit = { }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isMouseOver(mouseX, mouseY) || button != 0) return false
        clicks += 1
        onClick()
        return true
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        scrolls += 1
        return consumeScroll
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        releases += 1
        return true
    }
}

private class ScrollWidgetFixture(horizontal: Boolean) {
    val widgets = mutableListOf<ScrollChildWidget>()
    val removers = mutableListOf<Remover>()
    var disposals = 0
    val view = if (horizontal) XScrollView() else YScrollView()
    val widget: VanillaScrollViewWidget
    val controller: ViewController
    val interactiveWidgets = mutableListOf<AbstractWidget>()
    val renderableOnlyWidgets = mutableListOf<AbstractWidget>()
    val screen: Screen = object : Screen(Component.empty()) {
        override fun children() = interactiveWidgets
    }

    init {
        if (!horizontal) {
            view += YListView().also { list ->
                repeat(3) {
                    list += ScrollChildView()
                }
            }
        } else {
            view += XListView().also { list ->
                repeat(3) {
                    list += ScrollChildView()
                }
            }
        }
        view.initSizeCalculation(SCROLL_WIDGET_RENDERING_PROXY, { 100 }, { 100 })
        val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
        registry.register(ScrollChildView::class.java) { context, _, bounds ->
            val child = ScrollChildWidget(bounds)
            widgets += child
            val remover = context.addRenderableWidget(child)
            removers += remover
            object : ViewController {
                override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                override fun detach() = remover.remove()
                override fun dispose() {
                    disposals += 1
                }
            }
        }
        val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
            override val origin = IntPoint(10, 20)
            override fun addRenderableWidget(widget: AbstractWidget): Remover {
                interactiveWidgets += widget
                return Remover { interactiveWidgets.remove(widget) }
            }

            override fun addRenderableOnly(widget: AbstractWidget): Remover {
                renderableOnlyWidgets += widget
                return Remover { renderableOnlyWidgets.remove(widget) }
            }
        }
        widget = when (view) {
            is XScrollView -> VanillaXScrollViewWidget(view, IntRectangle(10, 20, 100, 100))
            is YScrollView -> VanillaYScrollViewWidget(view, IntRectangle(10, 20, 100, 100))
            else -> throw IllegalStateException("Unsupported scroll view")
        }
        controller = placeScrollView(context, widget, view.childView, registry)
    }
}

class VanillaScrollViewWidgetTest {

    @Test
    fun `描画ウィジェットは処理したクリックに対応する解放だけを消費するのだ`() {
        var consumeClick = false
        val renderer: ViewRenderer<ScrollChildView> = object : ViewRenderer<ScrollChildView> {
            override fun mouseClicked(view: ScrollChildView, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = consumeClick
        }
        val widget = VanillaViewRendererWidget(renderer, ScrollChildView(), IntRectangle(10, 20, 100, 100))
        assertFalse(widget.mouseClicked(30.0, 40.0, 0))
        assertFalse(widget.mouseDragged(30.0, 40.0, 0, 1.0, 1.0))
        assertFalse(widget.mouseReleased(30.0, 40.0, 0))

        consumeClick = true
        assertTrue(widget.mouseClicked(30.0, 40.0, 0))
        assertTrue(widget.mouseDragged(300.0, 400.0, 0, 1.0, 1.0))
        assertTrue(widget.mouseReleased(300.0, 400.0, 0))
        assertFalse(widget.mouseReleased(300.0, 400.0, 0))
    }

    @Test
    fun `表示範囲外の子へクリックを渡さないのだ`() {
        val fixture = ScrollWidgetFixture(horizontal = false)
        assertFalse(fixture.screen.mouseClicked(50.0, 150.0, 0))
        assertEquals(listOf(0, 0, 0), fixture.widgets.map { it.clicks })
        assertFalse(fixture.screen.mouseClicked(107.0, 50.0, 1))
        fixture.controller.dispose()
    }

    @Test
    fun `子のドラッグ終了は表示範囲の外でも届けるのだ`() {
        val fixture = ScrollWidgetFixture(horizontal = false)
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))

        assertTrue(fixture.screen.mouseReleased(-100.0, -100.0, 0))

        assertEquals(1, fixture.widgets[0].releases)
        assertFalse(fixture.screen.isDragging)
        fixture.controller.dispose()
    }

    @Test
    fun `子を画面へ個別登録し配置解除と破棄をコントローラーで行うのだ`() {
        val fixture = ScrollWidgetFixture(horizontal = false)
        assertEquals(3, fixture.screen.children().size)
        assertFalse(fixture.widget in fixture.screen.children())
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        assertTrue(fixture.widgets[0].isFocused)
        fixture.controller.dispose()
        assertEquals(3, fixture.disposals)
        fixture.controller.detach()
        assertTrue(fixture.screen.children().isEmpty())
        assertTrue(fixture.renderableOnlyWidgets.isEmpty())
    }

    @Test
    fun `親の移動後も子と表示領域が同じ距離だけ移動するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollWidgetFixture(horizontal)
            fixture.controller.setOffset(IntPoint(30, 40))
            assertEquals(IntPoint(30, 40), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
            assertTrue(fixture.screen.mouseClicked(50.0, 70.0, 0))
            assertFalse(fixture.screen.mouseClicked(20.0, 30.0, 0))
            assertEquals(1, fixture.widgets[0].clicks)
            fixture.controller.dispose()
            fixture.controller.detach()
        }
    }

    @Test
    fun `Tab のフォーカスは画面上の登録を経由して元の子へ届くのだ`() {
        val fixture = ScrollWidgetFixture(horizontal = false)
        fixture.screen.nextFocusPath(FocusNavigationEvent.TabNavigation(true))!!.applyFocus(true)
        assertTrue(fixture.widgets[0].isFocused)
        assertTrue(fixture.screen.focused === fixture.interactiveWidgets[0])
        assertNull(fixture.screen.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
        fixture.controller.dispose()
        fixture.controller.detach()
    }

    @Test
    fun `入れ子の表示領域の共通部分だけでクリックできるのだ`() {
        val child = ScrollChildWidget(IntRectangle(0, 0, 100, 100))
        val inner = VanillaClippedWidget(child) { IntRectangle(10, 10, 80, 80) }
        val outer = VanillaClippedWidget(inner) { IntRectangle(30, 30, 80, 80) }
        assertFalse(outer.mouseClicked(20.0, 40.0, 0))
        assertFalse(outer.mouseClicked(95.0, 40.0, 0))
        assertTrue(outer.mouseClicked(40.0, 40.0, 0))
        assertEquals(1, child.clicks)

        val first = VanillaClippedWidget(child) { IntRectangle(10, 10, 20, 20) }
        val second = VanillaClippedWidget(first) { IntRectangle(60, 60, 20, 20) }
        assertFalse(second.isActive)
        assertNull(second.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
    }

}
