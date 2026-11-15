package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.TooltipBackgroundView
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class TooltipBackgroundViewRendererTest {

    @Test
    fun `いずれかの寸法が4未満の背景は描画しないのだ`() {
        val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
        val view = TooltipBackgroundView()
        listOf(IntRectangle(10, 20, 0, 0), IntRectangle(10, 20, 0, 100), IntRectangle(10, 20, 100, 0), IntRectangle(10, 20, 3, 100), IntRectangle(10, 20, 100, 3)).forEach { bounds ->
            TooltipBackgroundViewRenderer.render(view, Slot(Unit), bounds, graphics, 30, 40, false, 0.0F)
        }
        Mockito.verifyNoInteractions(graphics)
    }

    @Test
    fun `両方の寸法が4以上の背景は描画するのだ`() {
        val view = TooltipBackgroundView()
        listOf(IntRectangle(10, 20, 4, 4), IntRectangle(10, 20, 100, 100)).forEach { bounds ->
            val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
            TooltipBackgroundViewRenderer.render(view, Slot(Unit), bounds, graphics, 30, 40, false, 0.0F)
            @Suppress("DEPRECATION")
            Mockito.verify(graphics).drawManaged(Mockito.any(Runnable::class.java))
        }
    }

}
