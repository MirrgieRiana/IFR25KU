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
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
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
            override fun addRenderableWidget(widget: AbstractWidget): Remover = throw UnsupportedOperationException()
            override fun addRenderableOnly(widget: AbstractWidget): Remover = throw UnsupportedOperationException()
        }
        widget = when (view) {
            is XScrollView -> VanillaXScrollViewWidget(view, IntRectangle(10, 20, 100, 100), context, registry)
            is YScrollView -> VanillaYScrollViewWidget(view, IntRectangle(10, 20, 100, 100), context, registry)
            else -> throw IllegalStateException("Unsupported scroll view")
        }
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
        assertFalse(fixture.widget.mouseClicked(50.0, 150.0, 0))
        assertEquals(listOf(0, 0, 0), fixture.widgets.map { it.clicks })
        assertFalse(fixture.widget.mouseClicked(107.0, 50.0, 1))
        fixture.widget.dispose()
    }

    @Test
    fun `子のドラッグ終了は表示範囲の外でも届けるのだ`() {
        val fixture = ScrollWidgetFixture(horizontal = false)
        assertTrue(fixture.widget.mouseClicked(50.0, 50.0, 0))

        assertTrue(fixture.widget.mouseReleased(-100.0, -100.0, 0))

        assertEquals(1, fixture.widgets[0].releases)
        assertFalse(fixture.widget.isDragging)
        fixture.widget.dispose()
    }

    @Test
    fun `クリック中に削除された子のフォーカスを保持しないのだ`() {
        val fixture = ScrollWidgetFixture(horizontal = false)
        fixture.widgets[0].onClick = { fixture.removers[0].remove() }

        assertTrue(fixture.widget.mouseClicked(50.0, 50.0, 0))

        assertNull(fixture.widget.focused)
        assertFalse(fixture.widget.isDragging)
        fixture.widget.dispose()
    }

}
