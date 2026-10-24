package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.sized
import net.minecraft.world.inventory.Slot

/**
 * メニューのスロットを置くViewなのだ～🌱
 * 中身の描画と操作はバニラの画面がやるため、このViewは[net.minecraft.world.inventory.Slot]の座標を書き換えるだけなのだ～🌱
 */
class MenuSlotView(val slot: Slot) : AbstractView(), PlaceableView {

    override val sizingX = Sizing.WRAP

    override val sizingY = Sizing.WRAP

    override fun evaluateContentSizeXImpl() = 16

    override fun evaluateContentSizeYImpl() = 16

    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(evaluateActualSize()))

}
