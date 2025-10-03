package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.IngredientStack
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

interface View {
    fun layout(rendererProxy: RendererProxy)
    fun getWidth(): Int
    fun getHeight(): Int
    fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int)
}

interface RendererProxy {
    fun calculateTextWidth(component: Component): Int
    fun getTextHeight(): Int
}

interface WidgetProxy {
    fun addInputSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int, drawBackground: Boolean)
    fun addCatalystSlotWidget(ingredientStack: IngredientStack, x: Int, y: Int, drawBackground: Boolean)
    fun addOutputSlotWidget(itemStack: ItemStack, x: Int, y: Int, drawBackground: Boolean)
    fun addTextWidget(component: Component, x: Int, y: Int, color: ColorPair?, shadow: Boolean, horizontalAlignment: Alignment?)
    fun addArrow(x: Int, y: Int, durationMilliSeconds: Int?)
}

class ColorPair(val lightModeArgb: Int, val darkModeArgb: Int) {
    companion object {
        val DARK_GRAY = ColorPair(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
    }
}

enum class Alignment {
    START, CENTER, END,
}

fun View(block: SingleView<View>.() -> Unit): View = SingleView { block(this) }.childView
