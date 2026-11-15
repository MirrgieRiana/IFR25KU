package miragefairy2024.client.util

import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import miragefairy2024.util.EMPTY_ITEM_STACK
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.RenderType
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

        // 背景
        if (showOverlay) {
            context.fill(x, y, x + width, y + height, overlayColor)
        }

        // アイテム本体と個数
        if (showItemStack) {
            context.renderFakeItem(itemStack, x, y)
            context.fill(RenderType.guiGhostRecipeOverlay(), x, y, x + width, y + height, 0x60FFFFFF)
            context.renderItemDecorations(Minecraft.getInstance().font, itemStack, x, y)
        }

    }

    /** スロットのツールチップに通常のTooltip機能を使用すると、スロットのオーバーレイの四角形が後にレンダリングされ表示が壊れます。 */
    fun drawGhostTooltip(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        if (!showItemStack) return
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
