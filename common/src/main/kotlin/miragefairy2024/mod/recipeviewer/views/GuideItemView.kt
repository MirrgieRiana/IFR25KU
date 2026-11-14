package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import net.minecraft.world.item.ItemStack

class GuideItemView(val itemStacks: List<ItemStack>) : FixedWidgetView(IntPoint(16, 16)) {
    var showItemStack = true
    var overlayArgb = 0x00000000
    var showOverlay = false

    fun getItemStack(time: Double): ItemStack = if (itemStacks.isEmpty()) ItemStack.EMPTY else itemStacks[(time / 30.0).toInt() % itemStacks.size]
}
