package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.mixins.api.SlotPositionSetter
import miragefairy2024.client.util.SlotType
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.mod.recipeviewer.views.AbstractView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import net.minecraft.world.inventory.Slot

/**
 * メニューのスロットを置くViewなのだ～🌱
 * 中身の描画と操作はバニラの画面がやるため、このViewは[net.minecraft.world.inventory.Slot]の座標を書き換えるだけなのだ～🌱
 */
class MenuSlotView(val slot: Slot) : AbstractView(), PlaceableView {

    override val sizingX = Sizing.WRAP

    override val sizingY = Sizing.WRAP

    override fun calculateContentSize() = IntPoint(16, 16)

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))

    /** バニラは[net.minecraft.world.inventory.Slot]の座標へ[miragefairy2024.client.mod.recipeviewer.vanilla.VanillaViewPlacerContext.origin]を描画とホバー判定の際に加算するため、絶対座標を書き込むと二重に加算されるのだ～🌱 */
    fun apply(offset: IntPoint) {
        (slot as SlotPositionSetter).`mirageFairy2024$setPosition`(offset.x, offset.y)
    }

}

/** スロットの枠を敷いて、中にメニューのスロットを置くのだ～🌱 */
fun menuSlotView(slot: Slot, slotType: SlotType?): View {
    return View {
        view += StackView().configure {
            if (slotType != null) view += ImageView(ViewTexture(slotType.texture, IntPoint(18, 18), IntRectangle(0, 0, 18, 18)))
            view += MenuSlotView(slot).configure { }.margin(1)
        }
    }
}
