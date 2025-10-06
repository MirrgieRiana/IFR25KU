@file:Suppress("FunctionName")

package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.IngredientStack
import mirrg.kotlin.helium.atLeast
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack


abstract class ContainerView<P, V : View> : View {

    val children = mutableListOf<PositionedView>()

    inner class PositionedView(val position: P, val view: V) {
        var xCache = 0
        var yCache = 0
    }

    fun add(position: P, view: V) {
        children += PositionedView(position, view)
    }

    private var minWidthCache = 0
    private var minHeightCache = 0
    private var widthCache = 0
    private var heightCache = 0

    override fun layout(rendererProxy: RendererProxy) {
        children.forEach {
            it.view.layout(rendererProxy)
        }
        minWidthCache = calculateMinWidth()
        minHeightCache = calculateMinHeight()
        widthCache = calculateWidth()
        heightCache = calculateHeight()
    }

    override fun getMinWidth() = minWidthCache
    override fun getMinHeight() = minHeightCache
    override fun getWidth() = widthCache
    override fun getHeight() = heightCache

    abstract fun calculateMinWidth(): Int
    abstract fun calculateMinHeight(): Int
    abstract fun calculateWidth(): Int
    abstract fun calculateHeight(): Int

    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>) {
        children.forEach {
            it.view.assemble(x + it.xCache, y + it.yCache, viewPlacer)
        }
    }

}

interface DefaultedContainerView<V : View> {
    fun add(view: V)
}

operator fun <V : View> DefaultedContainerView<V>.plusAssign(view: V) = this.add(view)
operator fun <P, V : View> ContainerView<P, V>.plusAssign(pair: Pair<P, V>) = this.add(pair.first, pair.second)


class SingleView<V : View> : ContainerView<Unit, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(Unit, view)
    override fun calculateMinWidth() = children.single().view.getMinWidth()
    override fun calculateMinHeight() = children.single().view.getMinHeight()
    override fun calculateWidth() = children.single().view.getWidth()
    override fun calculateHeight() = children.single().view.getHeight()
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        children.single().xCache = 0
        children.single().yCache = 0
    }

    val childView get() = children.single().view
}

fun SingleView(block: SingleView<View>.() -> Unit) = SingleView<View>().apply { block() }


class AbsoluteView<V : View>(private val width: Int, private val height: Int) : ContainerView<IntPoint, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(IntPoint(0, 0), view)
    override fun calculateMinWidth() = width
    override fun calculateMinHeight() = height
    override fun calculateWidth() = width
    override fun calculateHeight() = height
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        children.forEach {
            it.xCache = it.position.x
            it.yCache = it.position.y
        }
    }
}

fun AbsoluteView(width: Int, height: Int, block: AbsoluteView<View>.() -> Unit) = AbsoluteView<View>(width, height).apply { block() }


abstract class ListView<V : View> : ContainerView<Alignment, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(Alignment.START, view)
}

class XListView<V : View> : ListView<V>() {
    var minHeight = 0
    override fun calculateMinWidth() = children.sumOf { it.view.getMinWidth() }
    override fun calculateMinHeight() = (children.maxOfOrNull { it.view.getMinHeight() } ?: 0) atLeast minHeight
    override fun calculateWidth() = children.sumOf { it.view.getWidth() }
    override fun calculateHeight() = (children.maxOfOrNull { it.view.getHeight() } ?: 0) atLeast minHeight
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        var x = 0
        children.forEach {
            it.xCache = x
            it.yCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (getHeight() - it.view.getHeight()) / 2
                Alignment.END -> getHeight() - it.view.getHeight()
            }
            x += it.view.getWidth()
        }
    }
}

fun XListView(block: XListView<View>.() -> Unit) = XListView<View>().apply { block() }

class YListView<V : View> : ListView<V>() {
    var minWidth = 0
    override fun calculateMinWidth() = (children.maxOfOrNull { it.view.getMinWidth() } ?: 0) atLeast minWidth
    override fun calculateMinHeight() = children.sumOf { it.view.getMinHeight() }
    override fun calculateWidth() = (children.maxOfOrNull { it.view.getWidth() } ?: 0) atLeast minWidth
    override fun calculateHeight() = children.sumOf { it.view.getHeight() }
    override fun layout(rendererProxy: RendererProxy) {
        super.layout(rendererProxy)
        var y = 0
        children.forEach {
            it.xCache = when (it.position) {
                Alignment.START -> 0
                Alignment.CENTER -> (getWidth() - it.view.getWidth()) / 2
                Alignment.END -> getWidth() - it.view.getWidth()
            }
            it.yCache = y
            y += it.view.getHeight()
        }
    }
}

fun YListView(block: YListView<View>.() -> Unit) = YListView<View>().apply { block() }


abstract class SolidView(private val width: Int, private val height: Int) : View {
    override fun layout(rendererProxy: RendererProxy) = Unit
    override fun getMinWidth() = width
    override fun getMinHeight() = height
    override fun getWidth() = width
    override fun getHeight() = height
    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>) = viewPlacer.place(this, x, y)
}


abstract class SpaceView(width: Int, height: Int) : SolidView(width, height) {
    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>) = Unit
}

class XSpaceView(width: Int) : SpaceView(width, 0)
class YSpaceView(height: Int) : SpaceView(0, height)


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


class TextView(val text: Component) : View {
    var minWidth = 0
    private var widthCache = 0
    private var heightCache = 0

    override fun layout(rendererProxy: RendererProxy) {
        widthCache = rendererProxy.calculateTextWidth(text) atLeast minWidth
        heightCache = rendererProxy.getTextHeight()
    }

    override fun getMinWidth() = minWidth
    override fun getMinHeight() = heightCache
    override fun getWidth() = widthCache
    override fun getHeight() = heightCache

    var color: ColorPair? = null
    var shadow = true
    var horizontalAlignment: Alignment? = null
    var tooltip: List<Component>? = null

    override fun assemble(x: Int, y: Int, viewPlacer: ViewPlacer<View>) = viewPlacer.place(this, x, y)
}


class ImageView(val textureId: ResourceLocation, val bound: IntRectangle) : SolidView(bound.width, bound.height)


class ArrowView() : SolidView(24, 17) {
    var durationMilliSeconds: Int? = null
}
