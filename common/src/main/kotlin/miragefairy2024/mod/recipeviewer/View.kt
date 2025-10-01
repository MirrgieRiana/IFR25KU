package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.IngredientStack
import net.minecraft.world.item.ItemStack

interface View {
    fun layout()
    fun getWidth(): Int
    fun getHeight(): Int
    fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int)
}

interface WidgetProxy {
    fun addInputSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int)
    fun addCatalystSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int)
    fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int)
}

object ViewScope

fun View(block: context(ViewScope) SingleView<View>.() -> Unit): View = ViewScope.run { Single { block(this) }.childView }
