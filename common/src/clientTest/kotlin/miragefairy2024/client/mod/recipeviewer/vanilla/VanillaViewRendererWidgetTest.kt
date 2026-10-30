package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private class RendererTestView : FixedWidgetView(IntPoint(100, 100))

class VanillaViewRendererWidgetTest {

    @Test
    fun `処理したクリックに対応するドラッグと解放だけを消費するのだ`() {
        var consumeClick = false
        val renderer: ViewRenderer<RendererTestView> = object : ViewRenderer<RendererTestView> {
            override fun mouseClicked(view: RendererTestView, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = consumeClick
        }
        val widget = VanillaViewRendererWidget(renderer, RendererTestView(), IntRectangle(10, 20, 100, 100))
        assertFalse(widget.mouseClicked(30.0, 40.0, 0))
        assertFalse(widget.mouseDragged(30.0, 40.0, 0, 1.0, 1.0))
        assertFalse(widget.mouseReleased(30.0, 40.0, 0))

        consumeClick = true
        assertTrue(widget.mouseClicked(30.0, 40.0, 0))
        assertFalse(widget.mouseDragged(30.0, 40.0, 1, 1.0, 1.0))
        assertFalse(widget.mouseReleased(30.0, 40.0, 1))
        assertTrue(widget.mouseDragged(300.0, 400.0, 0, 1.0, 1.0))
        assertTrue(widget.mouseReleased(300.0, 400.0, 0))
        assertFalse(widget.mouseReleased(300.0, 400.0, 0))

        assertTrue(widget.mouseClicked(30.0, 40.0, 0))
        consumeClick = false
        assertFalse(widget.mouseClicked(30.0, 40.0, 0))
        assertFalse(widget.mouseDragged(30.0, 40.0, 0, 1.0, 1.0))
        assertFalse(widget.mouseReleased(30.0, 40.0, 0))
    }

}
