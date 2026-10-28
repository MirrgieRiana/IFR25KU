package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private class ScrollChildView : FixedWidgetView(IntPoint(100, 100))

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

}
