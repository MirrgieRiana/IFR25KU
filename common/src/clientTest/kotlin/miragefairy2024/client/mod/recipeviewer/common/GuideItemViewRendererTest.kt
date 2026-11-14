package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.vanilla.GuiTestEnvironment
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.GuideItemView
import mirrg.kotlin.helium.Slot
import net.minecraft.SharedConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.server.Bootstrap
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions
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

    private fun checkItemStack(view: GuideItemView, time: Double, expected: ItemStack) {
        GuiTestEnvironment().use {
            Mockito.mockStatic(Screen::class.java).use { screen ->
                screen.`when`<Boolean> { Screen.hasControlDown() }.thenReturn(true)
                val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
                GuideItemViewRenderer.render(view, Slot(time), IntRectangle(10, 20, 16, 16), graphics, 0, 0, false, 0.0F)
                Mockito.verify(graphics).renderFakeItem(Mockito.same(expected), Mockito.eq(10), Mockito.eq(20))
            }
        }
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
                val state = Slot(GuideItemViewRenderer.createState(view))

                GuideItemViewRenderer.render(view, state, bounds, graphics, 0, 0, false, 0.0F)

                val inOrder: InOrder = Mockito.inOrder(graphics)
                inOrder.verify(graphics).fill(10, 20, 26, 36, 0x20FF0000)
                inOrder.verify(graphics).renderFakeItem(itemStack, 10, 20)
                inOrder.verify(graphics).fill(RenderType.guiGhostRecipeOverlay(), 10, 20, 26, 36, 0x60FFFFFF)
                inOrder.verify(graphics).renderItemDecorations(Mockito.isNull(), Mockito.eq(itemStack), Mockito.eq(10), Mockito.eq(20))
                Mockito.verifyNoMoreInteractions(graphics)

                Mockito.clearInvocations(graphics)
                view.showOverlay = false
                GuideItemViewRenderer.render(view, state, bounds, graphics, 0, 0, false, 0.0F)
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
                val state = Slot(GuideItemViewRenderer.createState(view))

                GuideItemViewRenderer.render(view, state, bounds, graphics, 0, 0, false, 0.0F)
                Mockito.verify(graphics).fill(10, 20, 26, 36, 0x2000FF00)
                Mockito.verifyNoMoreInteractions(graphics)

                Mockito.clearInvocations(graphics)
                view.showOverlay = false
                GuideItemViewRenderer.render(view, state, bounds, graphics, 0, 0, false, 0.0F)
                Mockito.verifyNoInteractions(graphics)
            }
        }
    }

    @Test
    fun `候補が空なら空のアイテムを描画に渡すのだ`() {
        val view = GuideItemView(emptyList())
        checkItemStack(view, 0.0, ItemStack.EMPTY)
        checkItemStack(view, 90.0, ItemStack.EMPTY)
    }

    @Test
    fun `候補は30ティックごとに切り替わって先頭へ戻るのだ`() {
        val first: ItemStack = ItemStack(Items.STONE)
        val second: ItemStack = ItemStack(Items.DIRT)
        val third: ItemStack = ItemStack(Items.GLASS)
        val view = GuideItemView(listOf(first, second, third))
        checkItemStack(view, 0.0, first)
        checkItemStack(view, 29.999, first)
        checkItemStack(view, 30.0, second)
        checkItemStack(view, 60.0, third)
        checkItemStack(view, 90.0, first)
    }

    @Test
    fun `候補が1個なら時間が経っても同じアイテムを描画するのだ`() {
        val itemStack: ItemStack = ItemStack(Items.STONE)
        val view = GuideItemView(listOf(itemStack))
        checkItemStack(view, 1000.0, itemStack)
    }

    @Test
    fun `同じViewでも配置ごとの候補を描画しCtrl中は時間を進めないのだ`() {
        GuiTestEnvironment().use {
            Mockito.mockStatic(Screen::class.java).use { screen ->
                screen.`when`<Boolean> { Screen.hasControlDown() }.thenReturn(true)
                val graphics: GuiGraphics = Mockito.mock(GuiGraphics::class.java)
                val first: ItemStack = ItemStack(Items.STONE)
                val second: ItemStack = ItemStack(Items.DIRT)
                val view = GuideItemView(listOf(first, second))
                val bounds = IntRectangle(10, 20, 16, 16)
                val firstState = Slot(GuideItemViewRenderer.createState(view))
                val secondState = Slot(GuideItemViewRenderer.createState(view))
                Assertions.assertEquals(0.0, firstState.value)
                Assertions.assertEquals(0.0, secondState.value)
                secondState.value = 30.0

                GuideItemViewRenderer.render(view, firstState, bounds, graphics, 0, 0, false, 0.0F)
                GuideItemViewRenderer.render(view, secondState, bounds, graphics, 0, 0, false, 0.0F)
                GuideItemViewRenderer.render(view, firstState, bounds, graphics, 0, 0, false, 0.0F)

                val inOrder: InOrder = Mockito.inOrder(graphics)
                inOrder.verify(graphics).renderFakeItem(first, 10, 20)
                inOrder.verify(graphics).renderFakeItem(second, 10, 20)
                inOrder.verify(graphics).renderFakeItem(first, 10, 20)
                Assertions.assertEquals(0.0, firstState.value)
                Assertions.assertEquals(30.0, secondState.value)
            }
        }
    }

}
