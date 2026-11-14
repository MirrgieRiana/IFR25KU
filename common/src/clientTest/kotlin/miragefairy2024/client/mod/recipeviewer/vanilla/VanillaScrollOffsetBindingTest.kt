package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XScrollbarView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import net.minecraft.client.gui.components.AbstractWidget
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

private class OffsetBindingTestView : FixedWidgetView(IntPoint(300, 300))

class VanillaScrollOffsetBindingTest {
    @Test
    fun `両 View の指定位置を初回配置と変更と再配置へ反映し破棄時に購読を解除するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                val offset = if (horizontal) (view as XSimpleScrollView).scrollOffsetX else (view as YSimpleScrollView).scrollOffsetY
                view += OffsetBindingTestView()
                view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
                val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
                Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
                val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
                lateinit var child: AbstractWidget
                registry.register { _, _: OffsetBindingTestView, bounds ->
                    child = Mockito.mock(AbstractWidget::class.java)
                    child.setPosition(bounds.x, bounds.y)
                    object : ViewController {
                        override fun setOffset(offset: IntPoint) = child.setPosition(offset.x, offset.y)
                        override fun detach() = Unit
                    }
                }
                fun createWidget(): VanillaSimpleScrollViewWidget {
                    val bounds = IntRectangle(10, 20, 100, 100)
                    return if (horizontal) VanillaXSimpleScrollViewWidget(view as XSimpleScrollView, bounds, context, registry) else VanillaYSimpleScrollViewWidget(view as YSimpleScrollView, bounds, context, registry)
                }
                offset.value = 175.25
                val first = createWidget()
                try {
                    Mockito.verify(child).setPosition(if (horizontal) -165 else 10, if (horizontal) 20 else -155)
                    offset.value = 999.75
                    Mockito.verify(child).setPosition(if (horizontal) -190 else 10, if (horizontal) 20 else -180)
                    first.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(999.75, offset.value)
                } finally {
                    first.dispose()
                    first.detach()
                }
                Mockito.clearInvocations(child)
                offset.value = 40.25
                Mockito.verifyNoInteractions(child)
                Assertions.assertTrue(offset.listeners.isEmpty())
                val second = createWidget()
                try {
                    Mockito.verify(child).setPosition(if (horizontal) -30 else 10, if (horizontal) 20 else -20)
                    Assertions.assertEquals(40.25, offset.value)
                } finally {
                    second.dispose()
                    second.detach()
                }
                val bar = if (horizontal) XScrollbarView() else YScrollbarView()
                val barOffset = if (horizontal) (bar as XScrollbarView).scrollOffsetX else (bar as YScrollbarView).scrollOffsetY
                bar.contentLength = 300
                bar.viewportLength = 100
                barOffset.value = 999.75
                val scrollbar = if (horizontal) VanillaXScrollbarViewWidget(bar as XScrollbarView, IntRectangle(10, 200, 100, 5)) else VanillaYScrollbarViewWidget(bar as YScrollbarView, IntRectangle(200, 20, 5, 100))
                try {
                    scrollbar.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(if (horizontal) IntRectangle(77, 200, 33, 5) else IntRectangle(200, 87, 5, 33), environment.fills.last())
                    Assertions.assertEquals(999.75, barOffset.value)
                    Assertions.assertTrue(scrollbar.mouseScrolled(scrollbar.x + 1.0, scrollbar.y + 1.0, 0.0, 1.0))
                    Assertions.assertEquals(185.0, barOffset.value)
                } finally {
                    scrollbar.dispose()
                }
            }
        }
    }
}
