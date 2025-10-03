@file:Suppress("FunctionName")

package miragefairy2024.mod.recipeviewer

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient


abstract class ContainerView<P, V : View> : View {

    val children = mutableListOf<PositionedView>()

    inner class PositionedView(val position: P, val view: V) {
        var xCache = 0
        var yCache = 0
    }

    fun add(position: P, view: V) {
        children += PositionedView(position, view)
    }

    private var widthCache = 0
    private var heightCache = 0

    override fun layout(rendererProxy: RendererProxy) {
        children.forEach {
            it.view.layout(rendererProxy)
        }
        widthCache = calculateWidth()
        heightCache = calculateHeight()
    }

    override fun getWidth() = widthCache
    override fun getHeight() = heightCache

    abstract fun calculateWidth(): Int
    abstract fun calculateHeight(): Int

    override fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int) {
        children.forEach {
            it.view.addWidgets(widgetProxy, x + it.xCache, y + it.yCache)
        }
    }

}

interface DefaultedContainerView<V : View> {
    fun add(view: V)
}

operator fun <P, V : View> ContainerView<P, V>.set(position: P, view: V) = this.add(position, view)
operator fun <V : View> DefaultedContainerView<V>.plusAssign(view: V) = this.add(view)
operator fun <P, V : View> ContainerView<P, V>.plusAssign(pair: Pair<P, V>) = this.add(pair.first, pair.second)


class SingleView<V : View> : ContainerView<Unit, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(Unit, view)
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


abstract class ListView<V : View> : ContainerView<Alignment, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(Alignment.START, view)
}

class XListView<V : View> : ListView<V>() {
    override fun calculateWidth() = children.sumOf { it.view.getWidth() }
    override fun calculateHeight() = children.maxOfOrNull { it.view.getHeight() } ?: 0
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
    override fun calculateWidth() = children.maxOfOrNull { it.view.getWidth() } ?: 0
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
    override fun getWidth() = width
    override fun getHeight() = height
}


class XSpaceView(width: Int) : SolidView(width, 0) {
    override fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int) = Unit
}

class YSpaceView(height: Int) : SolidView(0, height) {
    override fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int) = Unit
}


abstract class SlotView : SolidView(18, 18)

class InputSlotView(private val ingredient: Ingredient) : SlotView() {
    override fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int) {
        widgetProxy.addInputSlotWidget(ingredient, x, y)
    }
}

class CatalystSlotView(private val ingredient: Ingredient) : SlotView() {
    override fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int) {
        widgetProxy.addCatalystSlotWidget(ingredient, x, y)
    }
}

class OutputSlotView(private val itemStack: ItemStack) : SlotView() {
    override fun addWidgets(widgetProxy: WidgetProxy, x: Int, y: Int) {
        widgetProxy.addOutputSlotWidget(itemStack, x, y)
    }
}
