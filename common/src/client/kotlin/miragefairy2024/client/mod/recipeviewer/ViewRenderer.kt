package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.plusAssign
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics

/**
 * Widget の各辺から外向きに測った非負のはみ出し量なのだ～🌱
 * `0` を指定した辺からは描画がはみ出さないのだ～🌱
 * 各辺に [kotlin.Int.MAX_VALUE] を指定すると、その方向のはみ出し量に上限がないことを表すのだ～🌱
 */
data class WidgetRenderOverflow(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int) {
    init {
        require(xMin >= 0 && xMax >= 0 && yMin >= 0 && yMax >= 0) { "Widget render outsets must be non-negative" }
    }

    companion object {
        val NONE = WidgetRenderOverflow(0, 0, 0, 0)
        val UNBOUNDED = WidgetRenderOverflow(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }
}

interface ViewRenderer<in V : PlaceableView, S> {
    fun createState(view: V): S
    fun isFocusable(view: V, state: Slot<S>) = false
    fun mouseClicked(view: V, state: Slot<S>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = false
    fun keyPressed(view: V, state: Slot<S>, bounds: IntRectangle, keyCode: Int, scanCode: Int, modifiers: Int) = false
    fun getRenderOverflow(view: V, bounds: IntRectangle) = WidgetRenderOverflow.NONE
    fun render(view: V, state: Slot<S>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) = Unit
}

object ViewRendererRegistry {
    val registry = SubscribableBuffer<Entry<*, *>>()

    fun <V : PlaceableView, S> register(viewClass: Class<V>, viewRenderer: ViewRenderer<V, S>) {
        registry += Entry(viewClass, viewRenderer)
    }

    class Entry<V : PlaceableView, S>(val viewClass: Class<V>, val viewRenderer: ViewRenderer<V, S>)
}
