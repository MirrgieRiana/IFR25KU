package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import net.minecraft.world.item.ItemStack

/**
 * 入れるべきアイテムを、スロットへ重ねて薄く見せる View なのだ～🌱
 * 複数ある場合は、一定の間隔で次のものへ移るのだ～🌱
 */
class GhostItemView(val itemStacks: List<ItemStack>) : FixedWidgetView(IntPoint(16, 16)) {

    /** アイテムそのものを描くかなのだ～🌱 既に入っている場合は、描かないのだ～🌱 */
    var showItemStack = true

    /** スロットの状態を知らせるために、全体へ重ねる色なのだ～🌱 */
    var overlayArgb = 0

    /** 重ねるかどうかは、呼び出し元が毎ティック判定するのだ～🌱 */
    var showOverlay = false

    /** 次のアイテムへ移るまでを数える時間なのだ～🌱 描画の側が進めるのだ～🌱 */
    var time = 0.0

    /** この時点で見せるアイテムなのだ～🌱 */
    val itemStack get() = if (itemStacks.isEmpty()) ItemStack.EMPTY else itemStacks[(time / 30.0).toInt() % itemStacks.size]

}
