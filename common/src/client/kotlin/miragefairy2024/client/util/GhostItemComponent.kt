package miragefairy2024.client.util

import com.mojang.blaze3d.platform.Lighting
import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import miragefairy2024.util.EMPTY_ITEM_STACK
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import kotlin.jvm.optionals.getOrNull

class GhostItemComponent(var itemStacks: List<ItemStack> = listOf()) : BaseComponent() {
    var showItemStack = true
    var overlayColor: Int = 0x00000000
    var showOverlay = false

    var time = 0.0

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        if (!Screen.hasControlDown()) time += delta

        val itemStack = if (itemStacks.isNotEmpty()) itemStacks[(time / 30.0).toInt() % itemStacks.size] else EMPTY_ITEM_STACK

        val notSideLit = !Minecraft.getInstance().itemRenderer.getModel(itemStack, null, null, 0).usesBlockLight()


        // アイテム本体
        if (showItemStack) {
            if (notSideLit) Lighting.setupForFlatItems()
            context.renderItem(itemStack, x, y)
            if (notSideLit) Lighting.setupFor3DItems()
        }

        // 個数
        if (showItemStack) context.renderItemDecorations(Minecraft.getInstance().font, itemStack, x, y)

        context.pose().stack {
            context.pose().translate(0F, 0F, 200F)
            if (showItemStack) context.drawGradientRect(x, y, width, height, 0x808B8B8B.toInt(), 0x808B8B8B.toInt(), 0x808B8B8B.toInt(), 0x808B8B8B.toInt()) // アイテムを半透明にするやつ
            if (showOverlay) context.drawGradientRect(x, y, width, height, overlayColor, overlayColor, overlayColor, overlayColor) // オーバーレイ
        }

    }

    /** スロットのツールチップに通常のTooltip機能を使用すると、スロットのオーバーレイの四角形が後にレンダリングされ表示が壊れます。 */
    fun drawGhostTooltip(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        if (!isInBoundingBox(mouseX.toDouble(), mouseY.toDouble())) return

        val itemStack = if (itemStacks.isNotEmpty()) itemStacks[(time / 30.0).toInt() % itemStacks.size] else EMPTY_ITEM_STACK
        if (itemStack.isEmpty) return

        val tooltip = mutableListOf<ClientTooltipComponent>()

        val tooltipContext = if (Minecraft.getInstance().options.advancedItemTooltips) TooltipFlag.ADVANCED else TooltipFlag.NORMAL
        val texts = itemStack.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, tooltipContext)
        tooltip += texts.map { ClientTooltipComponent.create(it.visualOrderText) }

        val data = itemStack.tooltipImage.getOrNull()
        if (data != null) tooltip.add(1, TooltipComponentCallback.EVENT.invoker().getComponent(data) ?: ClientTooltipComponent.create(data))

        context.drawTooltip(Minecraft.getInstance().font, mouseX, mouseY, tooltip)
        context.flush()
    }

    override fun determineHorizontalContentSize(sizing: Sizing) = 16
    override fun determineVerticalContentSize(sizing: Sizing) = 16
}
