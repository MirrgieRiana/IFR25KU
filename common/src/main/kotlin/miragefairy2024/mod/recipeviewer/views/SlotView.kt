package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.IngredientStack
import net.minecraft.world.item.ItemStack

abstract class SlotView : AbstractView(), PlaceableView {
    var drawBackground = true
    var margin = 1
    override fun calculateContentSizeImpl() = IntPoint(margin + 16 + margin, margin + 16 + margin)
    override fun calculateActualSizeImpl() = IntPoint(margin + 16 + margin, margin + 16 + margin)
    override fun attachTo(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(actualSize))
}

fun <V : SlotView> V.noBackground() = this.apply { this.drawBackground = false }
fun <V : SlotView> V.margin(margin: Int) = this.apply { this.margin = margin }
fun <V : SlotView> V.noMargin() = this.margin(0)

class InputSlotView(val ingredientStack: IngredientStack) : SlotView()
class CatalystSlotView(val ingredientStack: IngredientStack) : SlotView()
class OutputSlotView(val itemStack: ItemStack) : SlotView()
