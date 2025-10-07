package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.FreezableRegistry
import miragefairy2024.util.set
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

interface View {
    fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize
}

interface ViewWithMinSize {
    val minSize: IntPoint
    fun calculateSize(maxSize: IntPoint): ViewWithSize
}

interface ViewWithSize {
    val size: IntPoint
    fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<View>)
}

interface RendererProxy {
    fun calculateTextWidth(text: FormattedText): Int
    fun getTextHeight(): Int
    fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence>
}

fun interface ViewPlacer<in V : View> {
    fun place(view: V, bounds: IntRectangle): Remover
}

fun interface ContextViewPlacer<in C, in V : View> {
    fun place(context: C, view: V, bounds: IntRectangle): Remover
}

fun interface Remover {
    fun remove()
}

class ViewPlacerRegistry<C> {
    private val map = FreezableRegistry<Class<out View>, ContextViewPlacer<C, *>>()

    fun <V : View> register(viewClass: Class<V>, factory: ContextViewPlacer<C, V>) {
        map[viewClass] = factory
    }

    fun <V : View> place(context: C, view: V, bounds: IntRectangle): Remover {
        val contextViewPlacer = map.freezeAndGet()[view.javaClass]
        if (contextViewPlacer == null) throw IllegalArgumentException("Unsupported view: $view")
        @Suppress("UNCHECKED_CAST")
        contextViewPlacer as ContextViewPlacer<C, V>
        return contextViewPlacer.place(context, view, bounds)
    }
}

inline fun <C, reified V : View> ViewPlacerRegistry<C>.register(factory: ContextViewPlacer<C, V>) = this.register(V::class.java, factory)

class ColorPair(val lightModeArgb: Int, val darkModeArgb: Int) {
    companion object {
        val DARK_GRAY = ColorPair(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
    }
}

enum class Alignment {
    START, CENTER, END,
}

data class IntPoint(val x: Int, val y: Int) {
    companion object {
        val ZERO = IntPoint(0, 0)
    }
}

operator fun IntPoint.unaryMinus() = IntPoint(-x, -y)
fun IntPoint.plus(dx: Int, dy: Int) = IntPoint(x + dx, y + dy)
fun IntPoint.offset(dx: Int, dy: Int) = this.plus(dx, dy)
fun IntPoint.minus(dx: Int, dy: Int) = this.plus(-dx, -dy)
operator fun IntPoint.plus(other: IntPoint) = this.plus(other.x, other.y)
operator fun IntPoint.minus(other: IntPoint) = this.minus(other.x, other.y)
infix fun IntPoint.max(other: IntPoint) = IntPoint(x max other.x, y max other.y)
infix fun IntPoint.min(other: IntPoint) = IntPoint(x min other.x, y min other.y)
fun IntPoint.sized(size: IntPoint) = IntRectangle(x, y, size.x, size.y)

data class IntRectangle(val x: Int, val y: Int, val xSize: Int, val ySize: Int) {
    companion object {
        val ZERO = IntRectangle(0, 0, 0, 0)
    }
}

val IntRectangle.offset get() = IntPoint(x, y)
val IntRectangle.size get() = IntPoint(xSize, ySize)
fun IntRectangle.offset(dx: Int, dy: Int) = IntRectangle(x + dx, y + dy, xSize, ySize)
fun IntRectangle.grow(d: Int) = this.grow(d, d)
fun IntRectangle.grow(dx: Int, dy: Int) = this.grow(dx, dx, dy, dy)
fun IntRectangle.grow(dxMin: Int, dxMax: Int, dyMin: Int, dyMax: Int) = IntRectangle(x - dxMin, y - dyMin, xSize + dxMin + dxMax, ySize + dyMin + dyMax)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, xSize, ySize)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, xSize, ySize)

fun View(block: Child<Unit, SingleView>.() -> Unit): View = SingleView().apply { block(Child(Unit, this)) }.childView
