package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.client.util.SlotType
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MenuSlotView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.plusAssign
import net.minecraft.world.inventory.Slot

/** スロットの枠を敷いて、中にメニューのスロットを置くのだ～🌱 */
fun menuSlotView(slot: Slot, slotType: SlotType?): View {
    return View {
        view += StackView().configure {
            if (slotType != null) view += ImageView(ViewTexture(slotType.texture, IntPoint(18, 18), IntRectangle(0, 0, 18, 18)))
            view += MenuSlotView(slot).configure { }.margin(1)
        }
    }
}
