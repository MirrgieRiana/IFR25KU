package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.ContextViewPlacer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
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
    var clickHandled = true
    var clicks = 0
    var keys = 0
    var releases = 0
    var characters = 0
    var drags = 0
    var scrolls = 0

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isMouseOver(mouseX, mouseY) || button != 0) return false
        clicks += 1
        onClick()
        return clickHandled
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        keys += 1
        return true
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        characters += 1
        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        releases += 1
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        drags += 1
        return true
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        scrolls += 1
        return true
    }
}

private class AdapterFixture(vararg views: TestView) {
    val widgets = mutableListOf<RecordingWidget>()
    val removers = mutableListOf<Remover>()
    var disposals = 0
    val adapter = ViewAdapter(
        XListView().also { root -> views.forEach { root += it } },
        IntPoint(100, 100),
        IntPoint(30, 40),
        ADAPTER_RENDERING_PROXY,
        ContextViewPlacer { context, view, bounds ->
            val widget = RecordingWidget(bounds)
            widgets += widget
            val remover = if ((view as TestView).interactive) context.addRenderableWidget(widget) else context.addRenderableOnly(widget)
            removers += remover
            object : ViewController {
                override fun setOffset(offset: IntPoint) = widget.setPosition(offset.x, offset.y)
                override fun detach() = throw UnsupportedOperationException()
                override fun dispose() {
                    disposals += 1
                }
            }
        },
    )
}

class ViewAdapterTest {
    @Test
    fun `Screen なしで配置し寸法と登録を維持して移動するのだ`() {
        val fixture = AdapterFixture(TestView(true), TestView(true))
        val adapter = fixture.adapter
        assertEquals(IntPoint(20, 20), adapter.size)
        assertEquals(listOf(30, 40), fixture.widgets.map { it.x })

        adapter.setOffset(IntPoint(100, 200))

        assertEquals(listOf(100, 110), fixture.widgets.map { it.x })
        assertEquals(listOf(200, 200), fixture.widgets.map { it.y })
        assertEquals(ScreenRectangle(100, 200, 20, 20), adapter.rectangle)
        assertEquals(2, adapter.children().size)
        assertEquals(0, fixture.disposals)
        adapter.dispose()
    }

    @Test
    fun `クリックした子だけにキーと文字を渡し親の再フォーカスでも維持するのだ`() {
        val fixture = AdapterFixture(TestView(true), TestView(true))
        val adapter = fixture.adapter
        val parent = object : AbstractContainerEventHandler() {
            override fun children() = listOf(adapter)
        }
        assertTrue(parent.mouseClicked(31.0, 41.0, 0))
        assertTrue(parent.keyPressed(65, 0, 0))
        assertTrue(parent.mouseClicked(41.0, 41.0, 0))
        assertSame(fixture.widgets[1], adapter.focused)
        assertFalse(fixture.widgets[0].isFocused)
        assertTrue(parent.keyPressed(66, 0, 0))
        assertTrue(parent.charTyped('a', 0))
        assertEquals(listOf(1, 1), fixture.widgets.map { it.keys })
        assertEquals(listOf(0, 1), fixture.widgets.map { it.characters })

        parent.focused = null

        assertFalse(adapter.isFocused)
        assertFalse(adapter.keyPressed(65, 0, 0))
        assertFalse(adapter.charTyped('b', 0))
        adapter.dispose()
    }

    @Test
    fun `ドラッグと領域外でのリリースを選択した子へ渡すのだ`() {
        val fixture = AdapterFixture(TestView(true), TestView(true))
        val adapter = fixture.adapter
        assertTrue(adapter.mouseClicked(31.0, 41.0, 0))
        assertTrue(adapter.mouseDragged(500.0, 500.0, 0, 10.0, 10.0))
        assertTrue(adapter.mouseReleased(500.0, 500.0, 0))
        assertFalse(adapter.isDragging)
        assertFalse(adapter.mouseDragged(500.0, 500.0, 0, 10.0, 10.0))
        assertEquals(listOf(1, 0), fixture.widgets.map { it.drags })
        assertEquals(listOf(1, 0), fixture.widgets.map { it.releases })
        adapter.dispose()
    }

    @Test
    fun `描画専用の子を入力とフォーカスとナレーションの対象から除くのだ`() {
        val fixture = AdapterFixture(TestView(false), TestView(true))
        val adapter = fixture.adapter
        fixture.widgets[0].isFocused = true
        assertEquals(listOf(fixture.widgets[1]), adapter.children())
        assertFalse(adapter.isMouseOver(31.0, 41.0))
        assertFalse(adapter.mouseClicked(31.0, 41.0, 0))
        assertFalse(adapter.mouseScrolled(31.0, 41.0, 0.0, 1.0))
        assertEquals(NarratableEntry.NarrationPriority.NONE, adapter.narrationPriority())
        adapter.nextFocusPath(FocusNavigationEvent.TabNavigation(true))!!.applyFocus(true)
        assertSame(fixture.widgets[1], adapter.focused)
        assertEquals(NarratableEntry.NarrationPriority.FOCUSED, adapter.narrationPriority())
        assertTrue(adapter.mouseScrolled(41.0, 41.0, 0.0, 1.0))
        assertEquals(listOf(0, 1), fixture.widgets.map { it.scrolls })
        adapter.dispose()
    }

    @Test
    fun `クリック中に子が削除されても削除済みの子へフォーカスを戻さないのだ`() {
        val fixture = AdapterFixture(TestView(true), TestView(true))
        val adapter = fixture.adapter
        fixture.widgets[0].onClick = {
            fixture.removers[0].remove()
            fixture.removers[1].remove()
        }

        assertTrue(adapter.mouseClicked(31.0, 41.0, 0))

        assertNull(adapter.focused)
        assertFalse(adapter.isDragging)
        assertFalse(adapter.keyPressed(65, 0, 0))
        assertTrue(adapter.children().isEmpty())
        adapter.dispose()
    }

    @Test
    fun `処理されないクリック中に登録を変更しても走査を継続できるのだ`() {
        val fixture = AdapterFixture(TestView(true), TestView(true))
        val adapter = fixture.adapter
        fixture.widgets[0].clickHandled = false
        fixture.widgets[1].setPosition(30, 40)
        fixture.widgets[0].onClick = { fixture.removers[1].remove() }
        assertFalse(adapter.mouseClicked(31.0, 41.0, 0))
        assertEquals(listOf(1, 0), fixture.widgets.map { it.clicks })

        fixture.widgets[0].clickHandled = true
        assertTrue(adapter.mouseClicked(31.0, 41.0, 0))
        fixture.removers[0].remove()
        fixture.removers[0].remove()
        assertNull(adapter.focused)
        assertFalse(adapter.isDragging)
        adapter.dispose()
    }

    @Test
    fun `文字入力中は Escape 以外の未処理キーを消費するのだ`() {
        val editBox = object : EditBox(null, 10, 20, Component.empty()) {
            private var focused2 = false
            override fun setFocused(focused: Boolean) {
                focused2 = focused
            }
            override fun isFocused() = focused2
            override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = false
        }
        val adapter = ViewAdapter(TestView(true), IntPoint(100, 100), IntPoint.ZERO, ADAPTER_RENDERING_PROXY, ContextViewPlacer { context, _, _ ->
            context.addRenderableWidget(editBox)
            ViewController.NONE
        })
        adapter.focused = editBox
        assertTrue(adapter.keyPressed(GLFW.GLFW_KEY_E, 0, 0))
        assertFalse(adapter.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0))
        editBox.setEditable(false)
        assertFalse(adapter.keyPressed(GLFW.GLFW_KEY_E, 0, 0))
        adapter.dispose()
    }

    @Test
    fun `破棄は一度だけ購読を解除しメニュースロットと同様に detach を要求しないのだ`() {
        val fixture = AdapterFixture(TestView(true))
        val adapter = fixture.adapter
        adapter.mouseClicked(31.0, 41.0, 0)

        adapter.dispose()
        adapter.dispose()
        fixture.removers[0].remove()

        assertEquals(1, fixture.disposals)
        assertNull(adapter.focused)
        assertFalse(adapter.isDragging)
        assertTrue(adapter.children().isEmpty())
        assertFalse(adapter.keyPressed(65, 0, 0))
        assertFalse(adapter.mouseClicked(31.0, 41.0, 0))
        assertThrows(IllegalStateException::class.java) { adapter.setOffset(IntPoint.ZERO) }
    }
}
