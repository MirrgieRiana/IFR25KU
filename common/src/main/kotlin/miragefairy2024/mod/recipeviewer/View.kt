package miragefairy2024.mod.recipeviewer

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

interface View {
    fun layout()
    fun getWidth(): Int
    fun getHeight(): Int
    fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int)
}

interface WidgetProxy {
    fun addInputSlotWidget(ingredient: Ingredient, x: Int, y: Int)
    fun addCatalystSlotWidget(ingredient: Ingredient, x: Int, y: Int)
    fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int)
}

object ViewScope

fun View(block: context(ViewScope) SingleView<View>.() -> Unit): View = ViewScope.run { Single { block(this) }.childView }
