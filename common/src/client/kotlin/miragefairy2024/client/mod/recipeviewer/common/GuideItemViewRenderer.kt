package miragefairy2024.client.mod.recipeviewer.common

import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.contains
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.GuideItemView
import mirrg.kotlin.helium.Slot
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType

object GuideItemViewRenderer : ViewRenderer<GuideItemView, Double> {
    override fun createState(view: GuideItemView) = 0.0

    override fun render(view: GuideItemView, state: Slot<Double>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) {
        val minecraft = Minecraft.getInstance()
        if (!Screen.hasControlDown()) state.value += minecraft.timer.realtimeDeltaTicks

        val itemStack = view.getItemStack(state.value)
        if (view.showOverlay) {
            graphics.fill(bounds.x, bounds.y, bounds.x2, bounds.y2, view.overlayArgb)
        }
        if (view.showItemStack) {
            graphics.renderFakeItem(itemStack, bounds.x, bounds.y)
            graphics.fill(RenderType.guiGhostRecipeOverlay(), bounds.x, bounds.y, bounds.x2, bounds.y2, 0x60FFFFFF)
            graphics.renderItemDecorations(minecraft.font, itemStack, bounds.x, bounds.y)
        }
    }

    /** 描画時と同じ [state] を渡し、スロットとオーバーレイに隠れないよう画面本体の描画後に呼ぶのだ～🌱 */
    fun renderTooltip(view: GuideItemView, state: Slot<Double>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (!bounds.contains(mouseX, mouseY)) return
        val itemStack = view.getItemStack(state.value)
        if (itemStack.isEmpty) return
        graphics.renderTooltip(Minecraft.getInstance().font, itemStack, mouseX, mouseY)
    }
}
