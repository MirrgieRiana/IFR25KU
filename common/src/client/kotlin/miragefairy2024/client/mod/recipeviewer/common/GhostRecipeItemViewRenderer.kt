package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.GhostRecipeItemView
import miragefairy2024.util.isNotEmpty
import mirrg.kotlin.helium.Slot
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.ItemStack

object GhostRecipeItemViewRenderer : ViewRenderer<GhostRecipeItemView, Double> {
    override fun createState(view: GhostRecipeItemView) = 0.0

    override fun render(view: GhostRecipeItemView, state: Slot<Double>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        val minecraft: Minecraft = Minecraft.getInstance()
        if (!Screen.hasControlDown()) state.value += minecraft.timer.realtimeDeltaTicks

        val itemStack = if (view.itemStacks.isEmpty()) ItemStack.EMPTY else view.itemStacks[(state.value / 30.0).toInt() % view.itemStacks.size]
        if (view.showOverlay) {
            graphics.fill(bounds.x, bounds.y, bounds.x2, bounds.y2, view.overlayArgb)
        }
        if (view.showItemStack) {
            graphics.renderFakeItem(itemStack, bounds.x, bounds.y)
            graphics.fill(RenderType.guiGhostRecipeOverlay(), bounds.x, bounds.y, bounds.x2, bounds.y2, 0x60FFFFFF)
            graphics.renderItemDecorations(minecraft.font, itemStack, bounds.x, bounds.y)
            if (isHovered && itemStack.isNotEmpty) {
                minecraft.screen?.setTooltipForNextRenderPass(Screen.getTooltipFromItem(minecraft, itemStack).map { it.visualOrderText })
            }
        }
    }

}
