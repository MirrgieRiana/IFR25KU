package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.mod.recipeviewer.view.View
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.plusAssign
import net.minecraft.client.gui.GuiGraphics

fun interface ViewRenderer<in V : View> {
    fun render(view: V, x: Int, y: Int, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float)
}

object ViewRendererRegistry {
    val registry = SubscribableBuffer<Entry<*>>()

    fun <V : View> register(viewClass: Class<V>, viewRenderer: ViewRenderer<V>) {
        registry += Entry(viewClass, viewRenderer)
    }

    class Entry<V : View>(val viewClass: Class<V>, val viewRenderer: ViewRenderer<V>)
}
