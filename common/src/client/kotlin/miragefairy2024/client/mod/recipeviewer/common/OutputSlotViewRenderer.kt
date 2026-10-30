package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

object OutputSlotViewRenderer : ViewRenderer<OutputSlotView> {
    override fun render(view: OutputSlotView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (view.drawBackground) {
            val texture = SlotType.NORMAL.viewTexture
            graphics.blit(texture.id, bounds.x, bounds.y, 0.0F, 0.0F, bounds.sizeX, bounds.sizeY, texture.size.x, texture.size.y)
        }
        val itemX = bounds.x + view.margin
        val itemY = bounds.y + view.margin
        graphics.renderItem(view.itemStack, itemX, itemY)
        graphics.renderItemDecorations(Minecraft.getInstance().font, view.itemStack, itemX, itemY)
    }
}
