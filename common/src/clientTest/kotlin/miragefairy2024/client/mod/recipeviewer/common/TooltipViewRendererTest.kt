package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.client.mod.recipeviewer.vanilla.VanillaViewRendererWidget
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.TooltipView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private val TOOLTIP_RENDERING_PROXY: RenderingProxy = object : RenderingProxy {
    override fun calculateTextWidth(text: FormattedText) = 0
    override fun calculateTextWidth(text: FormattedCharSequence) = 0
    override fun getTextHeight() = 9
    override fun wrapText(text: FormattedText, maxWidth: Int) = listOf(FormattedCharSequence.EMPTY)
}

class TooltipViewRendererTest {
    @Test
    fun `単一行と複数行のツールチップはクリップ解除と画面描画の後に描画するのだ`() {
        val tooltip = text { "tooltip"() }
        fun check(view: View) {
            GuiTestEnvironment().use { environment ->
                view.initSizeCalculation(TOOLTIP_RENDERING_PROXY, { 100 }, { 100 })
                lateinit var widget: VanillaViewRendererWidget<TooltipView, Unit>
                view.attachTo(IntPoint(10, 20)) { placedView, bounds ->
                    if (placedView is TooltipView) widget = VanillaViewRendererWidget(TooltipViewRenderer, placedView, bounds)
                    ViewController.NONE
                }
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

                environment.screen.renderWithTooltip(environment.graphics, 30, 25, 0.0F)
                Assertions.assertTrue(widget.isHovered)
                Assertions.assertEquals(listOf(listOf(tooltip.visualOrderText)), environment.tooltips)
                Assertions.assertEquals(listOf("scissor-end", "screen-end", "tooltip"), environment.events.takeLast(3))

                environment.events.clear()
                environment.tooltips.clear()
                environment.screen.renderWithTooltip(environment.graphics, 70, 25, 0.0F)
                Assertions.assertFalse(widget.isHovered)
                Assertions.assertTrue(environment.tooltips.isEmpty())
                Assertions.assertFalse("tooltip" in environment.events)
            }
        }
        check(View {
            view += TextView().configure {
                view.sizingX = Sizing.FILL
            }.tooltip(tooltip)
        })
        check(View {
            view += MultiLineTextView().configure().tooltip(tooltip)
        })
    }
}
