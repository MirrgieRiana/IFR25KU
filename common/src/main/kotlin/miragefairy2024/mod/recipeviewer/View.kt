package miragefairy2024.mod.recipeviewer

import net.minecraft.network.chat.Component

interface View {
    fun layout(rendererProxy: RendererProxy)
    fun getWidth(): Int
    fun getHeight(): Int
    fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer)
}

interface RendererProxy {
    fun calculateTextWidth(component: Component): Int
    fun getTextHeight(): Int
}

interface ViewPlacer {
    fun addInputSlotView(view: InputSlotView, x: Int, y: Int)
    fun addCatalystSlotView(view: CatalystSlotView, x: Int, y: Int)
    fun addOutputSlotView(view: OutputSlotView, x: Int, y: Int)
    fun addTextView(view: TextView, x: Int, y: Int)
    fun addArrowView(view: ArrowView, x: Int, y: Int)
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
