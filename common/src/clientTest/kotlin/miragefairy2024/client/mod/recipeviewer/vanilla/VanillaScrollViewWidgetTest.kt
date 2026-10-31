package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.MinContentSizeYView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.narration.ScreenNarrationCollector
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.gui.navigation.ScreenDirection
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.inventory.Slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW

private val SCROLL_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class ScrollTestView(val interactive: Boolean) : FixedWidgetView(IntPoint(100, 100))

private class ScrollTestWidget(bounds: IntRectangle, private val label: String) : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
    val clicks = mutableListOf<Int>()
    val moves = mutableListOf<Pair<Double, Double>>()
    val drags = mutableListOf<Int>()
    val releases = mutableListOf<Int>()
    var scrolls = 0
    var consumeScroll = false
    var consumeKeys = false
    var characters = ""
    var onClick: () -> Unit = { }
    var priority = NarratableEntry.NarrationPriority.NONE

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = narrationElementOutput.add(NarratedElementType.TITLE, label)
    override fun narrationPriority() = if (isFocused) NarratableEntry.NarrationPriority.FOCUSED else priority

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        moves += Pair(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isMouseOver(mouseX, mouseY)) return false
        clicks += button
        onClick()
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        drags += button
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        releases += button
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        scrolls += 1
        return consumeScroll
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int) = consumeKeys
    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int) = consumeKeys

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        characters += codePoint
        return true
    }
}

private class ScrollFixture {
    val widgets = mutableListOf<ScrollTestWidget>()
    val widgetContexts = mutableListOf<VanillaViewPlacerContext>()
    val removers = mutableListOf<Remover>()
    val screenWidgets = mutableListOf<AbstractWidget>()
    var disposals = 0
    var slotPlacements = 0
    val screen: Screen = object : Screen(text { empty() }) {
        override fun children() = screenWidgets
    }.also {
        it.width = 300
        it.height = 300
    }
    private val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
        override val origin = IntPoint(10, 20)

        override fun addRenderableWidget(widget: AbstractWidget): Remover {
            screenWidgets += widget
            return Remover { screenWidgets.remove(widget) }
        }

        override fun addRenderableOnly(widget: AbstractWidget) = throw AssertionError("Scroll containers must accept input")
    }
    private val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()

    init {
        registry.register { context, view: ScrollTestView, bounds ->
            val widget = ScrollTestWidget(bounds, "child-${widgets.size}")
            widgets += widget
            widgetContexts += context
            val remover = if (view.interactive) context.addRenderableWidget(widget) else context.addRenderableOnly(widget)
            removers += remover
            object : ViewController {
                override fun setOffset(offset: IntPoint) = widget.setPosition(offset.x, offset.y)
                override fun detach() = remover.remove()
                override fun dispose() {
                    disposals += 1
                }
            }
        }
        fun place(context: VanillaViewPlacerContext, widget: VanillaScrollViewWidget): ViewController {
            val remover = context.addRenderableWidget(widget)
            return object : ViewController {
                override fun setOffset(offset: IntPoint) = widget.setPosition(offset.x, offset.y)
                override fun dispose() = widget.dispose()
                override fun detach() {
                    widget.detach()
                    remover.remove()
                }
            }
        }
        registry.register { context, view: XScrollView, bounds ->
            place(context, VanillaXScrollViewWidget(view, bounds, context, registry))
        }
        registry.register { context, view: YScrollView, bounds ->
            place(context, VanillaYScrollViewWidget(view, bounds, context, registry))
        }
        registry.register { _, _: MenuSlotView, _ ->
            slotPlacements += 1
            throw AssertionError("MenuSlotView must be rejected before placement")
        }
    }

    fun attach(view: ScrollView, size: IntPoint): ViewController {
        view.initSizeCalculation(SCROLL_RENDERING_PROXY, { size.x }, { size.y })
        return view.attachTo(context.origin) { child, bounds -> registry.place(context, child, bounds) }
    }

    fun focusNext(forward: Boolean): Boolean {
        val path = screen.nextFocusPath(FocusNavigationEvent.TabNavigation(forward)) ?: return false
        screen.currentFocusPath?.applyFocus(false)
        path.applyFocus(true)
        return true
    }
}

private fun createScrollView(horizontal: Boolean): ScrollView {
    val view = if (horizontal) XScrollView() else YScrollView()
    val list = if (horizontal) XListView() else YListView()
    repeat(3) {
        list += ScrollTestView(true)
    }
    view += list
    return view
}

class VanillaScrollViewWidgetTest {

    @Test
    fun `子を画面へ個別登録せずスクロール端ではホイールを親へ返すのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val controller = fixture.attach(createScrollView(horizontal), IntPoint(100, 100))
            val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
            assertEquals(3, root.children().size)
            fixture.widgets[0].consumeScroll = true
            assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
            assertEquals(IntPoint(10, 20), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
            fixture.widgets[0].consumeScroll = false
            assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1000.0))
            val expected = if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180)
            assertEquals(expected, IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
            assertFalse(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `重なった子へのホイールは Screen と同じく最初の子だけへ届けるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.widgets[1].setPosition(fixture.widgets[0].x, fixture.widgets[0].y)
        fixture.widgets[1].consumeScroll = true

        assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))

        assertEquals(1, fixture.widgets[0].scrolls)
        assertEquals(0, fixture.widgets[1].scrolls)
        assertEquals(5, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `一部だけ見えている子をクリックしても表示位置を変更しないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
        assertEquals(5, fixture.widgets[0].y)

        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))

        assertEquals(5, fixture.widgets[0].y)
        assertTrue(fixture.widgets[0].isFocused)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `領域外のクリックを除外し押下先へドラッグと解放を届けるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertFalse(root.mouseClicked(50.0, 150.0, 0))
        assertTrue(root.getChildAt(50.0, 150.0).isEmpty)
        assertTrue(root.getChildAt(107.0, 50.0).isEmpty)
        assertTrue(root.getChildAt(50.0, 50.0).isPresent)
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        assertTrue(root.mouseDragged(-100.0, -100.0, 0, 1.0, 1.0))
        assertTrue(root.mouseReleased(-100.0, -100.0, 0))
        assertEquals(listOf(0), fixture.widgets[0].drags)
        assertEquals(listOf(0), fixture.widgets[0].releases)
        assertFalse(root.mouseReleased(-100.0, -100.0, 0))
        assertFalse(root.mouseDragged(-100.0, -100.0, 0, 1.0, 1.0))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `領域外の移動は各ボタンの押下先だけへ実座標で届けるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget

        root.mouseMoved(50.0, 150.0)
        assertTrue(fixture.widgets.all { it.moves.isEmpty() })
        root.mouseMoved(50.0, 50.0)
        fixture.widgets.forEach {
            assertEquals(listOf(Pair(50.0, 50.0)), it.moves)
        }

        assertTrue(root.mouseClicked(50.0, 50.0, 0))
        assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -100.0 / 15))
        assertTrue(root.mouseClicked(50.0, 50.0, 1))
        root.mouseMoved(-100.0, -100.0)
        assertEquals(listOf(Pair(50.0, 50.0), Pair(-100.0, -100.0)), fixture.widgets[0].moves)
        assertEquals(listOf(Pair(50.0, 50.0), Pair(-100.0, -100.0)), fixture.widgets[1].moves)
        assertEquals(listOf(Pair(50.0, 50.0)), fixture.widgets[2].moves)

        assertTrue(root.mouseReleased(-100.0, -100.0, 0))
        root.mouseMoved(-200.0, -200.0)
        assertEquals(Pair(-100.0, -100.0), fixture.widgets[0].moves.last())
        assertEquals(Pair(-200.0, -200.0), fixture.widgets[1].moves.last())
        assertEquals(listOf(Pair(50.0, 50.0)), fixture.widgets[2].moves)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `解放を受け取らず領域外で再び押した場合は前回の押下先を解除するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(root.mouseClicked(50.0, 50.0, 0))

        assertFalse(root.mouseClicked(200.0, 200.0, 0))

        assertFalse(root.hasMouseCapture(0))
        assertFalse(root.isDragging)
        assertFalse(root.mouseReleased(200.0, 200.0, 0))
        assertTrue(fixture.widgets[0].releases.isEmpty())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `複数のボタンの解放をそれぞれの押下先へ届けるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(root.mouseClicked(50.0, 50.0, 0))
        assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -100.0 / 15))
        assertTrue(root.mouseClicked(50.0, 50.0, 1))
        assertTrue(root.mouseReleased(-100.0, -100.0, 0))
        assertTrue(root.mouseDragged(-100.0, -100.0, 1, 1.0, 1.0))
        assertTrue(root.mouseReleased(-100.0, -100.0, 1))
        assertEquals(listOf(0), fixture.widgets[0].releases)
        assertEquals(listOf(1), fixture.widgets[1].releases)
        assertEquals(listOf(1), fixture.widgets[1].drags)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `縦横のバーをドラッグして領域外で離せるのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val controller = fixture.attach(createScrollView(horizontal), IntPoint(100, 100))
            val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
            val mouseX = if (horizontal) 20.0 else 107.0
            val mouseY = if (horizontal) 117.0 else 30.0
            assertTrue(fixture.screen.mouseClicked(mouseX, mouseY, 0))
            assertTrue(root.mouseDragged(400.0, 400.0, 0, 300.0, 300.0))
            assertEquals(if (horizontal) -190 else -180, if (horizontal) fixture.widgets[0].x else fixture.widgets[0].y)
            assertTrue(root.mouseReleased(400.0, 400.0, 0))
            assertFalse(root.hasMouseCapture(0))
            assertFalse(root.mouseDragged(mouseX, mouseY, 0, 1.0, 1.0))
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `スクロールバーの操作で子のフォーカスを解除しないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(fixture.focusNext(true))

        assertTrue(fixture.screen.mouseClicked(107.0, 30.0, 0))
        assertTrue(root.mouseDragged(107.0, 100.0, 0, 0.0, 70.0))
        assertTrue(root.mouseReleased(107.0, 100.0, 0))

        assertEquals(fixture.widgets[0], root.focused)
        assertTrue(fixture.widgets[0].isFocused)
        assertTrue(fixture.widgets[0].releases.isEmpty())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `Tab と逆 Tab で画面外の子へ移動して表示するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        assertTrue(fixture.focusNext(true))
        assertTrue(fixture.widgets[0].isFocused)
        assertTrue(fixture.focusNext(true))
        assertTrue(fixture.widgets[1].isFocused)
        assertEquals(20, fixture.widgets[1].y)
        assertTrue(fixture.focusNext(true))
        assertTrue(fixture.widgets[2].isFocused)
        assertEquals(20, fixture.widgets[2].y)
        assertFalse(fixture.focusNext(true))
        assertTrue(fixture.focusNext(false))
        assertTrue(fixture.widgets[1].isFocused)
        assertEquals(20, fixture.widgets[1].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `外側をマウスで選択した後に逆 Tab で戻ると保持していた子を選択するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val outside = ScrollTestWidget(IntRectangle(200, 20, 50, 50), "outside")
        fixture.screenWidgets += outside
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        assertTrue(fixture.widgets[0].isFocused)
        assertTrue(fixture.screen.mouseClicked(210.0, 30.0, 0))
        assertFalse(fixture.widgets[0].isFocused)

        assertTrue(fixture.focusNext(false))

        assertTrue(fixture.widgets[0].isFocused)
        assertEquals(20, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `再入先を問い合わせただけでは保持している子とフォーカス状態を変更しないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        val outside = ScrollTestWidget(IntRectangle(200, 20, 50, 50), "outside")
        fixture.screenWidgets += outside
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        assertTrue(fixture.screen.mouseClicked(210.0, 30.0, 0))
        val positions = fixture.widgets.map { IntPoint(it.x, it.y) }

        repeat(2) {
            assertTrue(root.nextFocusPath(FocusNavigationEvent.TabNavigation(false)) != null)
            assertEquals(fixture.widgets[0], root.focused)
            assertFalse(root.isFocused)
            assertTrue(fixture.widgets.none { it.isFocused })
            assertTrue(outside.isFocused)
            assertEquals(positions, fixture.widgets.map { IntPoint(it.x, it.y) })
        }
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `子が無効になった後に領域自身を選択すると以前の子へのフォーカスを解除するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        val outside = ScrollTestWidget(IntRectangle(200, 20, 50, 50), "outside")
        fixture.screenWidgets += outside
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        assertTrue(fixture.screen.mouseClicked(210.0, 30.0, 0))
        fixture.widgets.forEach {
            it.active = false
        }

        assertTrue(fixture.focusNext(false))

        assertTrue(root.isFocused)
        assertNull(root.focused)
        assertTrue(fixture.widgets.none { it.isFocused })
        assertEquals(root, root.currentFocusPath?.component())
        assertTrue(root.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
        assertEquals(-180, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `矢印の未処理入力は子へのフォーカス移動に使うのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.focusNext(true)
        assertFalse(root.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
        val path = fixture.screen.nextFocusPath(FocusNavigationEvent.ArrowNavigation(ScreenDirection.DOWN))!!
        fixture.screen.currentFocusPath?.applyFocus(false)
        path.applyFocus(true)
        assertTrue(fixture.widgets[1].isFocused)
        assertEquals(20, fixture.widgets[1].y)
        fixture.widgets[1].consumeKeys = true
        assertTrue(root.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
        assertTrue(root.keyReleased(GLFW.GLFW_KEY_DOWN, 0, 0))
        assertTrue(root.charTyped('a', 0))
        assertEquals("a", fixture.widgets[1].characters)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `入れ子の内側を確定した後で外側もフォーカス先を表示するのだ`() {
        val fixture = ScrollFixture()
        val inner = createScrollView(false)
        val outer = YScrollView()
        outer += MinContentSizeYView(200).also { it += inner }
        val controller = fixture.attach(outer, IntPoint(100, 100))
        repeat(3) {
            assertTrue(fixture.focusNext(true))
        }
        assertTrue(fixture.widgets[2].isFocused)
        assertEquals(20, fixture.widgets[2].y)
        assertTrue(fixture.widgets[2].y + fixture.widgets[2].height <= 120)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `クリック中に削除した子へ通知せず対応する解放を消費するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.widgets[0].onClick = { fixture.removers[0].remove() }
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        assertNull(root.focused)
        assertTrue(root.hasMouseCapture(0))
        assertTrue(root.mouseReleased(50.0, 50.0, 0))
        assertFalse(root.hasMouseCapture(0))
        assertTrue(fixture.widgets[0].releases.isEmpty())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `描画専用の登録が残っても入力の登録を解除した子へ通知しないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        val renderRemover = fixture.widgetContexts[0].addRenderableOnly(fixture.widgets[0])
        assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))

        fixture.removers[0].remove()

        assertEquals(fixture.widgets.drop(1), root.children())
        assertNull(root.focused)
        assertFalse(root.charTyped('a', 0))
        assertTrue(root.mouseReleased(50.0, 50.0, 0))
        assertTrue(fixture.widgets[0].releases.isEmpty())
        renderRemover.remove()
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `同じ Widget の重複登録でも解除した登録の押下先だけを失効させるのだ`() {
        listOf(false, true).forEach { removePressedRegistration ->
            val fixture = ScrollFixture()
            val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
            val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
            val widget = fixture.widgets[0]
            val duplicateRemover = fixture.widgetContexts[0].addRenderableWidget(widget)
            assertEquals(fixture.widgets + widget, root.children())
            assertTrue(root.mouseClicked(50.0, 50.0, 0))

            if (removePressedRegistration) {
                fixture.removers[0].remove()
            } else {
                duplicateRemover.remove()
            }

            assertEquals(if (removePressedRegistration) fixture.widgets.drop(1) + widget else fixture.widgets, root.children())
            assertEquals(widget, root.focused)
            assertTrue(root.hasMouseCapture(0))
            root.mouseMoved(-100.0, -100.0)
            assertTrue(root.mouseDragged(-100.0, -100.0, 0, 1.0, 1.0))
            assertTrue(root.mouseReleased(-100.0, -100.0, 0))
            assertFalse(root.hasMouseCapture(0))
            assertEquals(if (removePressedRegistration) emptyList<Int>() else listOf(0), widget.drags)
            assertEquals(if (removePressedRegistration) emptyList<Int>() else listOf(0), widget.releases)
            assertEquals(if (removePressedRegistration) emptyList<Pair<Double, Double>>() else listOf(Pair(-100.0, -100.0)), widget.moves)

            if (removePressedRegistration) {
                duplicateRemover.remove()
            } else {
                fixture.removers[0].remove()
            }

            assertEquals(fixture.widgets.drop(1), root.children())
            assertNull(root.focused)
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `読み上げ候補を表示領域で絞らず有効な子から選ぶのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.widgets[2].priority = NarratableEntry.NarrationPriority.HOVERED
        assertTrue(fixture.widgets[2].y >= root.y + root.height)
        assertEquals(NarratableEntry.NarrationPriority.HOVERED, root.narrationPriority())

        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        val text = narration.collectNarrationText(true)

        assertTrue(text.contains("child-2"))
        assertFalse(text.contains("child-0"))
        fixture.widgets[2].active = false
        assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `読み上げ候補がなければ領域自身のフォーカスを優先度へ含めないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        root.isFocused = true

        assertTrue(root.isFocused)
        assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        assertEquals("", narration.collectNarrationText(true))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `同じ優先度の子は登録順ではなく tabOrderGroup の順で読み上げるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.widgets[0].priority = NarratableEntry.NarrationPriority.HOVERED
        fixture.widgets[0].tabOrderGroup = 2
        fixture.widgets[2].priority = NarratableEntry.NarrationPriority.HOVERED
        fixture.widgets[2].tabOrderGroup = 1

        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        val text = narration.collectNarrationText(true)

        assertTrue(text.contains("child-2"))
        assertFalse(text.contains("child-0"))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `最後の入力登録が消えるまで以前の読み上げ対象を保持するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        val widget = fixture.widgets[0]
        widget.priority = NarratableEntry.NarrationPriority.FOCUSED
        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        assertTrue(narration.collectNarrationText(true).contains("child-0"))
        val duplicateRemover = fixture.widgetContexts[0].addRenderableWidget(widget)
        fixture.widgets[1].priority = NarratableEntry.NarrationPriority.HOVERED

        fixture.removers[0].remove()
        narration.update { root.updateNarration(it) }
        val retainedText = narration.collectNarrationText(true)
        assertTrue(retainedText.contains("child-1"))
        assertFalse(retainedText.contains("child-0"))

        duplicateRemover.remove()
        val newRemover = fixture.widgetContexts[0].addRenderableWidget(widget)
        narration.update { root.updateNarration(it) }
        val newText = narration.collectNarrationText(true)
        assertTrue(newText.contains("child-0"))
        assertFalse(newText.contains("child-1"))
        newRemover.remove()
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `フォーカスした子を優先して読み上げるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.widgets[2].priority = NarratableEntry.NarrationPriority.HOVERED
        fixture.focusNext(true)
        assertEquals(NarratableEntry.NarrationPriority.FOCUSED, root.narrationPriority())
        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        val text = narration.collectNarrationText(true)
        assertTrue(text.contains("child-0"))
        assertFalse(text.contains("child-2"))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `ホイールで表示範囲外へ移動したフォーカス先も読み上げるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.focusNext(true)
        assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1000.0))
        assertTrue(fixture.widgets[0].isFocused)
        assertEquals(NarratableEntry.NarrationPriority.FOCUSED, root.narrationPriority())

        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        val text = narration.collectNarrationText(true)

        assertTrue(text.contains("child-0"))
        assertFalse(text.contains("child-2"))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `描画専用の子を入力とフォーカスへ登録しないのだ`() {
        val fixture = ScrollFixture()
        val view = YScrollView()
        view += ScrollTestView(false)
        val controller = fixture.attach(view, IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(root.children().isEmpty())
        assertFalse(root.mouseClicked(50.0, 50.0, 0))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `親の移動と破棄と配置解除を子へ反映するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        root.mouseScrolled(50.0, 50.0, 0.0, -1.0)
        controller.setOffset(IntPoint(30, 40))
        assertEquals(IntPoint(30, 25), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
        assertTrue(root.mouseClicked(50.0, 50.0, 0))
        assertTrue(root.hasMouseCapture(0))
        controller.dispose()
        assertEquals(3, fixture.disposals)
        assertFalse(root.isActive)
        assertFalse(root.hasMouseCapture(0))
        assertFalse(root.mouseDragged(50.0, 50.0, 0, 1.0, 1.0))
        assertFalse(root.mouseReleased(50.0, 50.0, 0))
        assertTrue(fixture.widgets[0].drags.isEmpty())
        assertTrue(fixture.widgets[0].releases.isEmpty())
        assertFalse(root.mouseClicked(50.0, 50.0, 0))
        controller.detach()
        assertTrue(root.children().isEmpty())
        assertTrue(fixture.screenWidgets.isEmpty())
    }

    @Test
    fun `Slot の配置を例外で拒否するのだ`() {
        val fixture = ScrollFixture()
        val view = YScrollView()
        view += YListView().also {
            it += ScrollTestView(true)
            it += MenuSlotView(Slot(null, 0, 0, 0))
        }
        val exception = assertThrows(UnsupportedOperationException::class.java) { fixture.attach(view, IntPoint(100, 100)) }
        assertEquals("MenuSlotView cannot be placed inside a scroll view", exception.message)
        assertEquals(0, fixture.slotPlacements)
    }

    @Test
    fun `入れ子でも外側の表示領域外へクリックとホイールを渡さないのだ`() {
        val fixture = ScrollFixture()
        val outer = YScrollView()
        outer += MinContentSizeYView(200).also { it += createScrollView(false) }
        val controller = fixture.attach(outer, IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertFalse(root.mouseClicked(50.0, 150.0, 0))
        assertFalse(root.mouseScrolled(50.0, 150.0, 0.0, -1.0))
        assertTrue(root.getChildAt(50.0, 150.0).isEmpty)
        assertTrue(fixture.widgets.all { it.clicks.isEmpty() && it.scrolls == 0 })
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `縦横のスクロールを入れ子にして両軸を独立に移動するのだ`() {
        val fixture = ScrollFixture()
        val outer = XScrollView()
        outer += createScrollView(false)
        val controller = fixture.attach(outer, IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(root.mouseScrolled(50.0, 50.0, -1.0, 0.0))
        assertEquals(IntPoint(5, 20), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
        assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
        assertEquals(IntPoint(5, 5), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `子が消費したキーではスクロールせず未処理のページ移動キーだけを使うのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(fixture.focusNext(true))
        assertTrue(root.keyPressed(GLFW.GLFW_KEY_PAGE_DOWN, 0, 0))
        assertEquals(-60, fixture.widgets[0].y)

        fixture.widgets[0].consumeKeys = true
        assertTrue(root.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
        assertEquals(-60, fixture.widgets[0].y)

        fixture.widgets[0].consumeKeys = false
        assertTrue(root.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
        assertEquals(20, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `入力する子がなくても領域自体をキーでスクロールできるのだ`() {
        val fixture = ScrollFixture()
        val view = YScrollView()
        view += YListView().also { list ->
            repeat(3) {
                list += ScrollTestView(false)
            }
        }
        val controller = fixture.attach(view, IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertTrue(fixture.focusNext(true))
        assertTrue(root.isFocused)
        assertTrue(root.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
        assertEquals(-180, fixture.widgets[0].y)
        assertTrue(root.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
        assertEquals(20, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `表示領域がゼロでもバーの除算やクリックを行わないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(0, 0))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        assertFalse(root.mouseClicked(10.0, 20.0, 0))
        assertFalse(root.mouseScrolled(10.0, 20.0, 0.0, -1.0))
        controller.dispose()
        controller.detach()
    }

}
