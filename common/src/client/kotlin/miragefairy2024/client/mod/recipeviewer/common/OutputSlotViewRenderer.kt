package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.util.SlotType
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/** スロットの背景のテクスチャは、枠を含めて18x18なのだ～🌱 */
private const val SLOT_TEXTURE_SIZE = 18

object OutputSlotViewRenderer : ViewRenderer<OutputSlotView> {
    override fun render(view: OutputSlotView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (view.drawBackground) {
            graphics.blit(SlotType.NORMAL.texture, bounds.x, bounds.y, 0F, 0F, bounds.sizeX, bounds.sizeY, SLOT_TEXTURE_SIZE, SLOT_TEXTURE_SIZE)
        }
        val itemX = bounds.x + view.margin
        val itemY = bounds.y + view.margin
        graphics.renderItem(view.itemStack, itemX, itemY)
        graphics.renderItemDecorations(Minecraft.getInstance().font, view.itemStack, itemX, itemY)
    }
}
