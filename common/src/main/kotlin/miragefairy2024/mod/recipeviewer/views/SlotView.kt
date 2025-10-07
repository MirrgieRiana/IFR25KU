package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.mod.recipeviewer.view.ViewWithMinSize
import miragefairy2024.mod.recipeviewer.view.ViewWithSize
import miragefairy2024.mod.recipeviewer.view.sized
import miragefairy2024.util.IngredientStack
import net.minecraft.world.item.ItemStack

abstract class SlotView : View, PlaceableView {
    var drawBackground = true
    var margin = 1
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint(16 + margin * 2, 16 + margin * 2)
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = minSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this@SlotView, offset.sized(size))
                }
            }
        }
    }
}

fun <V : SlotView> V.noBackground() = this.apply { this.drawBackground = false }
fun <V : SlotView> V.margin(margin: Int) = this.apply { this.margin = margin }
fun <V : SlotView> V.noMargin() = this.margin(0)

class InputSlotView(val ingredientStack: IngredientStack) : SlotView()
class CatalystSlotView(val ingredientStack: IngredientStack) : SlotView()
class OutputSlotView(val itemStack: ItemStack) : SlotView()
