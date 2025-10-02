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

    override fun layout() {
        children.forEach {
            it.view.layout()
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

operator fun <P, V : View> ContainerView<P, V>.set(position: P, view: V) = this.add(position, view)
operator fun <V : View> ContainerView<Unit, V>.plusAssign(view: V) = this.add(Unit, view)
context(ContainerView<P, V>) infix fun <P, V : View> P.has(view: V) = this@ContainerView.add(this, view)


class SingleView<V : View> : ContainerView<Unit, V>() {
    override fun calculateWidth() = children.single().view.getWidth()
    override fun calculateHeight() = children.single().view.getHeight()
    override fun layout() {
        super.layout()
        children.single().xCache = 0
        children.single().yCache = 0
    }

    val childView get() = children.single().view
}

context(ViewScope) fun SingleView(block: SingleView<View>.() -> Unit) = SingleView<View>().apply { block() }


class XListView<V : View> : ContainerView<Unit, V>() {
    override fun calculateWidth() = children.sumOf { it.view.getWidth() }
    override fun calculateHeight() = children.maxOfOrNull { it.view.getHeight() } ?: 0
    override fun layout() {
        super.layout()
        var x = 0
        children.forEach {
            it.xCache = x
            it.yCache = 0
            x += it.view.getWidth()
        }
    }
}

context(ViewScope) fun XListView(block: XListView<View>.() -> Unit) = XListView<View>().apply { block() }


class YListView<V : View> : ContainerView<Unit, V>() {
    override fun calculateWidth() = children.maxOfOrNull { it.view.getWidth() } ?: 0
    override fun calculateHeight() = children.sumOf { it.view.getHeight() }
    override fun layout() {
        super.layout()
        var y = 0
        children.forEach {
            it.xCache = 0
            it.yCache = y
            y += it.view.getHeight()
        }
    }
}

context(ViewScope) fun YListView(block: YListView<View>.() -> Unit) = YListView<View>().apply { block() }


abstract class SolidView(private val width: Int, private val height: Int) : View {
    override fun layout() = Unit
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
