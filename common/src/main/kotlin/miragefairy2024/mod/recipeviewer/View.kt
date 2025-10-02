package miragefairy2024.mod.recipeviewer

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient

interface View {
    fun layout(rendererProxy: RendererProxy)
    fun getWidth(): Int
    fun getHeight(): Int
    fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int)
}

interface RendererProxy

interface WidgetProxy {
    fun addInputSlotWidget(ingredient: Ingredient, x: Int, y: Int)
    fun addCatalystSlotWidget(ingredient: Ingredient, x: Int, y: Int)
    fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int)
}

fun View(block: SingleView<View>.() -> Unit): View = SingleView { block(this) }.childView
