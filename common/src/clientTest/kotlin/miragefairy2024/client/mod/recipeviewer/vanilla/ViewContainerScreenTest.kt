package miragefairy2024.client.mod.recipeviewer.vanilla

import com.mojang.blaze3d.vertex.PoseStack
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.register
import miragefairy2024.mod.recipeviewer.views.FixedWidgetView
import miragefairy2024.util.empty
import miragefairy2024.util.text
import net.minecraft.SharedConstants
import net.minecraft.client.GameNarrator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics as GuiGraphicsExtractor // MIGRATION2612 ReplaceImport net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.server.Bootstrap
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

private class ClippedSlotView(val slot: Slot) : FixedWidgetView(IntPoint(40, 40)) {
    var clipBounds = IntRectangle.ZERO
}

class ViewContainerScreenTest {
    @Test
    fun `クリップされたスロットのアイテムはドラッグ配布時だけ描画するのだ`() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        VANILLA_VIEW_PLACER_REGISTRY.register { context, view: ClippedSlotView, _ ->
            val remover = context.registerMenuSlotPlacement(view.slot.index) { view.clipBounds }
            object : ViewController {
                override fun setOffset(offset: IntPoint) = Unit
                override fun detach() = remover.remove()
            }
        }

        GuiTestEnvironment().use {
            val stack = ItemStack(Items.STONE, 16)
            val slot = Slot(SimpleContainer(stack), 0, 8, 8)
            val view = ClippedSlotView(slot)
            val menu = object : AbstractContainerMenu(null, 0) {
                init {
                    addSlot(slot)
                }

                override fun quickMoveStack(player: Player, index: Int) = ItemStack.EMPTY
                override fun stillValid(player: Player) = true
            }
            val inventory = Inventory(Mockito.mock(Player::class.java))
            val screen = object : ViewContainerScreen<AbstractContainerMenu>(menu, inventory, text { empty() }) {
                override fun createRootView(screenEventSource: ScreenEventSource) = view

                fun setClip(offsetX: Int, offsetY: Int, width: Int, height: Int) {
                    view.clipBounds = IntRectangle(leftPos + slot.x + offsetX, topPos + slot.y + offsetY, width, height)
                }

                fun startQuickCrafting() {
                    isQuickCrafting = true
                }
            }
            val graphics = Mockito.mock(GuiGraphicsExtractor::class.java)
            Mockito.`when`(graphics.pose()).thenReturn(PoseStack())
            fun countItemDraws() = Mockito.mockingDetails(graphics).invocations.count { it.method.name == "renderItem" }

            val minecraft = Minecraft.getInstance()
            Mockito.`when`(minecraft.narrator).thenReturn(Mockito.mock(GameNarrator::class.java))
            screen.init(minecraft, 100, 100)
            try {
                screen.setClip(0, -18, 18, 18)
                screen.renderSlot(graphics, slot)
                Assertions.assertEquals(0, countItemDraws())

                screen.setClip(0, 15, 18, 1)
                screen.renderSlot(graphics, slot)
                Assertions.assertEquals(1, countItemDraws())

                screen.setClip(0, -18, 18, 18)
                screen.startQuickCrafting()
                screen.renderSlot(graphics, slot)
                Assertions.assertEquals(2, countItemDraws())
                val (x, y, width, height) = view.clipBounds
                Mockito.verify(graphics, Mockito.times(2)).enableScissor(x, y, x + width, y + height)
            } finally {
                screen.removed()
            }
        }
    }
}
