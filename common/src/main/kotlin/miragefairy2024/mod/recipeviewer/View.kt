package miragefairy2024.mod.recipeviewer

import net.minecraft.network.chat.Component

interface View {
    fun layout(rendererProxy: RendererProxy)
    fun getWidth(): Int
    fun getHeight(): Int
    fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>)
}

interface RendererProxy {
    fun calculateTextWidth(component: Component): Int
    fun getTextHeight(): Int
}

fun interface ViewPlacer<in V : View> {
    fun place(view: V, x: Int, y: Int)
}

fun interface ContextViewPlacer<in C, in V : View> {
    fun place(context: C, view: V, x: Int, y: Int)
}

class ViewPlacerRegistry<C> {
    val map = mutableMapOf<Class<out View>, ContextViewPlacer<C, *>>()
}

inline fun <C, reified V : View> ViewPlacerRegistry<C>.register(factory: ContextViewPlacer<C, V>) {
    this.map[V::class.java] = factory
}

fun <C, V : View> ViewPlacerRegistry<C>.place(context: C, view: V, x: Int, y: Int) {
    val contextViewPlacer = this.map[view.javaClass]
    if (contextViewPlacer == null) throw IllegalArgumentException("Unsupported view: $view")
    @Suppress("UNCHECKED_CAST")
    contextViewPlacer as ContextViewPlacer<C, V>
    contextViewPlacer.place(context, view, x, y)
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
