package miragefairy2024.client.mod.recipeviewer.screen

import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.util.SlotType
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import net.minecraft.world.inventory.Slot

/** アイテムの絵の一辺の長さなのだ～🌱 */
private const val MENU_SLOT_SIZE = 16

/** アイテムの絵を囲む枠の太さなのだ～🌱 */
private const val MENU_SLOT_MARGIN = 1

const val MENU_SLOT_BACKGROUND_SIZE = MENU_SLOT_MARGIN + MENU_SLOT_SIZE + MENU_SLOT_MARGIN

/**
 * メニューのスロットを置くViewなのだ～🌱
 * 中身の描画と操作はバニラの画面がやるため、このViewは[net.minecraft.world.inventory.Slot]の座標を書き換えるだけなのだ～🌱
 * バニラはその座標を[net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.leftPos]からの相対で扱うため、書き込む側がその分を引くのだ～🌱
 */
class MenuSlotView(val slot: Slot) : AbstractView(), PlaceableView {

    override val sizingX = Sizing.WRAP

    override val sizingY = Sizing.WRAP

    override fun calculateContentSize() = IntPoint(MENU_SLOT_SIZE, MENU_SLOT_SIZE)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))

    /** スロットの座標の原点からの位置を書き込むのだ～🌱 */
    fun apply(offset: IntPoint) {
        (slot as SlotPositionSetter).`mirageFairy2024$setPosition`(offset.x, offset.y)
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
