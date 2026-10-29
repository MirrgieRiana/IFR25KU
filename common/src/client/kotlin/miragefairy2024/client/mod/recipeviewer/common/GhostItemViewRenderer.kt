package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.client.util.stack
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.contains
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.GhostItemView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

object GhostItemViewRenderer : ViewRenderer<GhostItemView> {
    override fun render(view: GhostItemView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val minecraft = Minecraft.getInstance()
        if (!Screen.hasControlDown()) view.time += minecraft.timer.realtimeDeltaTicks

        val itemStack = view.itemStack
        if (view.showItemStack) {
            graphics.renderItem(itemStack, bounds.x, bounds.y)
            graphics.renderItemDecorations(minecraft.font, itemStack, bounds.x, bounds.y)
        }

        graphics.pose().stack {
            graphics.pose().translate(0.0F, 0.0F, 200.0F)
            if (view.showItemStack) graphics.fill(bounds.x, bounds.y, bounds.x2, bounds.y2, 0x808B8B8B.toInt())
            if (view.showOverlay) graphics.fill(bounds.x, bounds.y, bounds.x2, bounds.y2, view.overlayArgb)
        }
    }

    /** スロットとオーバーレイに隠れないよう、画面本体の描画後に呼ぶのだ～🌱 */
    fun renderTooltip(view: GhostItemView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (!bounds.contains(mouseX, mouseY)) return
        val itemStack = view.itemStack
        if (itemStack.isEmpty) return
        graphics.renderTooltip(Minecraft.getInstance().font, itemStack, mouseX, mouseY)
    }
}
