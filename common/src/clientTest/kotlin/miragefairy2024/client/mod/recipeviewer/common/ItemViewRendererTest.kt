package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ItemView
import net.minecraft.SharedConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemViewRendererTest {

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
    }

    @Test
    fun `既定では個数などの表示も描画し無効時にはアイテム本体だけを描画するのだ`() {
        GuiTestEnvironment().use {
            val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
            val itemStack: ItemStack = ItemStack(Items.STONE, 64)
            val view = ItemView(itemStack)
            val bounds = IntRectangle(10, 20, 16, 16)

            ItemViewRenderer.render(view, bounds, graphics, 0, 0, false, 0.0F)
            Mockito.verify(graphics).renderFakeItem(itemStack, 10, 20)
            Mockito.verify(graphics).renderItemDecorations(Mockito.isNull(), Mockito.eq(itemStack), Mockito.eq(10), Mockito.eq(20))
            Mockito.verifyNoMoreInteractions(graphics)

            Mockito.clearInvocations(graphics)
            view.showDecorations = false
            ItemViewRenderer.render(view, bounds, graphics, 0, 0, false, 0.0F)
            Mockito.verify(graphics).renderFakeItem(itemStack, 10, 20)
            Mockito.verifyNoMoreInteractions(graphics)
        }
    }

}
