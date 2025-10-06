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

data class IntPoint(val x: Int, val y: Int)

fun IntPoint.offset(dx: Int, dy: Int) = IntPoint(x + dx, y + dy)
operator fun IntPoint.unaryMinus() = IntPoint(-x, -y)
operator fun IntPoint.plus(other: IntPoint) = IntPoint(x + other.x, y + other.y)
operator fun IntPoint.minus(other: IntPoint) = IntPoint(x - other.x, y - other.y)

data class IntRectangle(val x: Int, val y: Int, val width: Int, val height: Int)

val IntRectangle.topLeft get() = IntPoint(x, y)
val IntRectangle.topRight get() = IntPoint(x + width, y)
val IntRectangle.bottomLeft get() = IntPoint(x, y + height)
val IntRectangle.bottomRight get() = IntPoint(x + width, y + height)
fun IntRectangle.offset(dx: Int, dy: Int) = IntRectangle(x + dx, y + dy, width, height)
fun IntRectangle.grow(d: Int) = this.grow(d, d)
fun IntRectangle.grow(dw: Int, dh: Int) = IntRectangle(x - dw, y - dh, width + dw * 2, height + dh * 2)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, width, height)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, width, height)

fun View(block: SingleView<View>.() -> Unit): View = SingleView { block(this) }.childView
