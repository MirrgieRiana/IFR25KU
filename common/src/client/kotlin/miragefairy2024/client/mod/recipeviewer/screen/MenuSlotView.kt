package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.util.SlotType
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.Remover
import net.minecraft.world.inventory.Slot

/** アイテムの絵の一辺の長さなのだ～🌱 */
const val MENU_SLOT_SIZE = 16

/** アイテムの絵を囲む枠の太さなのだ～🌱 */
private const val MENU_SLOT_MARGIN = 1

const val MENU_SLOT_BACKGROUND_SIZE = MENU_SLOT_MARGIN + MENU_SLOT_SIZE + MENU_SLOT_MARGIN

/**
 * メニューのスロットを置くViewなのだ～🌱
 * 中身の描画と操作はバニラの画面がやるため、このViewは[net.minecraft.world.inventory.Slot]の座標を書き換えるだけなのだ～🌱
 * 座標は画面の左上からの相対であるため、[miragefairy2024.client.mod.recipeviewer.screen.ViewContainerScreen]が原点を画面の左上へ合わせるのだ～🌱
 */
class MenuSlotView(private val slot: Slot) : AbstractView() {

    override val sizingX = Sizing.WRAP

    override val sizingY = Sizing.WRAP

    override fun calculateContentSize() = IntPoint(MENU_SLOT_SIZE, MENU_SLOT_SIZE)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
        (slot as SlotPositionSetter).`mirageFairy2024$setPosition`(offset.x, offset.y)
        return Remover.NONE
    }

}

/**
 * スロットの枠を敷いて、中にメニューのスロットを置くのだ～🌱
 * 枠のテクスチャは一辺が[miragefairy2024.client.mod.recipeviewer.screen.MENU_SLOT_BACKGROUND_SIZE]の1枚だけであるため、四辺を0にして中央だけを敷くのだ～🌱
 */
fun menuSlotView(slot: Slot, slotType: SlotType?): View {
    return View {
        view += StackView().configure {
            if (slotType != null) view += NinePatchImageView(slotType.texture, 0, MENU_SLOT_BACKGROUND_SIZE, 0, 0, MENU_SLOT_BACKGROUND_SIZE, 0)
            view += MenuSlotView(slot).configure { }.margin(MENU_SLOT_MARGIN)
        }
    }
}
