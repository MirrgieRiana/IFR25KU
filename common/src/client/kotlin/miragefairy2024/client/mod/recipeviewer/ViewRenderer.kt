package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.plusAssign
import net.minecraft.client.gui.GuiGraphics

interface ViewRenderer<in V : PlaceableView> {
    fun mouseClicked(view: V, bounds: IntRectangle, mouseX: Int, mouseY: Int, button: Int) = false
    fun keyPressed(view: V, bounds: IntRectangle, keyCode: Int, scanCode: Int, modifiers: Int) = false
    fun render(view: V, bounds: IntRectangle, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) = Unit
}

object ViewRendererRegistry {
    val registry = SubscribableBuffer<Entry<*>>()

    fun <V : PlaceableView> register(viewClass: Class<V>, viewRenderer: ViewRenderer<V>) {
        registry += Entry(viewClass, viewRenderer)
    }

    class Entry<V : PlaceableView>(val viewClass: Class<V>, val viewRenderer: ViewRenderer<V>)
}
