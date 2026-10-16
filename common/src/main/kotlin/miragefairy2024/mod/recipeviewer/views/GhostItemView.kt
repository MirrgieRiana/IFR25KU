package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import net.minecraft.world.item.ItemStack

/**
 * 入れるべきアイテムを、スロットへ重ねて薄く見せるViewなのだ～🌱
 * 複数ある場合は、一定の間隔で次のものへ移るのだ～🌱
 */
class GhostItemView(val itemStacks: List<ItemStack>) : FixedWidgetView(IntPoint(ITEM_SIZE, ITEM_SIZE)) {

    /** アイテムそのものを描くかなのだ～🌱 既に入っている場合は、描かないのだ～🌱 */
    var showItemStack = true

    /** スロットの状態を知らせるために、全体へ重ねる色なのだ～🌱 */
    var overlayArgb = 0

    /** 重ねるかどうかは、呼び出し元が毎ティック判定するのだ～🌱 */
    var showOverlay = false

    /** 次のアイテムへ移るまでを数える時間なのだ～🌱 描画の側が進めるのだ～🌱 */
    var time = 0.0

    /** この時点で見せるアイテムなのだ～🌱 */
    val itemStack get() = if (itemStacks.isEmpty()) ItemStack.EMPTY else itemStacks[(time / SWITCH_INTERVAL_TICKS).toInt() % itemStacks.size]

    companion object {
        private const val ITEM_SIZE = 16

        /** 次のアイテムへ移るまでのティック数なのだ～🌱 */
        private const val SWITCH_INTERVAL_TICKS = 30.0
    }

}
