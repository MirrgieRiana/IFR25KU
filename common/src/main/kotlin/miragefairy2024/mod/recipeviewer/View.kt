package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.IngredientStack
import net.minecraft.world.item.ItemStack

interface View {
    fun layout(rendererProxy: RendererProxy)
    fun getWidth(): Int
    fun getHeight(): Int
    fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int)
}

interface RendererProxy

interface WidgetProxy {
    fun addInputSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int, drawBackground: Boolean)
    fun addCatalystSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int, drawBackground: Boolean)
    fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int, drawBackground: Boolean)
    fun addArrow(x: Int, y: Int, durationMilliSeconds: Int?)
}

enum class Alignment {
    START, CENTER, END,
}

fun View(block: SingleView<View>.() -> Unit): View = SingleView { block(this) }.childView
