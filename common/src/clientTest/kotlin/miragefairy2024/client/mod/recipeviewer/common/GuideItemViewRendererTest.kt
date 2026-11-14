package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.GuideItemView
import net.minecraft.SharedConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.InOrder
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GuideItemViewRendererTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `背景とゴースト本体と白い重ね描きと個数を順に描画するのだ`() {
        GuiTestEnvironment().use {
            Mockito.mockStatic(Screen::class.java).use { screen ->
                screen.`when`<Boolean> { Screen.hasControlDown() }.thenReturn(true)
                val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
                val itemStack: ItemStack = ItemStack(Items.STONE, 64)
                val view = GuideItemView(listOf(itemStack)).also {
                    it.showOverlay = true
                    it.overlayArgb = 0x20FF0000
                }
                val bounds = IntRectangle(10, 20, 16, 16)

                GuideItemViewRenderer.render(view, bounds, graphics, 0, 0, false, 0.0F)

                val inOrder: InOrder = Mockito.inOrder(graphics)
                inOrder.verify(graphics).fill(10, 20, 26, 36, 0x20FF0000)
                inOrder.verify(graphics).renderFakeItem(itemStack, 10, 20)
                inOrder.verify(graphics).fill(RenderType.guiGhostRecipeOverlay(), 10, 20, 26, 36, 0x60FFFFFF)
                inOrder.verify(graphics).renderItemDecorations(Mockito.isNull(), Mockito.eq(itemStack), Mockito.eq(10), Mockito.eq(20))
                Mockito.verifyNoMoreInteractions(graphics)

                Mockito.clearInvocations(graphics)
                view.showOverlay = false
                GuideItemViewRenderer.render(view, bounds, graphics, 0, 0, false, 0.0F)
                inOrder.verify(graphics).renderFakeItem(itemStack, 10, 20)
                inOrder.verify(graphics).fill(RenderType.guiGhostRecipeOverlay(), 10, 20, 26, 36, 0x60FFFFFF)
                inOrder.verify(graphics).renderItemDecorations(Mockito.isNull(), Mockito.eq(itemStack), Mockito.eq(10), Mockito.eq(20))
                Mockito.verifyNoMoreInteractions(graphics)
            }
        }
    }

    @Test
    fun `本体を隠したときも背景を表示でき両方を隠すと描画しないのだ`() {
        GuiTestEnvironment().use {
            Mockito.mockStatic(Screen::class.java).use { screen ->
                screen.`when`<Boolean> { Screen.hasControlDown() }.thenReturn(true)
                val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
                val view = GuideItemView(listOf()).also {
                    it.showItemStack = false
                    it.showOverlay = true
                    it.overlayArgb = 0x2000FF00
                }
                val bounds = IntRectangle(10, 20, 16, 16)

                GuideItemViewRenderer.render(view, bounds, graphics, 0, 0, false, 0.0F)
                Mockito.verify(graphics).fill(10, 20, 26, 36, 0x2000FF00)
                Mockito.verifyNoMoreInteractions(graphics)

                Mockito.clearInvocations(graphics)
                view.showOverlay = false
                GuideItemViewRenderer.render(view, bounds, graphics, 0, 0, false, 0.0F)
                Mockito.verifyNoInteractions(graphics)
            }
        }
    }

}
