package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import net.minecraft.world.item.ItemStack

class GhostItemView(val itemStacks: List<ItemStack>) : FixedWidgetView(IntPoint(16, 16)) {
    var showItemStack = true
    var overlayArgb = 0x00000000
    var showOverlay = false

    /** 候補の切り替えに使う経過ティック数なのだ～🌱 */
    var time = 0.0

    val itemStack get() = if (itemStacks.isEmpty()) ItemStack.EMPTY else itemStacks[(time / 30.0).toInt() % itemStacks.size]
}
