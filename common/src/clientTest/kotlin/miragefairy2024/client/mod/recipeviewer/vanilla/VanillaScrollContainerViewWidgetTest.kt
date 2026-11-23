package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedView
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.empty
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW
import org.mockito.Mockito

private class ScrollContainerTestView(size: IntPoint) : FixedWidgetView(size)

class VanillaScrollContainerViewWidgetTest {
    @Test
    fun `View の指定位置と操作結果を共有し購読を解除してから再配置するのだ`() {
        listOf(false, true).forEach { horizontal ->
            val view = if (horizontal) XScrollContainerView() else YScrollContainerView()
            val scrollOffsetValue = if (horizontal) (view as XScrollContainerView).scrollOffsetX else (view as YScrollContainerView).scrollOffsetY
            val placements = mutableListOf<MutableList<IntPoint>>()
            view += object : FixedView(IntPoint(300, 300)) {
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
                    val offsets = mutableListOf(offset)
                    placements += offsets
                    return object : ViewController {
                        override fun setOffset(offset: IntPoint) {
                            offsets += offset
                        }
                        override fun detach() = Unit
                        override fun dispose() = Unit
                    }
                }
            }
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()

            fun attach(viewportLength: Int): VanillaScrollContainerViewWidget {
                view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { viewportLength }, { viewportLength })
                val bounds = IntRectangle(10, 20, viewportLength, viewportLength)
                return if (horizontal) VanillaXScrollContainerViewWidget(view as XScrollContainerView, bounds, context, registry) else VanillaYScrollContainerViewWidget(view as YScrollContainerView, bounds, context, registry)
            }

            scrollOffsetValue.value = 175.25
            val firstWidget = attach(100)
            var scrollInputs = 0
            val scrollInputRemover = view.onScrollInput.register {
                scrollInputs++
            }
            try {
                Assertions.assertEquals(if (horizontal) IntPoint(-165, 20) else IntPoint(10, -155), placements[0].last())
                Assertions.assertEquals(1, scrollOffsetValue.toList().size)
                scrollOffsetValue.value = -30.25
                Assertions.assertEquals(-30.25, scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(40, 20) else IntPoint(10, 50), placements[0].last())
                scrollOffsetValue.value = 230.75
                Assertions.assertEquals(230.75, scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-221, 20) else IntPoint(10, -211), placements[0].last())
                Assertions.assertTrue(firstWidget.mouseScrolled(50.0, 50.0, 0.0, 1.0))
                Assertions.assertEquals(215.75, scrollOffsetValue.value)
                Assertions.assertEquals(1, scrollInputs)
                firstWidget.isFocused = true
                Assertions.assertTrue(firstWidget.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0))
                Assertions.assertEquals(0.0, scrollOffsetValue.value)
                Assertions.assertEquals(2, scrollInputs)
                Assertions.assertTrue(firstWidget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                Assertions.assertEquals(200.0, scrollOffsetValue.value)
                Assertions.assertEquals(3, scrollInputs)
                Assertions.assertFalse(firstWidget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                Assertions.assertEquals(4, scrollInputs)
                Assertions.assertFalse(firstWidget.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                Assertions.assertEquals(5, scrollInputs)
                Assertions.assertFalse(firstWidget.mouseScrolled(50.0, 50.0, 0.0, 0.0))
                Assertions.assertEquals(5, scrollInputs)
            } finally {
                scrollInputRemover.remove()
                firstWidget.dispose()
                firstWidget.detach()
            }
            Assertions.assertTrue(scrollOffsetValue.toList().isEmpty())
            val previousOffsets = placements[0].toList()

            val secondWidget = attach(200)
            try {
                Assertions.assertEquals(200.0, scrollOffsetValue.value)
                Assertions.assertEquals(if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180), placements[1].last())
                Assertions.assertEquals(1, scrollOffsetValue.toList().size)
                scrollOffsetValue.value = 25.75
                Assertions.assertEquals(if (horizontal) IntPoint(-16, 20) else IntPoint(10, -6), placements[1].last())
                Assertions.assertEquals(previousOffsets, placements[0])
            } finally {
                secondWidget.dispose()
                secondWidget.detach()
            }
            Assertions.assertEquals(25.75, scrollOffsetValue.value)
            Assertions.assertTrue(scrollOffsetValue.toList().isEmpty())
        }
    }

    @Test
    fun `バーを削除した領域でも子のクリックとスクロールと破棄を中継するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XScrollContainerView() else YScrollContainerView()
                view += ScrollContainerTestView(IntPoint(300, 300))
                view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
                val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
                Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
                val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
                lateinit var child: AbstractWidget
                var disposed = false
                var releasedInput: Triple<Double, Double, Int>? = null
                registry.register { childContext, _: ScrollContainerTestView, bounds ->
                    child = object : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
                        override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
                        override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
                        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) = button == 0 && isMouseOver(mouseX, mouseY)
                        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
                            releasedInput = Triple(mouseX, mouseY, button)
                            return false
                        }
                    }
                    val remover = childContext.addRenderable(WidgetRegistration(child, true, WidgetRenderOverflow.UNBOUNDED))
                    object : ViewController {
                        override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                        override fun detach() = remover.remove()
                        override fun dispose() {
                            disposed = true
                        }
                    }
                }
                val bounds = IntRectangle(10, 20, 100, 100)
                val widget = if (horizontal) VanillaXScrollContainerViewWidget(view as XScrollContainerView, bounds, context, registry) else VanillaYScrollContainerViewWidget(view as YScrollContainerView, bounds, context, registry)
                Assertions.assertTrue(widget.mouseClicked(109.0, 119.0, 0))
                Assertions.assertSame(child, widget.focused)
                Assertions.assertNull(releasedInput)
                Assertions.assertTrue(widget.mouseReleased(109.0, 119.0, 0))
                Assertions.assertEquals(Triple(109.0, 119.0, 0), releasedInput)
                Assertions.assertTrue(widget.mouseScrolled(109.0, 119.0, 0.0, -1.0))
                Assertions.assertEquals(if (horizontal) IntPoint(-5, 20) else IntPoint(10, 5), IntPoint(child.x, child.y))
                widget.render(environment.graphics, 109, 119, 0.0F)
                Assertions.assertTrue(environment.fills.isEmpty())
                widget.dispose()
                Assertions.assertTrue(disposed)
                Assertions.assertFalse(widget.mouseClicked(109.0, 119.0, 0))
            }
        }
    }

    @Test
    fun `描画範囲が表示領域に届くかを辺ごとに判定するのだ`() {
        GuiTestEnvironment().use { environment ->
            val cases = listOf(
                Triple(IntPoint(0, 25), WidgetRenderOverflow.NONE, false),
                Triple(IntPoint(-1, 25), WidgetRenderOverflow(0, 1, 0, 0), false),
                Triple(IntPoint(0, 25), WidgetRenderOverflow(0, 1, 0, 0), true),
                Triple(IntPoint(-100, 25), WidgetRenderOverflow(0, Int.MAX_VALUE, 0, 0), true),
                Triple(IntPoint(100, 25), WidgetRenderOverflow(Int.MAX_VALUE, 0, 0, 0), true),
                Triple(IntPoint(15, -100), WidgetRenderOverflow(0, 0, 0, Int.MAX_VALUE), true),
                Triple(IntPoint(15, 100), WidgetRenderOverflow(0, 0, Int.MAX_VALUE, 0), true),
                Triple(IntPoint(-100, 10), WidgetRenderOverflow(0, Int.MAX_VALUE, 0, 0), false),
                Triple(IntPoint(30, 25), WidgetRenderOverflow(0, Int.MAX_VALUE, 0, 0), false),
                Triple(IntPoint(100, 100), WidgetRenderOverflow.UNBOUNDED, true),
            )
            val view = YScrollContainerView()
            view += YListView().also { listView ->
                cases.forEach {
                    listView += ScrollContainerTestView(IntPoint(10, 10))
                }
            }
            view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 20 }, { 20 })
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            val children = mutableListOf<AbstractWidget>()
            val renderedChildren = mutableListOf<Int>()
            registry.register { childContext, _: ScrollContainerTestView, bounds ->
                val index = children.size
                val child = object : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
                    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
                        renderedChildren += index
                    }

                    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
                }
                children += child
                val remover = childContext.addRenderable(WidgetRegistration(child, true, cases[index].second))
                object : ViewController {
                    override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                    override fun detach() = remover.remove()
                }
            }
            val widget = VanillaYScrollContainerViewWidget(view, IntRectangle(10, 20, 20, 20), context, registry)
            try {
                children.zip(cases).forEach { (child, case) ->
                    child.setPosition(case.first.x, case.first.y)
                }
                widget.render(environment.graphics, 0, 0, 0.0F)
                Assertions.assertEquals(cases.mapIndexedNotNull { index, case -> if (case.third) index else null }, renderedChildren)

                val child = children.first()
                child.setPosition(10, 20)
                widget.render(environment.graphics, 12, 22, 0.0F)
                Assertions.assertTrue(child.isHovered)

                renderedChildren.clear()
                child.setPosition(0, 25)
                widget.render(environment.graphics, 12, 22, 0.0F)
                Assertions.assertTrue(0 in renderedChildren)
                Assertions.assertFalse(child.isHovered)

                renderedChildren.clear()
                widget.render(environment.graphics, 12, 22, 0.0F)
                Assertions.assertFalse(0 in renderedChildren)
            } finally {
                widget.dispose()
                widget.detach()
            }
        }
    }

    @Test
    fun `表示領域が広がったあとは範囲外の位置から少しずつ戻れるのだ`() {
        listOf(false, true).forEach { horizontal ->
            val offsets = mutableListOf<IntPoint>()
            val view = if (horizontal) XScrollContainerView() else YScrollContainerView()
            view += object : FixedView(IntPoint(300, 300)) {
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
                    return object : ViewController {
                        override fun setOffset(offset: IntPoint) {
                            offsets += offset
                        }
                        override fun detach() = Unit
                        override fun dispose() = Unit
                    }
                }
            }
            view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            val bounds = IntRectangle(10, 20, 100, 100)
            val widget = if (horizontal) VanillaXScrollContainerViewWidget(view as XScrollContainerView, bounds, context, registry) else VanillaYScrollContainerViewWidget(view as YScrollContainerView, bounds, context, registry)
            try {
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                widget.setSize(300, 300)
                Assertions.assertEquals(listOf(if (horizontal) IntPoint(-190, 20) else IntPoint(10, -180)), offsets)
                Assertions.assertFalse(widget.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                Assertions.assertEquals(1, offsets.size)
                Assertions.assertTrue(widget.mouseScrolled(50.0, 50.0, 0.0, 1.0))
                Assertions.assertEquals(if (horizontal) IntPoint(-175, 20) else IntPoint(10, -165), offsets.last())
                val backwardKey = if (horizontal) GLFW.GLFW_KEY_LEFT else GLFW.GLFW_KEY_UP
                Assertions.assertTrue(widget.keyPressed(backwardKey, 0, 0))
                Assertions.assertEquals(if (horizontal) IntPoint(-160, 20) else IntPoint(10, -150), offsets.last())
            } finally {
                widget.dispose()
                widget.detach()
            }
        }
    }

    @Test
    fun `範囲外から逆 Tab で子に移ると子が収まる分だけスクロールするのだ`() {
        GuiTestEnvironment().use {
            val view = YScrollContainerView()
            view += YListView().also {
                it += YSpaceView(220)
                it += ScrollContainerTestView(IntPoint(20, 20))
                it += YSpaceView(10)
                it += ScrollContainerTestView(IntPoint(20, 20))
                it += YSpaceView(30)
            }
            view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 70 })
            val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
            Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            val children = mutableListOf<AbstractWidget>()
            registry.register { childContext, _: ScrollContainerTestView, bounds ->
                val child: AbstractWidget = object : AbstractWidget(bounds.x, bounds.y, bounds.sizeX, bounds.sizeY, text { empty() }) {
                    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
                    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
                }
                children += child
                val remover = childContext.addRenderable(WidgetRegistration(child, true, WidgetRenderOverflow.UNBOUNDED))
                object : ViewController {
                    override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                    override fun detach() = remover.remove()
                    override fun dispose() = Unit
                }
            }
            val widget = VanillaYScrollContainerViewWidget(view, IntRectangle(10, 20, 100, 70), context, registry)
            try {
                widget.isFocused = true
                Assertions.assertTrue(widget.keyPressed(GLFW.GLFW_KEY_END, 0, 0))
                widget.setHeight(100)
                Assertions.assertEquals(listOf(10, 40), children.map { it.y })

                fun focusPrevious() {
                    val path = widget.nextFocusPath(FocusNavigationEvent.TabNavigation(false))
                    Assertions.assertNotNull(path)
                    widget.currentFocusPath?.applyFocus(false)
                    path!!.applyFocus(true)
                }

                focusPrevious()
                Assertions.assertTrue(children[1].isFocused)
                Assertions.assertEquals(listOf(10, 40), children.map { it.y })

                focusPrevious()
                Assertions.assertTrue(children[0].isFocused)
                Assertions.assertEquals(listOf(20, 50), children.map { it.y })
            } finally {
                widget.dispose()
                widget.detach()
            }
        }
    }
}
