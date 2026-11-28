package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.common.SlotType
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import mirrg.kotlin.helium.atLeast
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.inventory.Slot

/**
 * メニューのスロットを置くViewなのだ～🌱
 * この View は [net.minecraft.world.inventory.Slot] の配置を管理し、中身の描画と操作はバニラの画面が行うのだ～🌱
 */
class MenuSlotView(val slot: Slot) : AbstractView(), PlaceableView {
    var margin = IntPoint(1, 1)
    override val sizingX = Sizing.WRAP
    override val sizingY = Sizing.WRAP
    override fun evaluateContentSizeXImpl() = margin.x + 16 + margin.x atLeast 0
    override fun evaluateContentSizeYImpl() = margin.y + 16 + margin.y atLeast 0
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))
}

/** スロットの枠を敷いて、中にメニューのスロットを置くのだ～🌱 */
fun menuSlotView(slot: Slot, type: SlotType): View {
    return View {
        view += StackView().configure {
            view += ImageView(type.viewTexture)
            view += MenuSlotView(slot).configure {
                view.margin = type.margin
            }
        }
    }
}
