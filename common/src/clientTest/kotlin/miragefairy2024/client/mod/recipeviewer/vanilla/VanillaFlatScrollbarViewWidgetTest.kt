package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.XFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.util.register
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.lwjgl.glfw.GLFW

class VanillaFlatScrollbarViewWidgetTest {
    @Test
    fun `縦横のフラットバーはつまみが透明でもフォーカス枠を描きスクロール範囲がなくなると枠を消すのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 7) else IntRectangle(10, 20, 7, 100)
                val (view, widget) = if (horizontal) {
                    val view = XFlatScrollbarView()
                    Pair(view, VanillaXFlatScrollbarViewWidget(view, bounds))
                } else {
                    val view = YFlatScrollbarView()
                    Pair(view, VanillaYFlatScrollbarViewWidget(view, bounds))
                }
                try {
                    view.contentLength = 300
                    view.viewportLength = 100
                    val path = checkNotNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
                    path.applyFocus(true)
                    widget.render(environment.graphics, 0, 0, 0F)
                    Assertions.assertEquals(listOf(GuiTestEnvironment.Outline(bounds, 0xFFFFFFFF.toInt())), environment.outlines)

                    environment.outlines.clear()
                    view.contentLength = view.viewportLength
                    widget.render(environment.graphics, 0, 0, 0F)
                    Assertions.assertTrue(environment.outlines.isEmpty())
                } finally {
                    widget.dispose()
                }
            }
        }
    }

    @Test
    fun `View の指定位置を直接読み操作結果と再配置後の位置を保持するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                val scrollOffsetValue = view.scrollOffset
                view.contentLength = 300
                view.viewportLength = 100
                val bounds = if (horizontal) IntRectangle(10, 200, 100, 5) else IntRectangle(200, 20, 5, 100)

                fun attach() = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)

                scrollOffsetValue.value = 50.25
                val firstWidget = attach()
                try {
                    Assertions.assertTrue(scrollOffsetValue.toList().isEmpty())
                    firstWidget.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(if (horizontal) IntRectangle(27, 200, 33, 5) else IntRectangle(200, 37, 5, 33), environment.fills.last())
                    scrollOffsetValue.value = 150.75
                    firstWidget.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(if (horizontal) IntRectangle(61, 200, 33, 5) else IntRectangle(200, 71, 5, 33), environment.fills.last())
                    Assertions.assertTrue(firstWidget.mouseScrolled(if (horizontal) 50.0 else 202.0, if (horizontal) 202.0 else 50.0, 0.0, -0.25))
                    Assertions.assertEquals(154.5, scrollOffsetValue.value)
                    firstWidget.isFocused = true
                    Assertions.assertTrue(firstWidget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                    Assertions.assertEquals(0.0, scrollOffsetValue.value)
                    Assertions.assertTrue(firstWidget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                    Assertions.assertEquals(200.0, scrollOffsetValue.value)
                } finally {
                    firstWidget.dispose()
                }
                Assertions.assertTrue(scrollOffsetValue.toList().isEmpty())

                view.contentLength = 150
                val secondWidget = attach()
                try {
                    Assertions.assertEquals(200.0, scrollOffsetValue.value)
                    Assertions.assertTrue(scrollOffsetValue.toList().isEmpty())
                    secondWidget.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                    scrollOffsetValue.value = -25.75
                    secondWidget.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(-25.75, scrollOffsetValue.value)
                    Assertions.assertEquals(if (horizontal) IntRectangle(10, 200, 57, 5) else IntRectangle(200, 20, 5, 57), environment.fills.last())
                } finally {
                    secondWidget.dispose()
                }
                Assertions.assertEquals(-25.75, scrollOffsetValue.value)
                Assertions.assertTrue(scrollOffsetValue.toList().isEmpty())
            }
        }
    }

    @Test
    fun `設定した太さと異なる実寸法でもその矩形で描画とクリックを扱うのだ`() {
        listOf(false, true).forEach { horizontal ->
            listOf(0, 5, 200).forEach { thickness ->
                GuiTestEnvironment().use {
                    val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                    view.scrollbarThickness = thickness
                    view.scrollbarColor = 0xA0FFFFFF.toInt()
                    view.contentLength = 300
                    view.viewportLength = 100
                    val bounds = if (horizontal) IntRectangle(10, 20, 100, 7) else IntRectangle(10, 20, 7, 100)
                    val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                    val graphics: GuiGraphicsExtractor = Mockito.mock(GuiGraphicsExtractor::class.java)
                    val mouseX = if (horizontal) 20.0 else 12.0
                    val mouseY = if (horizontal) 22.0 else 30.0
                    Mockito.`when`(graphics.containsPointInScissor(mouseX.toInt(), mouseY.toInt())).thenReturn(true)
                    widget.render(graphics, mouseX.toInt(), mouseY.toInt(), 0.0F)
                    if (horizontal) {
                        Mockito.verify(graphics).fill(10, 20, 43, 27, view.scrollbarColor)
                    } else {
                        Mockito.verify(graphics).fill(10, 20, 17, 53, view.scrollbarColor)
                    }
                    view.keyboardFocusable = false
                    Assertions.assertFalse(widget.mouseClicked(if (horizontal) 20.0 else 17.0, if (horizontal) 27.0 else 30.0, 0))
                    Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                    Assertions.assertTrue(widget.hasMouseCapture(0))
                    Assertions.assertTrue(widget.mouseReleased(mouseX, mouseY, 0))
                    Assertions.assertNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
                    view.keyboardFocusable = true
                    val path = widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true))
                    Assertions.assertNotNull(path)
                    path!!.applyFocus(true)
                    Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                    Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                    Assertions.assertTrue(widget.mouseScrolled(mouseX, mouseY, 0.0, -1.0))
                    widget.render(graphics, mouseX.toInt(), mouseY.toInt(), 0.0F)
                    if (horizontal) {
                        Mockito.verify(graphics).fill(15, 20, 48, 27, view.scrollbarColor)
                    } else {
                        Mockito.verify(graphics).fill(10, 25, 17, 58, view.scrollbarColor)
                    }
                    widget.dispose()
                }
            }
        }
    }

    @Test
    fun `縦横のバーはホバーとドラッグで表示されその後フェードアウトするのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use {
                Mockito.mockStatic(Util::class.java, Mockito.CALLS_REAL_METHODS).use { clock ->
                    var now = 1000L
                    clock.`when`<Long> { Util.getMillis() }.thenAnswer { now }
                    val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                    view.scrollbarColor = 0xA0FFFFFF.toInt()
                    view.contentLength = 300
                    view.viewportLength = 100
                    val bounds = if (horizontal) IntRectangle(10, 20, 100, 7) else IntRectangle(10, 20, 7, 100)
                    val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                    val graphics: GuiGraphicsExtractor = Mockito.mock(GuiGraphicsExtractor::class.java)
                    val inside = if (horizontal) IntPoint(20, 22) else IntPoint(12, 30)
                    val clipped = if (horizontal) IntPoint(90, 22) else IntPoint(12, 100)
                    val outside = IntPoint.ZERO
                    Mockito.`when`(graphics.containsPointInScissor(inside.x, inside.y)).thenReturn(true)
                    Mockito.`when`(graphics.containsPointInScissor(clipped.x, clipped.y)).thenReturn(false)

                    fun assertColor(at: Long, mouse: IntPoint, color: Int) {
                        now = at
                        Mockito.clearInvocations(graphics)
                        widget.render(graphics, mouse.x, mouse.y, 0.0F)
                        if (horizontal) {
                            Mockito.verify(graphics).fill(10, 20, 43, 27, color)
                        } else {
                            Mockito.verify(graphics).fill(10, 20, 17, 53, color)
                        }
                    }

                    try {
                        assertColor(1000L, outside, 0x00FFFFFF)
                        assertColor(1000L, clipped, 0x00FFFFFF)
                        assertColor(1000L, inside, view.scrollbarColor)
                        assertColor(3000L, inside, view.scrollbarColor)
                        assertColor(3750L, clipped, view.scrollbarColor)
                        assertColor(3938L, clipped, 0x88FFFFFF.toInt())
                        assertColor(4125L, clipped, 0x50FFFFFF)
                        assertColor(4500L, clipped, 0x00FFFFFF)

                        Assertions.assertTrue(widget.mouseClicked(inside.x.toDouble(), inside.y.toDouble(), 0))
                        assertColor(6500L, outside, view.scrollbarColor)
                        assertColor(8500L, outside, view.scrollbarColor)
                        Assertions.assertTrue(widget.mouseReleased(outside.x.toDouble(), outside.y.toDouble(), 0))
                        assertColor(9250L, outside, view.scrollbarColor)
                        assertColor(9625L, outside, 0x50FFFFFF)
                        assertColor(10000L, outside, 0x00FFFFFF)
                    } finally {
                        widget.dispose()
                    }
                }
            }
        }
    }

    @Test
    fun `縦横のバーは自身が受けたホイールとキー操作で再表示されるのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use {
                Mockito.mockStatic(Util::class.java, Mockito.CALLS_REAL_METHODS).use { clock ->
                    var now = 1000L
                    clock.`when`<Long> { Util.getMillis() }.thenAnswer { now }
                    val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                    view.scrollbarColor = 0xA0FFFFFF.toInt()
                    view.contentLength = 300
                    view.viewportLength = 100
                    view.scrollOffset.value = 200.0
                    val bounds = if (horizontal) IntRectangle(10, 20, 100, 7) else IntRectangle(10, 20, 7, 100)
                    val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                    val graphics: GuiGraphicsExtractor = Mockito.mock(GuiGraphicsExtractor::class.java)

                    fun assertColor(at: Long, color: Int) {
                        now = at
                        Mockito.clearInvocations(graphics)
                        widget.render(graphics, 0, 0, 0.0F)
                        if (horizontal) {
                            Mockito.verify(graphics).fill(77, 20, 110, 27, color)
                        } else {
                            Mockito.verify(graphics).fill(10, 87, 17, 120, color)
                        }
                    }

                    try {
                        assertColor(1000L, 0x00FFFFFF)
                        widget.isFocused = true
                        Assertions.assertFalse(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                        assertColor(1500L, view.scrollbarColor)
                        assertColor(1875L, 0x50FFFFFF)
                        assertColor(2250L, 0x00FFFFFF)
                        Mockito.verify(graphics).renderOutline(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, 0xFFFFFFFF.toInt())

                        Assertions.assertFalse(widget.mouseScrolled(12.0, 22.0, 0.0, -1.0))
                        assertColor(2750L, view.scrollbarColor)
                        assertColor(3500L, 0x00FFFFFF)
                    } finally {
                        widget.dispose()
                    }
                }
            }
        }
    }

    @Test
    fun `実際の太さが 0 なら描画とクリックをせずキー操作は継続するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                view.contentLength = 300
                view.viewportLength = 100
                val bounds = if (horizontal) IntRectangle(10, 20, 100, 0) else IntRectangle(10, 20, 0, 100)
                val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                widget.render(environment.graphics, 10, 20, 0.0F)
                Assertions.assertTrue(environment.fills.isEmpty())
                Assertions.assertFalse(widget.mouseClicked(10.0, 20.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                widget.isFocused = true
                widget.render(environment.graphics, 10, 20, 0F)
                Assertions.assertTrue(environment.outlines.isEmpty())
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                widget.dispose()
            }
        }
    }

    @Test
    fun `コンテンツ長が縮小しても現在位置を含む範囲で描画と操作を行うのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                view.contentLength = 300
                view.viewportLength = 100
                val bounds = if (horizontal) IntRectangle(10, 200, 100, 5) else IntRectangle(200, 20, 5, 100)
                val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                view.contentLength = 150
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                view.contentLength = 100
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                Assertions.assertFalse(widget.keyPressed(GLFW.GLFW_KEY_PAGE_DOWN, 0, 0))
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_PAGE_UP, 0, 0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(65, 200, 45, 5) else IntRectangle(200, 75, 5, 45), environment.fills.last())
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                environment.fills.clear()
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertTrue(environment.fills.isEmpty())
                view.contentLength = 300
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(listOf(if (horizontal) IntRectangle(10, 200, 33, 5) else IntRectangle(200, 20, 5, 33)), environment.fills)
                widget.dispose()
            }
        }
    }

    @Test
    fun `範囲外からドラッグして切り返してもつまみが逆方向に動かないのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                view.contentLength = 200
                view.viewportLength = 100
                val bounds = if (horizontal) IntRectangle(10, 200, 100, 5) else IntRectangle(200, 20, 5, 100)
                val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                view.contentLength = 150

                val mouseX = if (horizontal) 85.0 else 202.0
                val mouseY = if (horizontal) 202.0 else 95.0
                val deltaX = if (horizontal) 1.0 else 0.0
                val deltaY = if (horizontal) 0.0 else 1.0
                Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                Assertions.assertTrue(widget.mouseDragged(mouseX - 10 * deltaX, mouseY - 10 * deltaY, 0, -10 * deltaX, -10 * deltaY))
                widget.render(environment.graphics, 50, 50, 0.0F)
                val expectedThumb = if (horizontal) IntRectangle(54, 200, 56, 5) else IntRectangle(200, 64, 5, 56)
                Assertions.assertEquals(expectedThumb, environment.fills.last())

                Assertions.assertTrue(widget.mouseDragged(mouseX - 9 * deltaX, mouseY - 9 * deltaY, 0, deltaX, deltaY))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(expectedThumb, environment.fills.last())
                widget.dispose()
            }
        }
    }

    @Test
    fun `表示領域と異なる長さのバーでページ移動と両端へのドラッグを扱うのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                view.contentLength = 300
                view.viewportLength = 100
                val bounds = if (horizontal) IntRectangle(10, 200, 200, 5) else IntRectangle(200, 20, 5, 200)
                val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(listOf(if (horizontal) IntRectangle(10, 200, 67, 5) else IntRectangle(200, 20, 5, 67)), environment.fills)
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_PAGE_DOWN, 0, 0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(63, 200, 67, 5) else IntRectangle(200, 73, 5, 67), environment.fills.last())
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_PAGE_UP, 0, 0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(10, 200, 67, 5) else IntRectangle(200, 20, 5, 67), environment.fills.last())
                val mouseX = if (horizontal) 80.0 else 202.0
                val mouseY = if (horizontal) 202.0 else 90.0
                Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                Assertions.assertTrue(widget.hasMouseCapture(0))
                Assertions.assertTrue(widget.mouseDragged(500.0, 500.0, 0, 500.0 - mouseX, 500.0 - mouseY))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(143, 200, 67, 5) else IntRectangle(200, 153, 5, 67), environment.fills.last())
                Assertions.assertTrue(widget.mouseDragged(-100.0, -100.0, 0, -600.0, -600.0))
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(10, 200, 67, 5) else IntRectangle(200, 20, 5, 67), environment.fills.last())
                Assertions.assertTrue(widget.mouseReleased(-100.0, -100.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                widget.dispose()
            }
        }
    }

    @Test
    fun `ドラッグ中に実際の太さを 0 にしても移動を継続しリリースでキャプチャを解除するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XFlatScrollbarView() else YFlatScrollbarView()
                view.contentLength = 300
                view.viewportLength = 100
                val bounds = if (horizontal) IntRectangle(10, 200, 100, 5) else IntRectangle(200, 20, 5, 100)
                val widget = if (horizontal) VanillaXFlatScrollbarViewWidget(view as XFlatScrollbarView, bounds) else VanillaYFlatScrollbarViewWidget(view as YFlatScrollbarView, bounds)
                val mouseX = if (horizontal) 15.0 else 202.0
                val mouseY = if (horizontal) 202.0 else 25.0
                Assertions.assertTrue(widget.mouseClicked(mouseX, mouseY, 0))
                widget.setSize(if (horizontal) 100 else 0, if (horizontal) 0 else 100)
                Assertions.assertTrue(widget.mouseDragged(500.0, 500.0, 0, 500.0 - mouseX, 500.0 - mouseY))
                Assertions.assertTrue(widget.hasMouseCapture(0))
                widget.setSize(if (horizontal) 100 else 5, if (horizontal) 5 else 100)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                widget.setSize(100, 100)
                Assertions.assertTrue(widget.mouseDragged(-100.0, -100.0, 0, -600.0, -600.0))
                widget.setSize(if (horizontal) 100 else 5, if (horizontal) 5 else 100)
                widget.render(environment.graphics, 50, 50, 0.0F)
                Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                widget.setSize(if (horizontal) 100 else 0, if (horizontal) 0 else 100)
                Assertions.assertTrue(widget.mouseReleased(500.0, 500.0, 0))
                Assertions.assertFalse(widget.hasMouseCapture(0))
                Assertions.assertFalse(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
                widget.dispose()
            }
        }
    }

    @Test
    fun `トラックのクリックとホイールとキーを扱い破棄時にキャプチャを解除するのだ`() {
        GuiTestEnvironment().use { environment ->
            val view = YFlatScrollbarView()
            view.contentLength = 300
            view.viewportLength = 100
            val widget = VanillaYFlatScrollbarViewWidget(view, IntRectangle(200, 20, 5, 100))
            val scrollInputs = mutableListOf<Double>()
            val inputRemover = view.onScrollInput.register {
                scrollInputs += view.scrollOffset.value
            }
            Assertions.assertFalse(widget.mouseClicked(50.0, 50.0, 0))
            Assertions.assertFalse(widget.mouseClicked(202.0, 25.0, 1))
            Assertions.assertTrue(widget.mouseClicked(202.0, 110.0, 0))
            Assertions.assertEquals(200.0, view.scrollOffset.value)
            widget.render(environment.graphics, 202, 25, 0.0F)
            Assertions.assertEquals(IntRectangle(200, 87, 5, 33), environment.fills.last())
            Assertions.assertTrue(widget.mouseReleased(202.0, 110.0, 0))
            Assertions.assertFalse(widget.mouseScrolled(202.0, 25.0, 0.0, -1.0))
            widget.isFocused = true
            Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
            Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
            Assertions.assertEquals(15.0, view.scrollOffset.value)
            widget.render(environment.graphics, 202, 25, 0.0F)
            Assertions.assertEquals(IntRectangle(200, 25, 5, 33), environment.fills.last())
            Assertions.assertTrue(widget.mouseScrolled(202.0, 25.0, 0.0, -1.0))
            Assertions.assertEquals(30.0, view.scrollOffset.value)
            widget.render(environment.graphics, 202, 25, 0.0F)
            Assertions.assertEquals(IntRectangle(200, 30, 5, 33), environment.fills.last())
            Assertions.assertTrue(widget.mouseClicked(202.0, 40.0, 0))
            inputRemover.remove()
            widget.dispose()
            Assertions.assertEquals(listOf(200.0, 0.0, 15.0, 30.0), scrollInputs)
            Assertions.assertTrue(view.onScrollInput.toList().isEmpty())
            Assertions.assertFalse(widget.hasMouseCapture(0))
            Assertions.assertFalse(widget.mouseDragged(500.0, 500.0, 0, 0.0, 0.0))
            Assertions.assertFalse(widget.mouseReleased(500.0, 500.0, 0))
            Assertions.assertFalse(widget.mouseClicked(202.0, 40.0, 0))
            Assertions.assertNull(widget.nextFocusPath(FocusNavigationEvent.TabNavigation(true)))
        }
    }


}
