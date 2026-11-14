package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.views.ItemView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

object ItemViewRenderer : ViewRenderer<ItemView, Unit> {
    override fun render(view: ItemView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        graphics.renderFakeItem(view.itemStack, bounds.x, bounds.y)
        if (view.showDecorations) graphics.renderItemDecorations(Minecraft.getInstance().font, view.itemStack, bounds.x, bounds.y)
    }
}
