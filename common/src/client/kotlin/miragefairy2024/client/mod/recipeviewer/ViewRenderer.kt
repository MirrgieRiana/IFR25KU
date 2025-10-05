package miragefairy2024.client.mod.recipeviewer

import miragefairy2024.ModContext
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.util.FreezableRegistry
import miragefairy2024.util.set
import net.minecraft.client.gui.GuiGraphics

fun interface ViewRenderer<in V : View> {
    fun render(view: V, x: Int, y: Int, graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float)
}

object ViewRendererRegistry {
    private val map = FreezableRegistry<Class<out View>, ViewRenderer<*>>()

    context(ModContext)
    fun <V : View> register(viewClass: Class<V>, renderer: ViewRenderer<V>) {
        map[viewClass] = renderer
    }

    fun <V : View> get(viewClass: Class<V>): ViewRenderer<V> {
        @Suppress("UNCHECKED_CAST")
        return map.freezeAndGet()[viewClass] as ViewRenderer<V>
    }

    class Entry<V : View>(val viewClass: Class<V>, val renderer: ViewRenderer<V>)

    fun entries() = map.freezeAndGet().keys.map {
        fun <V : View> getEntry(viewClass: Class<V>) = Entry(viewClass, get(viewClass))
        getEntry(it as Class<View>)
    }
}
