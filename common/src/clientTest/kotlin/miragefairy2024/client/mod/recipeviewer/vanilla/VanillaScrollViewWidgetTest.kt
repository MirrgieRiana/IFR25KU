package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.systems.RenderSystem
import miragefairy2024.mod.recipeviewer.SCROLL_AREA_TRANSLATION
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.MinContentSizeYView
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XScrollView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import miragefairy2024.util.empty
import miragefairy2024.util.invoke
import miragefairy2024.util.register
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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

private val SCROLL_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = throw UnsupportedOperationException()
    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
}

private class ScrollTestView(val interactive: Boolean) : FixedWidgetView(IntPoint(100, 100))

private class ScrollTestWidget(bounds: IntRectangle, private val label: String) : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
    val initialBounds = bounds
    val offsets = mutableListOf<IntPoint>()
    val clicks = mutableListOf<Int>()
    val moves = mutableListOf<Pair<Double, Double>>()
    val drags = mutableListOf<Int>()
    val releases = mutableListOf<Int>()
    var scrolls = 0
    var consumeScroll = false
    var consumeKeys = false
    var characters = ""
    var onClick: () -> Unit = { }
    var onRender: (GuiGraphics, Int, Int, Float) -> Unit = { _, _, _, _ -> }
    var priority = NarratableEntry.NarrationPriority.NONE

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = onRender(guiGraphics, mouseX, mouseY, partialTick)
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
    var onChildPlaced: () -> Unit = { }
    var disposals = 0
    val screen: Screen = object : Screen(text { empty() }) {
        override fun children() = screenWidgets
    }.also {
        it.width = 300
        it.height = 300
    }
    private val context: VanillaViewPlacerContext = object : VanillaViewPlacerContext {
        override val origin = IntPoint(10, 20)
        override val canPlaceMenuSlots = true

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
            onChildPlaced()
            object : ViewController {
                override fun setOffset(offset: IntPoint) {
                    widget.offsets += offset
                    widget.setPosition(offset.x, offset.y)
                }

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

private val ScrollView.scrollOffsetValue
    get() = when (this) {
        is XScrollView -> this.scrollOffsetX
        is YScrollView -> this.scrollOffsetY
        else -> throw AssertionError("Unknown scroll axis")
    }

class VanillaScrollViewWidgetTest {

    @Test
    fun `View の初期位置と配置後の位置変更を小数のまま保持して子の配置へ反映するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = createScrollView(horizontal)
            Assertions.assertEquals(0.0, view.scrollOffsetValue.value)
            view.scrollOffsetValue.value = 45.25
            val controller = fixture.attach(view, IntPoint(100, 100))
            try {
                Assertions.assertEquals(45.25, view.scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-35, 20) else IntPoint(10, -25), IntPoint(fixture.widgets[0].initialBounds.x, fixture.widgets[0].initialBounds.y))
                Assertions.assertTrue(fixture.widgets.all { it.offsets.isEmpty() })
                Assertions.assertEquals(if (horizontal) IntPoint(-35, 20) else IntPoint(10, -25), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))

                view.scrollOffsetValue.value = 71.75

                Assertions.assertEquals(71.75, view.scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-62, 20) else IntPoint(10, -52), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                controller.setOffset(IntPoint(30, 40))
                Assertions.assertEquals(if (horizontal) IntPoint(-42, 40) else IntPoint(30, -32), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                Assertions.assertEquals(71.75, view.scrollOffsetValue.value)
            } finally {
                controller.dispose()
                controller.detach()
            }
        }
    }

    @Test
    fun `範囲外の指定値を保持し初回配置と位置変更と親の移動では範囲内に収めるのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(-50.0, 1000.0).forEach { initialOffset ->
                val fixture = ScrollFixture()
                val view = createScrollView(horizontal)
                view.scrollOffsetValue.value = initialOffset
                val controller = fixture.attach(view, IntPoint(100, 100))
                try {
                    val expectedOffset = if (initialOffset < 0.0) 0.0 else 200.0
                    Assertions.assertEquals(initialOffset, view.scrollOffsetValue.value)
                    Assertions.assertEquals(if (horizontal) 10 - expectedOffset.toInt() else 20 - expectedOffset.toInt(), if (horizontal) fixture.widgets[0].initialBounds.x else fixture.widgets[0].initialBounds.y)
                    Assertions.assertTrue(fixture.widgets.all { it.offsets.isEmpty() })
                    Assertions.assertEquals(if (horizontal) 10 - expectedOffset.toInt() else 20 - expectedOffset.toInt(), if (horizontal) fixture.widgets[0].x else fixture.widgets[0].y)

                    view.scrollOffsetValue.value = -1000.0
                    Assertions.assertEquals(-1000.0, view.scrollOffsetValue.value)
                    Assertions.assertEquals(IntPoint(10, 20), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                    fixture.widgets.forEach { it.offsets.clear() }
                    view.scrollOffsetValue.value = 1000.0
                    Assertions.assertEquals(1000.0, view.scrollOffsetValue.value)
                    Assertions.assertEquals(if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                    Assertions.assertTrue(fixture.widgets.all { it.offsets.size == 1 })
                    controller.setOffset(IntPoint(30, 40))
                    Assertions.assertEquals(1000.0, view.scrollOffsetValue.value)
                    Assertions.assertEquals(if (horizontal) IntPoint(-170, 40) else IntPoint(30, -160), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                } finally {
                    controller.dispose()
                    controller.detach()
                }
            }
        }
    }

    @Test
    fun `子の配置中に変更した指定値を保持し購読開始後の表示位置に反映するのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(Pair(71.75, 71.75), Pair(1000.0, 200.0), Pair(-1000.0, 0.0)).forEach { (requestedOffset, expectedOffset) ->
                val fixture = ScrollFixture()
                val view = createScrollView(horizontal)
                fixture.onChildPlaced = {
                    if (fixture.widgets.size == 1) {
                        view.scrollOffsetValue.value = requestedOffset
                        Assertions.assertEquals(requestedOffset, view.scrollOffsetValue.value)
                        Assertions.assertTrue(fixture.widgets.all { it.offsets.isEmpty() })
                    }
                }
                val controller = fixture.attach(view, IntPoint(100, 100))
                try {
                    Assertions.assertEquals(requestedOffset, view.scrollOffsetValue.value)
                    Assertions.assertEquals(IntPoint(10, 20), IntPoint(fixture.widgets[0].initialBounds.x, fixture.widgets[0].initialBounds.y))
                    val expectedPosition = if (horizontal) IntPoint(10 - expectedOffset.roundToInt(), 20) else IntPoint(10, 20 - expectedOffset.roundToInt())
                    Assertions.assertEquals(expectedPosition, IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                    val expectedOffsets = if (expectedOffset == 0.0) emptyList() else listOf(expectedPosition)
                    Assertions.assertEquals(expectedOffsets, fixture.widgets[0].offsets)
                    Assertions.assertTrue(fixture.widgets.all { it.offsets.size == expectedOffsets.size })
                } finally {
                    controller.dispose()
                    controller.detach()
                }
                Assertions.assertTrue(view.scrollOffsetValue.listeners.isEmpty())
            }
        }
    }

    @Test
    fun `ホイールとキー操作による位置変更を View から取得できるのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = createScrollView(horizontal)
            val controller = fixture.attach(view, IntPoint(100, 100))
            try {
                val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                val offsets = mutableListOf<Double>()
                val remover = view.scrollOffsetValue.register { _, offset ->
                    offsets += offset
                }
                try {
                    Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -0.25))
                    Assertions.assertEquals(3.75, view.scrollOffsetValue.value)
                    Assertions.assertEquals(if (horizontal) IntPoint(6, 20) else IntPoint(10, 16), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))

                    root.isFocused = true
                    Assertions.assertTrue(root.keyPressed(if (horizontal) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_DOWN, 0, 0))
                    Assertions.assertEquals(18.75, view.scrollOffsetValue.value)
                    Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_PAGE_DOWN, 0, 0))
                    Assertions.assertEquals(98.75, view.scrollOffsetValue.value)
                    Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                    Assertions.assertEquals(200.0, view.scrollOffsetValue.value)
                    Assertions.assertFalse(root.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                    Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                    Assertions.assertEquals(0.0, view.scrollOffsetValue.value)
                    Assertions.assertEquals(listOf(3.75, 18.75, 98.75, 200.0, 0.0), offsets)
                } finally {
                    remover.remove()
                }
            } finally {
                controller.dispose()
                controller.detach()
            }
        }
    }

    @Test
    fun `範囲外の指定でもホイールとキーは表示位置から移動し端の入力を消費しないのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(-1000.0, 1000.0).forEach { initialOffset ->
                val fixture = ScrollFixture()
                val view = createScrollView(horizontal)
                view.scrollOffsetValue.value = initialOffset
                val controller = fixture.attach(view, IntPoint(100, 100))
                try {
                    val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                    val outwardScroll = if (initialOffset < 0.0) 1.0 else -1.0
                    val expectedOutwardOffset = if (initialOffset < 0.0) -15.0 else 215.0
                    val expectedInwardOffset = if (initialOffset < 0.0) 15.0 else 185.0
                    val initialPosition = IntPoint(fixture.widgets[0].x, fixture.widgets[0].y)
                    Assertions.assertFalse(root.mouseScrolled(50.0, 50.0, 0.0, outwardScroll))
                    Assertions.assertEquals(expectedOutwardOffset, view.scrollOffsetValue.value)
                    Assertions.assertEquals(initialPosition, IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                    Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -outwardScroll))
                    Assertions.assertEquals(expectedInwardOffset, view.scrollOffsetValue.value)
                    Assertions.assertEquals(if (horizontal) IntPoint(10 - expectedInwardOffset.toInt(), 20) else IntPoint(10, 20 - expectedInwardOffset.toInt()), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))

                    view.scrollOffsetValue.value = initialOffset
                    root.isFocused = true
                    val backwardKey = if (horizontal) GLFW.GLFW_KEY_LEFT else GLFW.GLFW_KEY_UP
                    val forwardKey = if (horizontal) GLFW.GLFW_KEY_RIGHT else GLFW.GLFW_KEY_DOWN
                    Assertions.assertFalse(root.keyPressed(if (initialOffset < 0.0) backwardKey else forwardKey, 0, 0))
                    Assertions.assertEquals(expectedOutwardOffset, view.scrollOffsetValue.value)
                    Assertions.assertEquals(initialPosition, IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
                    Assertions.assertTrue(root.keyPressed(if (initialOffset < 0.0) forwardKey else backwardKey, 0, 0))
                    Assertions.assertEquals(expectedInwardOffset, view.scrollOffsetValue.value)

                    view.scrollOffsetValue.value = initialOffset
                    Assertions.assertTrue(root.keyPressed(if (initialOffset < 0.0) GLFW.GLFW_KEY_PAGE_DOWN else GLFW.GLFW_KEY_PAGE_UP, 0, 0))
                    Assertions.assertEquals(if (initialOffset < 0.0) 80.0 else 120.0, view.scrollOffsetValue.value)
                } finally {
                    controller.dispose()
                    controller.detach()
                }
            }
        }
    }

    @Test
    fun `再配置後も指定位置を保持し各表示領域に応じて適用するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = createScrollView(horizontal)
            view.scrollOffsetValue.value = 175.25
            val controller = fixture.attach(view, IntPoint(100, 100))
            controller.dispose()
            controller.detach()
            Assertions.assertEquals(175.25, view.scrollOffsetValue.value)
            Assertions.assertTrue(view.scrollOffsetValue.listeners.isEmpty())
            val previousPositions = fixture.widgets.map { IntPoint(it.x, it.y) }

            val newController = fixture.attach(view, IntPoint(200, 200))
            try {
                Assertions.assertEquals(175.25, view.scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-90, 20) else IntPoint(10, -80), IntPoint(fixture.widgets[3].x, fixture.widgets[3].y))
                Assertions.assertEquals(previousPositions, fixture.widgets.take(3).map { IntPoint(it.x, it.y) })
            } finally {
                newController.dispose()
                newController.detach()
            }
            Assertions.assertTrue(view.scrollOffsetValue.listeners.isEmpty())

            val restoredController = fixture.attach(view, IntPoint(100, 100))
            try {
                Assertions.assertEquals(175.25, view.scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-165, 20) else IntPoint(10, -155), IntPoint(fixture.widgets[6].x, fixture.widgets[6].y))
                view.scrollOffsetValue.value = 25.75
                Assertions.assertEquals(if (horizontal) IntPoint(-16, 20) else IntPoint(10, -6), IntPoint(fixture.widgets[6].x, fixture.widgets[6].y))
                Assertions.assertEquals(previousPositions, fixture.widgets.take(3).map { IntPoint(it.x, it.y) })
            } finally {
                restoredController.dispose()
                restoredController.detach()
            }
            Assertions.assertTrue(view.scrollOffsetValue.listeners.isEmpty())
        }
    }

    @Test
    fun `View を再生成する呼び出し側が指定位置を引き継ぎ短い内容でも指定値を保持するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = createScrollView(horizontal)
            val controller = fixture.attach(view, IntPoint(100, 100))
            val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
            Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -5.25))
            val savedOffset = view.scrollOffsetValue.value
            controller.dispose()
            controller.detach()

            val newView = createScrollView(horizontal)
            newView.scrollOffsetValue.value = savedOffset
            val newController = fixture.attach(newView, IntPoint(100, 100))
            try {
                Assertions.assertEquals(78.75, newView.scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-69, 20) else IntPoint(10, -59), IntPoint(fixture.widgets[3].x, fixture.widgets[3].y))
            } finally {
                newController.dispose()
                newController.detach()
            }

            val shorterView = if (horizontal) XScrollView() else YScrollView()
            shorterView += ScrollTestView(true)
            shorterView.scrollOffsetValue.value = newView.scrollOffsetValue.value
            val shorterController = fixture.attach(shorterView, IntPoint(100, 100))
            try {
                Assertions.assertEquals(78.75, shorterView.scrollOffsetValue.value)
                Assertions.assertEquals(IntPoint(10, 20), IntPoint(fixture.widgets[6].x, fixture.widgets[6].y))
            } finally {
                shorterController.dispose()
                shorterController.detach()
            }
        }
    }

    @Test
    fun `通知中に位置が変更された場合も最後の View の値で配置するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = createScrollView(horizontal)
            val remover = view.scrollOffsetValue.register { _, offset ->
                if (offset > 100.0) view.scrollOffsetValue.value = 33.25
            }
            val controller = fixture.attach(view, IntPoint(100, 100))
            try {
                view.scrollOffsetValue.value = 150.0
                Assertions.assertEquals(33.25, view.scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-23, 20) else IntPoint(10, -13), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
            } finally {
                controller.dispose()
                controller.detach()
                remover.remove()
            }
        }
    }

    @Test
    fun `範囲外の指定値でもスクロールバーを表示領域の端に描画するのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(-1000.0, 1000.0).forEach { initialOffset ->
                GuiTestEnvironment().use { environment ->
                    val fixture = ScrollFixture()
                    val view = createScrollView(horizontal)
                    view.scrollOffsetValue.value = initialOffset
                    val controller = fixture.attach(view, IntPoint(100, 100))
                    try {
                        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                        root.render(environment.graphics, 50, 50, 0.0F)
                        val expectedBounds = if (horizontal) IntRectangle(if (initialOffset < 0.0) 10 else 77, 115, 33, 5) else IntRectangle(105, if (initialOffset < 0.0) 20 else 87, 5, 33)
                        Assertions.assertEquals(listOf(expectedBounds), environment.fills)
                        Assertions.assertEquals(initialOffset, view.scrollOffsetValue.value)
                    } finally {
                        controller.dispose()
                        controller.detach()
                    }
                }
            }
        }
    }

    @Test
    fun `描画には実座標を渡し表示領域外のホバーをバニラの scissor で除外するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val fixture = ScrollFixture()
                val controller = fixture.attach(createScrollView(horizontal), IntPoint(100, 100))
                try {
                    val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                    val positions = mutableListOf<IntPoint>()
                    fixture.widgets.forEach { widget ->
                        widget.onRender = { _, mouseX, mouseY, _ ->
                            positions += IntPoint(mouseX, mouseY)
                            environment.events += "child"
                        }
                    }
                    root.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertTrue(fixture.widgets[0].isHovered)
                    Assertions.assertEquals(List(3) { IntPoint(50, 50) }, positions)
                    Assertions.assertEquals(listOf("child", "child", "child", "fill"), environment.events)

                    positions.clear()
                    val mouse = if (horizontal) IntPoint(50, 118) else IntPoint(108, 50)
                    root.render(environment.graphics, mouse.x, mouse.y, 0.0F)
                    Assertions.assertFalse(fixture.widgets[0].isHovered)
                    Assertions.assertEquals(List(3) { mouse }, positions)
                    Assertions.assertTrue(environment.graphics.containsPointInScissor(0, 0))
                } finally {
                    controller.dispose()
                    controller.detach()
                }
            }
        }
    }

    @Test
    fun `表示領域を拡大したら子とバーを描画前に現在範囲へ戻すのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val fixture = ScrollFixture()
                val controller = fixture.attach(createScrollView(horizontal), IntPoint(100, 100))
                try {
                    val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                    val renderedPositions = mutableListOf<IntPoint>()
                    fixture.widgets[0].onRender = { _, _, _, _ ->
                        renderedPositions += IntPoint(fixture.widgets[0].x, fixture.widgets[0].y)
                    }
                    Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1000.0))
                    root.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180), renderedPositions.last())
                    Assertions.assertEquals(if (horizontal) IntRectangle(77, 115, 33, 5) else IntRectangle(105, 87, 5, 33), environment.fills.last())

                    root.setSize(if (horizontal) 250 else 100, if (horizontal) 100 else 250)
                    root.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(if (horizontal) IntPoint(-40, 20) else IntPoint(10, -30), renderedPositions.last())
                    Assertions.assertEquals(if (horizontal) IntRectangle(52, 115, 208, 5) else IntRectangle(105, 62, 5, 208), environment.fills.last())

                    root.setSize(if (horizontal) 300 else 100, if (horizontal) 100 else 300)
                    environment.fills.clear()
                    root.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(IntPoint(10, 20), renderedPositions.last())
                    Assertions.assertTrue(environment.fills.isEmpty())

                    root.setSize(100, 100)
                    root.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(IntPoint(10, 20), renderedPositions.last())
                    Assertions.assertEquals(listOf(if (horizontal) IntRectangle(10, 115, 33, 5) else IntRectangle(105, 20, 5, 33)), environment.fills)
                } finally {
                    controller.dispose()
                    controller.detach()
                }
            }
        }
    }

    @Test
    fun `親の scissor との交差領域を GPU へ渡し描画後に親の領域を復元するのだ`() {
        GuiTestEnvironment().use { environment ->
            val fixture = ScrollFixture()
            val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
            environment.graphics.enableScissor(30, 40, 70, 80)
            try {
                val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                root.render(environment.graphics, 35, 45, 0.0F)
                Assertions.assertTrue(fixture.widgets[0].isHovered)
                root.render(environment.graphics, 90, 45, 0.0F)
                Assertions.assertFalse(fixture.widgets[0].isHovered)
                environment.renderSystem.verify({ RenderSystem.enableScissor(30, 220, 40, 40) }, Mockito.times(5))
                environment.renderSystem.verify({ RenderSystem.disableScissor() }, Mockito.never())
                Assertions.assertFalse(environment.graphics.containsPointInScissor(20, 45))
                Assertions.assertTrue(environment.graphics.containsPointInScissor(35, 45))
            } finally {
                environment.graphics.disableScissor()
                controller.dispose()
                controller.detach()
            }
            Assertions.assertTrue(environment.graphics.containsPointInScissor(0, 0))
        }
    }

    @Test
    fun `子の描画で例外が発生しても scissor を解除するのだ`() {
        GuiTestEnvironment().use { environment ->
            val fixture = ScrollFixture()
            val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
            try {
                val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                val failure = IllegalStateException("Rendering failed")
                fixture.widgets[0].onRender = { _, _, _, _ -> throw failure }
                val exception = Assertions.assertThrows(IllegalStateException::class.java) {
                    root.render(environment.graphics, 50, 50, 0.0F)
                }
                Assertions.assertSame(failure, exception)
                environment.renderSystem.verify { RenderSystem.disableScissor() }
                Assertions.assertTrue(environment.graphics.containsPointInScissor(0, 0))
                Assertions.assertTrue(environment.fills.isEmpty())
            } finally {
                controller.dispose()
                controller.detach()
            }
        }
    }

    @Test
    fun `子を画面へ個別登録せずスクロール端ではホイールを親へ返すのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val controller = fixture.attach(createScrollView(horizontal), IntPoint(100, 100))
            val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
            Assertions.assertEquals(3, root.children().size)
            fixture.widgets[0].consumeScroll = true
            Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
            Assertions.assertEquals(IntPoint(10, 20), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
            fixture.widgets[0].consumeScroll = false
            Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1000.0))
            val expected = if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180)
            Assertions.assertEquals(expected, IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
            Assertions.assertFalse(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
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

        Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))

        Assertions.assertEquals(1, fixture.widgets[0].scrolls)
        Assertions.assertEquals(0, fixture.widgets[1].scrolls)
        Assertions.assertEquals(5, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `一部だけ見えている子をクリックしても表示位置を変更しないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
        Assertions.assertEquals(5, fixture.widgets[0].y)

        Assertions.assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))

        Assertions.assertEquals(5, fixture.widgets[0].y)
        Assertions.assertTrue(fixture.widgets[0].isFocused)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `領域外のクリックを除外し押下先へドラッグと解放を届けるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertFalse(root.mouseClicked(50.0, 150.0, 0))
        Assertions.assertTrue(root.getChildAt(50.0, 150.0).isEmpty)
        Assertions.assertTrue(root.getChildAt(107.0, 50.0).isEmpty)
        Assertions.assertTrue(root.getChildAt(50.0, 50.0).isPresent)
        Assertions.assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        Assertions.assertTrue(root.mouseDragged(-100.0, -100.0, 0, 1.0, 1.0))
        Assertions.assertTrue(root.mouseReleased(-100.0, -100.0, 0))
        Assertions.assertEquals(listOf(0), fixture.widgets[0].drags)
        Assertions.assertEquals(listOf(0), fixture.widgets[0].releases)
        Assertions.assertFalse(root.mouseReleased(-100.0, -100.0, 0))
        Assertions.assertFalse(root.mouseDragged(-100.0, -100.0, 0, 1.0, 1.0))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `領域外の移動は各ボタンの押下先だけへ実座標で届けるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget

        root.mouseMoved(50.0, 150.0)
        Assertions.assertTrue(fixture.widgets.all { it.moves.isEmpty() })
        root.mouseMoved(50.0, 50.0)
        fixture.widgets.forEach {
            Assertions.assertEquals(listOf(Pair(50.0, 50.0)), it.moves)
        }

        Assertions.assertTrue(root.mouseClicked(50.0, 50.0, 0))
        Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -100.0 / 15))
        Assertions.assertTrue(root.mouseClicked(50.0, 50.0, 1))
        root.mouseMoved(-100.0, -100.0)
        Assertions.assertEquals(listOf(Pair(50.0, 50.0), Pair(-100.0, -100.0)), fixture.widgets[0].moves)
        Assertions.assertEquals(listOf(Pair(50.0, 50.0), Pair(-100.0, -100.0)), fixture.widgets[1].moves)
        Assertions.assertEquals(listOf(Pair(50.0, 50.0)), fixture.widgets[2].moves)

        Assertions.assertTrue(root.mouseReleased(-100.0, -100.0, 0))
        root.mouseMoved(-200.0, -200.0)
        Assertions.assertEquals(Pair(-100.0, -100.0), fixture.widgets[0].moves.last())
        Assertions.assertEquals(Pair(-200.0, -200.0), fixture.widgets[1].moves.last())
        Assertions.assertEquals(listOf(Pair(50.0, 50.0)), fixture.widgets[2].moves)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `解放を受け取らず領域外で再び押した場合は前回の押下先を解除するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertTrue(root.mouseClicked(50.0, 50.0, 0))

        Assertions.assertFalse(root.mouseClicked(200.0, 200.0, 0))

        Assertions.assertFalse(root.hasMouseCapture(0))
        Assertions.assertFalse(root.isDragging)
        Assertions.assertFalse(root.mouseReleased(200.0, 200.0, 0))
        Assertions.assertTrue(fixture.widgets[0].releases.isEmpty())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `複数のボタンの解放をそれぞれの押下先へ届けるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertTrue(root.mouseClicked(50.0, 50.0, 0))
        Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -100.0 / 15))
        Assertions.assertTrue(root.mouseClicked(50.0, 50.0, 1))
        Assertions.assertTrue(root.mouseReleased(-100.0, -100.0, 0))
        Assertions.assertTrue(root.mouseDragged(-100.0, -100.0, 1, 1.0, 1.0))
        Assertions.assertTrue(root.mouseReleased(-100.0, -100.0, 1))
        Assertions.assertEquals(listOf(0), fixture.widgets[0].releases)
        Assertions.assertEquals(listOf(1), fixture.widgets[1].releases)
        Assertions.assertEquals(listOf(1), fixture.widgets[1].drags)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `縦横のバーをドラッグして領域外で離せるのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = createScrollView(horizontal)
            val controller = fixture.attach(view, IntPoint(100, 100))
            val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
            val mouseX = if (horizontal) 20.0 else 107.0
            val mouseY = if (horizontal) 117.0 else 30.0
            Assertions.assertTrue(fixture.screen.mouseClicked(mouseX, mouseY, 0))
            Assertions.assertTrue(root.mouseDragged(400.0, 400.0, 0, 300.0, 300.0))
            Assertions.assertEquals(if (horizontal) -190 else -180, if (horizontal) fixture.widgets[0].x else fixture.widgets[0].y)
            Assertions.assertTrue(view.scrollOffsetValue.value > 200.0)
            Assertions.assertTrue(root.mouseReleased(400.0, 400.0, 0))
            Assertions.assertFalse(root.hasMouseCapture(0))
            Assertions.assertFalse(root.mouseDragged(mouseX, mouseY, 0, 1.0, 1.0))
            controller.dispose()
            controller.detach()
        }
    }

    @Test
    fun `ドラッグ中に太さが表示領域長以上になっても位置を壊さず解放するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val fixture = ScrollFixture()
                val view = createScrollView(horizontal)
                val controller = fixture.attach(view, IntPoint(100, 100))
                try {
                    val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
                    Assertions.assertTrue(root.mouseClicked(if (horizontal) 20.0 else 107.0, if (horizontal) 117.0 else 30.0, 0))
                    view.scrollbarThickness = 0
                    Assertions.assertTrue(root.mouseDragged(400.0, 400.0, 0, 300.0, 300.0))
                    val expected = if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180)
                    Assertions.assertEquals(expected, IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))

                    view.scrollbarThickness = 100
                    Assertions.assertTrue(root.mouseDragged(-100.0, -100.0, 0, -500.0, -500.0))
                    Assertions.assertTrue(root.hasMouseCapture(0))
                    Assertions.assertEquals(expected, IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))

                    view.scrollbarThickness = 5
                    root.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(if (horizontal) IntRectangle(77, 115, 33, 5) else IntRectangle(105, 87, 5, 33), environment.fills.last())
                    view.scrollbarThickness = 0
                    Assertions.assertTrue(root.mouseReleased(-100.0, -100.0, 0))
                    Assertions.assertFalse(root.hasMouseCapture(0))
                    Assertions.assertFalse(root.mouseDragged(50.0, 50.0, 0, 1.0, 1.0))
                } finally {
                    controller.dispose()
                    controller.detach()
                }
            }
        }
    }

    @Test
    fun `スクロールバーの操作で子のフォーカスを解除しないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertTrue(fixture.focusNext(true))

        Assertions.assertTrue(fixture.screen.mouseClicked(107.0, 30.0, 0))
        Assertions.assertTrue(root.mouseDragged(107.0, 100.0, 0, 0.0, 70.0))
        Assertions.assertTrue(root.mouseReleased(107.0, 100.0, 0))

        Assertions.assertEquals(fixture.widgets[0], root.focused)
        Assertions.assertTrue(fixture.widgets[0].isFocused)
        Assertions.assertTrue(fixture.widgets[0].releases.isEmpty())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `範囲外の指定値からフォーカス移動すると表示位置を基準に子を表示するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = createScrollView(horizontal)
            view.scrollOffsetValue.value = 1000.0
            val controller = fixture.attach(view, IntPoint(100, 100))
            try {
                Assertions.assertTrue(fixture.focusNext(true))
                Assertions.assertTrue(fixture.widgets[0].isFocused)
                Assertions.assertEquals(0.0, view.scrollOffsetValue.value)
                Assertions.assertEquals(IntPoint(10, 20), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
            } finally {
                controller.dispose()
                controller.detach()
            }
        }
    }

    @Test
    fun `Tab と逆 Tab で画面外の子へ移動して表示するのだ`() {
        val fixture = ScrollFixture()
        val view = createScrollView(false)
        val controller = fixture.attach(view, IntPoint(100, 100))
        Assertions.assertTrue(fixture.focusNext(true))
        Assertions.assertTrue(fixture.widgets[0].isFocused)
        Assertions.assertTrue(fixture.focusNext(true))
        Assertions.assertTrue(fixture.widgets[1].isFocused)
        Assertions.assertEquals(20, fixture.widgets[1].y)
        Assertions.assertEquals(100.0, view.scrollOffsetValue.value)
        Assertions.assertTrue(fixture.focusNext(true))
        Assertions.assertTrue(fixture.widgets[2].isFocused)
        Assertions.assertEquals(20, fixture.widgets[2].y)
        Assertions.assertEquals(200.0, view.scrollOffsetValue.value)
        Assertions.assertFalse(fixture.focusNext(true))
        Assertions.assertTrue(fixture.focusNext(false))
        Assertions.assertTrue(fixture.widgets[1].isFocused)
        Assertions.assertEquals(20, fixture.widgets[1].y)
        Assertions.assertEquals(100.0, view.scrollOffsetValue.value)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `外側をマウスで選択した後に逆 Tab で戻ると保持していた子を選択するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val outside = ScrollTestWidget(IntRectangle(200, 20, 50, 50), "outside")
        fixture.screenWidgets += outside
        Assertions.assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        Assertions.assertTrue(fixture.widgets[0].isFocused)
        Assertions.assertTrue(fixture.screen.mouseClicked(210.0, 30.0, 0))
        Assertions.assertFalse(fixture.widgets[0].isFocused)

        Assertions.assertTrue(fixture.focusNext(false))

        Assertions.assertTrue(fixture.widgets[0].isFocused)
        Assertions.assertEquals(20, fixture.widgets[0].y)
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
        Assertions.assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        Assertions.assertTrue(fixture.screen.mouseClicked(210.0, 30.0, 0))
        val positions = fixture.widgets.map { IntPoint(it.x, it.y) }

        repeat(2) {
            Assertions.assertTrue(root.nextFocusPath(FocusNavigationEvent.TabNavigation(false)) != null)
            Assertions.assertEquals(fixture.widgets[0], root.focused)
            Assertions.assertFalse(root.isFocused)
            Assertions.assertTrue(fixture.widgets.none { it.isFocused })
            Assertions.assertTrue(outside.isFocused)
            Assertions.assertEquals(positions, fixture.widgets.map { IntPoint(it.x, it.y) })
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
        Assertions.assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        Assertions.assertTrue(fixture.screen.mouseClicked(210.0, 30.0, 0))
        fixture.widgets.forEach {
            it.active = false
        }

        Assertions.assertTrue(fixture.focusNext(false))

        Assertions.assertTrue(root.isFocused)
        Assertions.assertNull(root.focused)
        Assertions.assertTrue(fixture.widgets.none { it.isFocused })
        Assertions.assertEquals(root, root.currentFocusPath?.component())
        Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
        Assertions.assertEquals(-180, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `矢印の未処理入力は子へのフォーカス移動に使うのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.focusNext(true)
        Assertions.assertFalse(root.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
        val path = fixture.screen.nextFocusPath(FocusNavigationEvent.ArrowNavigation(ScreenDirection.DOWN))!!
        fixture.screen.currentFocusPath?.applyFocus(false)
        path.applyFocus(true)
        Assertions.assertTrue(fixture.widgets[1].isFocused)
        Assertions.assertEquals(20, fixture.widgets[1].y)
        fixture.widgets[1].consumeKeys = true
        Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
        Assertions.assertTrue(root.keyReleased(GLFW.GLFW_KEY_DOWN, 0, 0))
        Assertions.assertTrue(root.charTyped('a', 0))
        Assertions.assertEquals("a", fixture.widgets[1].characters)
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
            Assertions.assertTrue(fixture.focusNext(true))
        }
        Assertions.assertTrue(fixture.widgets[2].isFocused)
        Assertions.assertEquals(20, fixture.widgets[2].y)
        Assertions.assertTrue(fixture.widgets[2].y + fixture.widgets[2].height <= 120)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `クリック中に削除した子へ通知せず対応する解放を消費するのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.widgets[0].onClick = { fixture.removers[0].remove() }
        Assertions.assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))
        Assertions.assertNull(root.focused)
        Assertions.assertTrue(root.hasMouseCapture(0))
        Assertions.assertTrue(root.mouseReleased(50.0, 50.0, 0))
        Assertions.assertFalse(root.hasMouseCapture(0))
        Assertions.assertTrue(fixture.widgets[0].releases.isEmpty())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `描画専用の登録が残っても入力の登録を解除した子へ通知しないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        val renderRemover = fixture.widgetContexts[0].addRenderableOnly(fixture.widgets[0])
        Assertions.assertTrue(fixture.screen.mouseClicked(50.0, 50.0, 0))

        fixture.removers[0].remove()

        Assertions.assertEquals(fixture.widgets.drop(1), root.children())
        Assertions.assertNull(root.focused)
        Assertions.assertFalse(root.charTyped('a', 0))
        Assertions.assertTrue(root.mouseReleased(50.0, 50.0, 0))
        Assertions.assertTrue(fixture.widgets[0].releases.isEmpty())
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
            Assertions.assertEquals(fixture.widgets + widget, root.children())
            Assertions.assertTrue(root.mouseClicked(50.0, 50.0, 0))

            if (removePressedRegistration) {
                fixture.removers[0].remove()
            } else {
                duplicateRemover.remove()
            }

            Assertions.assertEquals(if (removePressedRegistration) fixture.widgets.drop(1) + widget else fixture.widgets, root.children())
            Assertions.assertEquals(widget, root.focused)
            Assertions.assertTrue(root.hasMouseCapture(0))
            root.mouseMoved(-100.0, -100.0)
            Assertions.assertTrue(root.mouseDragged(-100.0, -100.0, 0, 1.0, 1.0))
            Assertions.assertTrue(root.mouseReleased(-100.0, -100.0, 0))
            Assertions.assertFalse(root.hasMouseCapture(0))
            Assertions.assertEquals(if (removePressedRegistration) emptyList<Int>() else listOf(0), widget.drags)
            Assertions.assertEquals(if (removePressedRegistration) emptyList<Int>() else listOf(0), widget.releases)
            Assertions.assertEquals(if (removePressedRegistration) emptyList<Pair<Double, Double>>() else listOf(Pair(-100.0, -100.0)), widget.moves)

            if (removePressedRegistration) {
                duplicateRemover.remove()
            } else {
                fixture.removers[0].remove()
            }

            Assertions.assertEquals(fixture.widgets.drop(1), root.children())
            Assertions.assertNull(root.focused)
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
        Assertions.assertTrue(fixture.widgets[2].y >= root.y + root.height)
        Assertions.assertEquals(NarratableEntry.NarrationPriority.HOVERED, root.narrationPriority())

        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        val text = narration.collectNarrationText(true)

        Assertions.assertTrue(text.contains("child-2"))
        Assertions.assertFalse(text.contains("child-0"))
        fixture.widgets[2].active = false
        Assertions.assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `子にフォーカスしていない領域自身を読み上げるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
        root.isFocused = true

        Assertions.assertTrue(root.isFocused)
        Assertions.assertEquals(NarratableEntry.NarrationPriority.FOCUSED, root.narrationPriority())
        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        Assertions.assertEquals(text { SCROLL_AREA_TRANSLATION() }.string, narration.collectNarrationText(true))
        root.isFocused = false
        Assertions.assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
        root.isFocused = true
        root.active = false
        Assertions.assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `描画専用の子だけでもキーボードでフォーカスした領域自身を読み上げるのだ`() {
        listOf(false, true).forEach { horizontal ->
            val fixture = ScrollFixture()
            val view = if (horizontal) XScrollView() else YScrollView()
            view += ScrollTestView(false)
            val controller = fixture.attach(view, IntPoint(50, 50))
            val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
            Assertions.assertTrue(root.children().isEmpty())
            Assertions.assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
            Assertions.assertTrue(fixture.focusNext(true))
            Assertions.assertTrue(root.isFocused)
            Assertions.assertNull(root.focused)
            Assertions.assertEquals(NarratableEntry.NarrationPriority.FOCUSED, root.narrationPriority())

            val narration = ScreenNarrationCollector()
            narration.update { root.updateNarration(it) }
            Assertions.assertEquals(text { SCROLL_AREA_TRANSLATION() }.string, narration.collectNarrationText(true))
            root.currentFocusPath!!.applyFocus(false)
            Assertions.assertEquals(NarratableEntry.NarrationPriority.NONE, root.narrationPriority())
            controller.dispose()
            controller.detach()
        }
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

        Assertions.assertTrue(text.contains("child-2"))
        Assertions.assertFalse(text.contains("child-0"))
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
        Assertions.assertTrue(narration.collectNarrationText(true).contains("child-0"))
        val duplicateRemover = fixture.widgetContexts[0].addRenderableWidget(widget)
        fixture.widgets[1].priority = NarratableEntry.NarrationPriority.HOVERED

        fixture.removers[0].remove()
        narration.update { root.updateNarration(it) }
        val retainedText = narration.collectNarrationText(true)
        Assertions.assertTrue(retainedText.contains("child-1"))
        Assertions.assertFalse(retainedText.contains("child-0"))

        duplicateRemover.remove()
        val newRemover = fixture.widgetContexts[0].addRenderableWidget(widget)
        narration.update { root.updateNarration(it) }
        val newText = narration.collectNarrationText(true)
        Assertions.assertTrue(newText.contains("child-0"))
        Assertions.assertFalse(newText.contains("child-1"))
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
        Assertions.assertEquals(NarratableEntry.NarrationPriority.FOCUSED, root.narrationPriority())
        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        val text = narration.collectNarrationText(true)
        Assertions.assertTrue(text.contains("child-0"))
        Assertions.assertFalse(text.contains("child-2"))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `ホイールで表示範囲外へ移動したフォーカス先も読み上げるのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        fixture.focusNext(true)
        Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1000.0))
        Assertions.assertTrue(fixture.widgets[0].isFocused)
        Assertions.assertEquals(NarratableEntry.NarrationPriority.FOCUSED, root.narrationPriority())

        val narration = ScreenNarrationCollector()
        narration.update { root.updateNarration(it) }
        val text = narration.collectNarrationText(true)

        Assertions.assertTrue(text.contains("child-0"))
        Assertions.assertFalse(text.contains("child-2"))
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
        Assertions.assertTrue(root.children().isEmpty())
        Assertions.assertFalse(root.mouseClicked(50.0, 50.0, 0))
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
        Assertions.assertEquals(IntPoint(30, 25), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
        Assertions.assertTrue(root.mouseClicked(50.0, 50.0, 0))
        Assertions.assertTrue(root.hasMouseCapture(0))
        controller.dispose()
        Assertions.assertEquals(3, fixture.disposals)
        Assertions.assertFalse(root.isActive)
        Assertions.assertFalse(root.hasMouseCapture(0))
        Assertions.assertFalse(root.mouseDragged(50.0, 50.0, 0, 1.0, 1.0))
        Assertions.assertFalse(root.mouseReleased(50.0, 50.0, 0))
        Assertions.assertTrue(fixture.widgets[0].drags.isEmpty())
        Assertions.assertTrue(fixture.widgets[0].releases.isEmpty())
        Assertions.assertFalse(root.mouseClicked(50.0, 50.0, 0))
        controller.detach()
        Assertions.assertTrue(root.children().isEmpty())
        Assertions.assertTrue(fixture.screenWidgets.isEmpty())
    }

    @Test
    fun `入れ子でも外側の表示領域外へクリックとホイールを渡さないのだ`() {
        val fixture = ScrollFixture()
        val outer = YScrollView()
        outer += MinContentSizeYView(200).also { it += createScrollView(false) }
        val controller = fixture.attach(outer, IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertFalse(root.mouseClicked(50.0, 150.0, 0))
        Assertions.assertFalse(root.mouseScrolled(50.0, 150.0, 0.0, -1.0))
        Assertions.assertTrue(root.getChildAt(50.0, 150.0).isEmpty)
        Assertions.assertTrue(fixture.widgets.all { it.clicks.isEmpty() && it.scrolls == 0 })
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
        Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, -1.0, 0.0))
        Assertions.assertEquals(IntPoint(5, 20), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
        Assertions.assertTrue(root.mouseScrolled(50.0, 50.0, 0.0, -1.0))
        Assertions.assertEquals(IntPoint(5, 5), IntPoint(fixture.widgets[0].x, fixture.widgets[0].y))
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `子が消費したキーではスクロールせず未処理のページ移動キーだけを使うのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(100, 100))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertTrue(fixture.focusNext(true))
        Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_PAGE_DOWN, 0, 0))
        Assertions.assertEquals(-60, fixture.widgets[0].y)

        fixture.widgets[0].consumeKeys = true
        Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
        Assertions.assertEquals(-60, fixture.widgets[0].y)

        fixture.widgets[0].consumeKeys = false
        Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
        Assertions.assertEquals(20, fixture.widgets[0].y)
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
        Assertions.assertTrue(fixture.focusNext(true))
        Assertions.assertTrue(root.isFocused)
        Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
        Assertions.assertEquals(-180, fixture.widgets[0].y)
        Assertions.assertTrue(root.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
        Assertions.assertEquals(20, fixture.widgets[0].y)
        controller.dispose()
        controller.detach()
    }

    @Test
    fun `表示領域がゼロでもバーの除算やクリックを行わないのだ`() {
        val fixture = ScrollFixture()
        val controller = fixture.attach(createScrollView(false), IntPoint(0, 0))
        val root = fixture.screenWidgets.single() as VanillaScrollViewWidget
        Assertions.assertFalse(root.mouseClicked(10.0, 20.0, 0))
        Assertions.assertFalse(root.mouseScrolled(10.0, 20.0, 0.0, -1.0))
        controller.dispose()
        controller.detach()
    }

}
