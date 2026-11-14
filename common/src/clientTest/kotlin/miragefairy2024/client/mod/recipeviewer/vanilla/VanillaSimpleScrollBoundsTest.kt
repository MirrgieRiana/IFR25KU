package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.mod.recipeviewer.views.XSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.YSimpleScrollView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.register
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

private class BoundsTestView : FixedWidgetView(IntPoint(300, 300))

class VanillaSimpleScrollBoundsTest {
    @Test
    fun `バニラの実際の寸法を生成時と変更後の描画時に通知するのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                view += BoundsTestView()
                view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 50 }, { 50 })
                val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
                Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
                val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
                registry.register { _, _: BoundsTestView, _ -> ViewController.NONE }
                var changes = 0
                view.onScrollBoundsChanged.register { changes++ }
                val bounds = IntRectangle(10, 20, 100, 100)
                val widget = if (horizontal) VanillaXSimpleScrollViewWidget(view as XSimpleScrollView, bounds, context, registry) else VanillaYSimpleScrollViewWidget(view as YSimpleScrollView, bounds, context, registry)
                try {
                    Assertions.assertEquals(300, view.contentLength)
                    Assertions.assertEquals(100, view.viewportLength)
                    widget.setSize(if (horizontal) 200 else 100, if (horizontal) 100 else 200)
                    widget.render(environment.graphics, 50, 50, 0.0F)
                    widget.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(200, view.viewportLength)
                    Assertions.assertEquals(2, changes)
                } finally {
                    widget.dispose()
                    widget.detach()
                }
            }
        }
    }

    @Test
    fun `破棄済み Widget の描画では再配置後の寸法通知を上書きしないのだ`() {
        listOf(false, true).forEach { horizontal ->
            GuiTestEnvironment().use { environment ->
                val view = if (horizontal) XSimpleScrollView() else YSimpleScrollView()
                view += BoundsTestView()
                view.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 300 }, { 300 })
                val context: VanillaViewPlacerContext = Mockito.mock(VanillaViewPlacerContext::class.java)
                Mockito.`when`(context.origin).thenReturn(IntPoint.ZERO)
                val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
                registry.register { _, _: BoundsTestView, _ -> ViewController.NONE }
                fun createWidget(length: Int): VanillaSimpleScrollViewWidget {
                    val bounds = if (horizontal) IntRectangle(10, 20, length, 100) else IntRectangle(10, 20, 100, length)
                    return if (horizontal) VanillaXSimpleScrollViewWidget(view as XSimpleScrollView, bounds, context, registry) else VanillaYSimpleScrollViewWidget(view as YSimpleScrollView, bounds, context, registry)
                }
                val old = createWidget(100)
                old.dispose()
                val current = createWidget(200)
                try {
                    Assertions.assertEquals(200, view.viewportLength)
                    old.render(environment.graphics, 50, 50, 0.0F)
                    Assertions.assertEquals(200, view.viewportLength)
                } finally {
                    current.dispose()
                    current.detach()
                    old.detach()
                }
            }
        }
    }
}
