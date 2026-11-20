package miragefairy2024.client.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.util.Remover
import net.minecraft.world.inventory.Slot

/** 配置解除後も Slot はメニューに残るため、未登録と解除済みを区別して保持するのだ～🌱 */
class MenuSlotPlacementRegistry {

    class Placement(val clipBounds: () -> IntRectangle?)

    private val placementMap = mutableMapOf<Slot, Placement?>()

    fun isManaged(slot: Slot) = slot in placementMap
    fun getPlacement(slot: Slot) = placementMap[slot]

    /** [clipBounds] は取得時点の画面座標で返し、クリップしない場合は null を返すのだ～🌱 */
    fun register(slot: Slot, clipBounds: () -> IntRectangle?): Remover {
        val placement = Placement(clipBounds)
        placementMap[slot] = placement
        return Remover {
            if (placementMap[slot] === placement) placementMap[slot] = null
        }
    }

    fun clear() {
        placementMap.clear()
    }

}
