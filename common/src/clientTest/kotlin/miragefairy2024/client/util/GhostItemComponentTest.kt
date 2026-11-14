package miragefairy2024.client.util

import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Size
import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import net.minecraft.SharedConstants
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
class GhostItemComponentTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `背景とゴースト本体と専用の白い重ね描きと個数を順に描画するのだ`() {
        GuiTestEnvironment().use {
            Mockito.mockStatic(Screen::class.java).use {
                val context: OwoUIDrawContext = Mockito.mock(OwoUIDrawContext::class.java)
                val itemStack: ItemStack = ItemStack(Items.STONE, 64)
                val component = GhostItemComponent(listOf(itemStack)).also {
                    it.updateX(10)
                    it.updateY(20)
                    it.inflate(Size.of(16, 16))
                    it.overlayColor = 0x20FF0000
                    it.showOverlay = true
                }

                component.draw(context, 0, 0, 0.0F, 0.0F)

                val inOrder: InOrder = Mockito.inOrder(context)
                inOrder.verify(context).fill(10, 20, 26, 36, 0x20FF0000)
                inOrder.verify(context).renderFakeItem(itemStack, 10, 20)
                inOrder.verify(context).fill(RenderType.guiGhostRecipeOverlay(), 10, 20, 26, 36, 0x30FFFFFF)
                inOrder.verify(context).renderItemDecorations(Mockito.isNull(), Mockito.eq(itemStack), Mockito.eq(10), Mockito.eq(20))
                Mockito.verifyNoMoreInteractions(context)

                Mockito.clearInvocations(context)
                component.showOverlay = false
                component.draw(context, 0, 0, 0.0F, 0.0F)
                inOrder.verify(context).renderFakeItem(itemStack, 10, 20)
                inOrder.verify(context).fill(RenderType.guiGhostRecipeOverlay(), 10, 20, 26, 36, 0x30FFFFFF)
                inOrder.verify(context).renderItemDecorations(Mockito.isNull(), Mockito.eq(itemStack), Mockito.eq(10), Mockito.eq(20))
                Mockito.verifyNoMoreInteractions(context)
            }
        }
    }

    @Test
    fun `本体を隠したときも背景を表示でき両方を隠すと描画しないのだ`() {
        GuiTestEnvironment().use {
            Mockito.mockStatic(Screen::class.java).use {
                val context: OwoUIDrawContext = Mockito.mock(OwoUIDrawContext::class.java)
                val component = GhostItemComponent(listOf()).also {
                    it.updateX(10)
                    it.updateY(20)
                    it.inflate(Size.of(16, 16))
                    it.showItemStack = false
                    it.overlayColor = 0x2000FF00
                    it.showOverlay = true
                }

                component.draw(context, 0, 0, 0.0F, 0.0F)
                Mockito.verify(context).fill(10, 20, 26, 36, 0x2000FF00)
                Mockito.verifyNoMoreInteractions(context)

                Mockito.clearInvocations(context)
                component.showOverlay = false
                component.draw(context, 0, 0, 0.0F, 0.0F)
                Mockito.verifyNoInteractions(context)
            }
        }
    }

}
