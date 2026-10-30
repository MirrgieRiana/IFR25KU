package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.views.ScrollView
import miragefairy2024.mod.recipeviewer.views.ScrollbarStyle
import miragefairy2024.mod.recipeviewer.views.SpaceView
import miragefairy2024.mod.recipeviewer.views.plusAssign
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScrollViewWidgetTest {

    private fun createWidget(): ScrollViewWidget {
        val renderingProxy = object : RenderingProxy {
            override fun calculateTextWidth(text: FormattedText) = throw UnsupportedOperationException()
            override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
            override fun getTextHeight() = throw UnsupportedOperationException()
            override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = throw UnsupportedOperationException()
        }
        val view = ScrollView(5, ScrollbarStyle.Vanilla)
        view += SpaceView(IntPoint(100, 300))
        view.initSizeCalculation(renderingProxy, { 100 }, { 50 })
        return ScrollViewWidget(view, IntRectangle(10, 20, 100, 50))
    }

    @Test
    fun `子が処理しないクリックはバニラのスロットへ渡せるのだ～🌱`() {
        val widget = createWidget()
        assertFalse(widget.mouseClicked(30.0, 30.0, 0))
        assertFalse(widget.mouseClicked(30.0, 30.0, 1))
        assertFalse(widget.mouseClicked(0.0, 0.0, 0))
        assertTrue(widget.mouseClicked(107.0, 30.0, 0))
    }

    @Test
    fun `スクロールバー上でもホイールの入力対象になるのだ～🌱`() {
        val widget = createWidget()
        assertTrue(widget.isMouseOver(107.0, 30.0))
        assertFalse(widget.isMouseOver(111.0, 30.0))
        val clicks = mutableListOf<Double>()
        widget.addWidget(object : AbstractWidget(10, 20, 100, 300, Component.empty()) {
            override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
            override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                clicks += mouseY
                return true
            }
        })
        widget.mouseScrolled(107.0, 30.0, 0.0, -1.0)
        widget.mouseClicked(30.0, 30.0, 0)
        widget.view.scrollStep = 18.0
        widget.mouseScrolled(107.0, 30.0, 0.0, -1.0)
        widget.mouseClicked(30.0, 30.0, 0)
        assertEquals(listOf(45.0, 63.0), clicks)
    }

    @Test
    fun `子へのクリックはスクロール前の座標へ変換し登録解除後は渡さないのだ～🌱`() {
        val widget = createWidget()
        widget.mouseScrolled(30.0, 30.0, 0.0, -1.0)
        val clicks = mutableListOf<Pair<Double, Double>>()
        val child = object : AbstractWidget(10, 35, 20, 20, Component.empty()) {
            override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit
            override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit
            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                clicks += Pair(mouseX, mouseY)
                return true
            }
        }
        val remover = widget.addWidget(child)
        assertTrue(widget.mouseClicked(12.0, 25.0, 0))
        assertEquals(listOf(Pair(12.0, 40.0)), clicks)
        remover.remove()
        assertFalse(widget.mouseClicked(12.0, 25.0, 0))
        assertEquals(1, clicks.size)
    }

}
