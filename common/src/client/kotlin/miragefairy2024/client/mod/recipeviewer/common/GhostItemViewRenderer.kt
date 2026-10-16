package miragefairy2024.client.mod.recipeviewer.common

import com.mojang.blaze3d.platform.Lighting
import miragefairy2024.client.mod.recipeviewer.ViewRenderer
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.contains
import miragefairy2024.mod.recipeviewer.view.x1
import miragefairy2024.mod.recipeviewer.view.x2
import miragefairy2024.mod.recipeviewer.view.y1
import miragefairy2024.mod.recipeviewer.view.y2
import miragefairy2024.mod.recipeviewer.views.GhostItemView
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

/** 入れるべきアイテムを薄く見せる色なのだ～🌱 */
private const val ITEM_OVERLAY_ARGB = 0x808B8B8B.toInt()

object GhostItemViewRenderer : ViewRenderer<GhostItemView> {
    override fun render(view: GhostItemView, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // 左コントロールを押している間は止めて、アイテムを読む時間を取れるようにするのだ～🌱
        if (!Screen.hasControlDown()) view.time += delta

        val itemStack = view.itemStack

        if (view.showItemStack) {
            // ブロックの明かりを使わないモデルは、平らに照らさないと暗く描かれるのだ～🌱
            val flat = !Minecraft.getInstance().itemRenderer.getModel(itemStack, null, null, 0).usesBlockLight()
            if (flat) Lighting.setupForFlatItems()
            graphics.renderItem(itemStack, bounds.x, bounds.y)
            graphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, bounds.x, bounds.y)
            if (flat) Lighting.setupFor3DItems()
        }

        graphics.pose().pushPose()
        // アイテムより手前へ出すのだ～🌱
        graphics.pose().translate(0F, 0F, 200F)
        if (view.showItemStack) graphics.fill(bounds.x1, bounds.y1, bounds.x2, bounds.y2, ITEM_OVERLAY_ARGB)
        if (view.showOverlay) graphics.fill(bounds.x1, bounds.y1, bounds.x2, bounds.y2, view.overlayArgb)
        graphics.pose().popPose()

        // 重ねる四角形より後で描かないと、ツールチップが隠れるのだ～🌱
        if (!bounds.contains(mouseX, mouseY)) return
        if (itemStack.isEmpty) return
        graphics.renderTooltip(Minecraft.getInstance().font, itemStack, mouseX, mouseY)
    }
}
