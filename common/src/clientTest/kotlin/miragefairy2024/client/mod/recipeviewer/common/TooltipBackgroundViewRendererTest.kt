package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import net.minecraft.client.gui.GuiGraphics
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class TooltipBackgroundViewRendererTest {

    @Test
    fun `いずれかの寸法が零の背景は描画せず通常寸法の背景は描画するのだ`() {
        val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
        val view = TooltipBackgroundView()
        listOf(IntRectangle(10, 20, 0, 0), IntRectangle(10, 20, 0, 100), IntRectangle(10, 20, 100, 0)).forEach { bounds ->
            TooltipBackgroundViewRenderer.render(view, bounds, graphics, 30, 40, false, 0.0F)
        }
        Mockito.verifyNoInteractions(graphics)

        TooltipBackgroundViewRenderer.render(view, IntRectangle(10, 20, 100, 100), graphics, 30, 40, false, 0.0F)
        @Suppress("DEPRECATION")
        Mockito.verify(graphics).drawManaged(Mockito.any(Runnable::class.java))
    }

}
