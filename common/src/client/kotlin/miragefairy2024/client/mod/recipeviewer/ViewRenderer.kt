package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.plusAssign
import mirrg.kotlin.helium.Slot
import net.minecraft.client.gui.GuiGraphics

interface ViewRenderer<in V : PlaceableView, S> {
    fun createState(view: V): S
    fun isFocusable(view: V, state: Slot<S>) = false
    fun mouseClicked(view: V, state: Slot<S>, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = false
    fun keyPressed(view: V, state: Slot<S>, bounds: IntRectangle, keyCode: Int, scanCode: Int, modifiers: Int) = false
    fun render(view: V, state: Slot<S>, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, isHovered: Boolean, delta: Float) = Unit
}

object ViewRendererRegistry {
    val registry = SubscribableBuffer<Entry<*, *>>()

    fun <V : PlaceableView, S> register(viewClass: Class<V>, viewRenderer: ViewRenderer<V, S>) {
        registry += Entry(viewClass, viewRenderer)
    }

    class Entry<V : PlaceableView, S>(val viewClass: Class<V>, val viewRenderer: ViewRenderer<V, S>)
}
