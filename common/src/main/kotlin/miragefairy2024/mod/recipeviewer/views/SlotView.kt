package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.RendererProxy
import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.mod.recipeviewer.view.ViewPlacer
import miragefairy2024.util.IngredientStack
import net.minecraft.world.item.ItemStack

abstract class SlotView : View {
    var drawBackground = true
    var margin = 1
    override fun layout(rendererProxy: RendererProxy) = Unit
    override fun getMinWidth() = getWidth()
    override fun getMinHeight() = getHeight()
    override fun getWidth() = 16 + margin * 2
    override fun getHeight() = 16 + margin * 2
    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>) = viewPlacer.place(this, x, y)
}

fun <V : SlotView> V.noBackground() = this.apply { this.drawBackground = false }
fun <V : SlotView> V.margin(margin: Int) = this.apply { this.margin = margin }
fun <V : SlotView> V.noMargin() = this.margin(0)

class InputSlotView(val ingredientStack: IngredientStack) : SlotView()
class CatalystSlotView(val ingredientStack: IngredientStack) : SlotView()
class OutputSlotView(val itemStack: ItemStack) : SlotView()
