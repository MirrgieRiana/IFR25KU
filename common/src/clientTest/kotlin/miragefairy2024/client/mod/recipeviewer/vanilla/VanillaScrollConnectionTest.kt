package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewPlacerRegistry
import miragefairy2024.mod.recipeviewer.views.FixedView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.mod.recipeviewer.views.connectScrollbar
import miragefairy2024.mod.recipeviewer.views.plusAssign
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class VanillaScrollConnectionTest {
    @Test
    fun `配置済みの両側が範囲外の指定位置を共有し解除後は個別に操作できるのだ`() {
        GuiTestEnvironment().use { environment ->
            val scrollView = YScrollContainerView()
            var childOffset = IntPoint.ZERO
            scrollView += object : FixedView(IntPoint(300, 300)) {
                override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): ViewController {
                    childOffset = offset
                    return object : ViewController {
                        override fun setOffset(offset: IntPoint) {
                            childOffset = offset
                        }
                        override fun detach() = Unit
                        override fun dispose() = Unit
                    }
                }
            }
            scrollView.initSizeCalculation(Mockito.mock(RenderingProxy::class.java), { 100 }, { 100 })
            val context = Mockito.mock(VanillaViewPlacerContext::class.java)
            val registry = ViewPlacerRegistry<VanillaViewPlacerContext>()
            val scroll = VanillaYScrollContainerViewWidget(scrollView, IntRectangle(10, 20, 100, 100), context, registry)
            val barView = YScrollbarView()
            val bar = VanillaYScrollbarViewWidget(barView, IntRectangle(250, 20, 5, 100))
            try {
                Assertions.assertTrue(scroll.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                val connection = connectScrollbar(scrollView, barView)
                try {
                    Assertions.assertEquals(15.0, barView.scrollOffset.value)

                    barView.scrollOffset.value = 120.5
                    scroll.setSize(100, 200)
                    Assertions.assertEquals(IntPoint(10, -101), childOffset)
                    Assertions.assertFalse(scroll.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                    Assertions.assertEquals(120.5, scrollView.scrollOffsetY.value)
                    Assertions.assertTrue(scroll.mouseScrolled(50.0, 50.0, 0.0, 1.0))
                    Assertions.assertEquals(105.5, scrollView.scrollOffsetY.value)
                    Assertions.assertEquals(IntPoint(10, -86), childOffset)
                    bar.render(environment.graphics, 0, 0, 0.0F)
                    Assertions.assertEquals(IntRectangle(250, 55, 5, 65), environment.fills.last())

                    Assertions.assertTrue(bar.mouseClicked(252.0, 60.0, 0))
                    Assertions.assertEquals(105.5, scrollView.scrollOffsetY.value)
                    Assertions.assertTrue(bar.mouseDragged(252.0, 25.0, 0, 0.0, -35.0))
                    Assertions.assertTrue(bar.mouseReleased(252.0, 25.0, 0))
                    Assertions.assertEquals(0.0, scrollView.scrollOffsetY.value)
                    Assertions.assertEquals(IntPoint(10, 20), childOffset)

                    scrollView.scrollOffsetY.value = 55.25
                    connection.remove()
                    Assertions.assertEquals(55.25, scrollView.scrollOffsetY.value)
                    Assertions.assertEquals(55.25, barView.scrollOffset.value)
                    Assertions.assertTrue(scroll.mouseScrolled(50.0, 50.0, 0.0, -1.0))
                    Assertions.assertTrue(bar.mouseScrolled(252.0, 50.0, 0.0, 1.0))
                    Assertions.assertEquals(IntPoint(10, -50), childOffset)
                    bar.render(environment.graphics, 0, 0, 0.0F)
                    Assertions.assertEquals(IntRectangle(250, 33, 5, 67), environment.fills.last())
                    Assertions.assertEquals(70.25, scrollView.scrollOffsetY.value)
                    Assertions.assertEquals(40.25, barView.scrollOffset.value)
                } finally {
                    connection.remove()
                }
            } finally {
                bar.dispose()
                scroll.dispose()
                scroll.detach()
            }
        }
    }
}
