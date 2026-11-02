package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.client.mod.recipeviewer.vanilla.VanillaViewRendererWidget
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private val TEXT_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = 0
    override fun calculateTextWidth(text: FormattedCharSequence) = 0
    override fun getTextHeight() = 9
    override fun wrapText(text: FormattedText, maxWidth: Int) = emptyList<FormattedCharSequence>()
}

class TextViewRendererTest {
    @Test
    fun `単一行と複数行のツールチップはクリップ解除と画面描画の後に描画するのだ`() {
        val tooltip = text { "tooltip"() }
        fun <V : PlaceableView> check(view: V, renderer: ViewRenderer<V>) {
            GuiTestEnvironment().use { environment ->
                view.initSizeCalculation(TEXT_RENDERING_PROXY, { 100 }, { 100 })
                val widget = VanillaViewRendererWidget(renderer, view, IntRectangle(10, 20, 100, 100))
                environment.render = { graphics, mouseX, mouseY, partialTick ->
                    graphics.enableScissor(10, 20, 50, 60)
                    try {
                        widget.render(graphics, mouseX, mouseY, partialTick)
                        Assertions.assertTrue(environment.tooltips.isEmpty())
                    } finally {
                        graphics.disableScissor()
                    }
                    environment.events += "scissor-end"
                }

                environment.screen.renderWithTooltip(environment.graphics, 30, 30, 0.0F)
                Assertions.assertTrue(widget.isHovered)
                Assertions.assertEquals(listOf(listOf(tooltip.visualOrderText)), environment.tooltips)
                Assertions.assertEquals(listOf("scissor-end", "screen-end", "tooltip"), environment.events.takeLast(3))

                environment.events.clear()
                environment.tooltips.clear()
                environment.screen.renderWithTooltip(environment.graphics, 70, 30, 0.0F)
                Assertions.assertFalse(widget.isHovered)
                Assertions.assertTrue(environment.tooltips.isEmpty())
                Assertions.assertFalse("tooltip" in environment.events)
            }
        }
        check(TextView().also { it.tooltip = listOf(tooltip) }, TextViewRenderer)
        check(MultiLineTextView().also { it.tooltip = listOf(tooltip) }, MultiLineTextViewRenderer)
    }
}
