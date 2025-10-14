package miragefairy2024.client.mod.magicplant

import miragefairy2024.ModContext
import miragefairy2024.client.mixins.api.RenderingEvent
import miragefairy2024.client.mod.recipeviewer.ViewOwoAdapterRegistry
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.magicplant.MagicPlantSeedItem
import miragefairy2024.mod.magicplant.TraitEncyclopediaView
import miragefairy2024.mod.magicplant.getTraitStacks
import miragefairy2024.mod.magicplant.minus
import miragefairy2024.mod.magicplant.negativeBitCount
import miragefairy2024.mod.magicplant.positiveBitCount
import miragefairy2024.mod.magicplant.traitListScreenHandlerType
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType

context(ModContext)
fun initMagicPlantClientModule() {
    traitListScreenHandlerType.registerHandledScreen { gui, inventory, title -> TraitListScreen(gui, inventory, title) }

    RenderingEvent.RENDER_ITEM_DECORATIONS.register { graphics, font, stack, x, y, text ->
        if (stack.item !is MagicPlantSeedItem) return@register

        val player = Minecraft.getInstance().player ?: return@register
        val otherItemStack = player.mainHandItem
        if (otherItemStack === stack) return@register

        val traitStacks = stack.getTraitStacks() ?: return@register
        val otherTraitStacks = if (otherItemStack.item is MagicPlantSeedItem) otherItemStack.getTraitStacks() ?: return@register else return@register
        val plusBitCount = (traitStacks - otherTraitStacks).positiveBitCount + (otherTraitStacks - traitStacks).negativeBitCount
        val minusBitCount = (otherTraitStacks - traitStacks).positiveBitCount + (traitStacks - otherTraitStacks).negativeBitCount

        graphics.pose().pushPose()
        try {
            graphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 8, 0x888B8B8B.toInt())
            graphics.pose().translate(0.0F, 0.0F, 200.0F)
            if (plusBitCount > 0) graphics.drawString(font, "$plusBitCount", x, y, ChatFormatting.GREEN.color!!, false)
            if (minusBitCount > 0) graphics.drawString(font, "$minusBitCount", x + 19 - 2 - font.width("$minusBitCount"), y, ChatFormatting.DARK_RED.color!!, false)
        } finally {
            graphics.pose().popPose()
        }
    }

    ViewOwoAdapterRegistry.register(TraitEncyclopediaView::class.java, TraitEncyclopediaViewOwoAdapter)
}
