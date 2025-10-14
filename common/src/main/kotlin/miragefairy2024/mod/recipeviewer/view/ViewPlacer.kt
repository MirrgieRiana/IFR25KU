package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.FreezableRegistry
import miragefairy2024.util.set

fun interface ViewPlacer<in V : View> {
    fun place(view: V, bounds: IntRectangle)
}

fun interface ContextViewPlacer<in C, in V : View> {
    fun place(context: C, view: V, bounds: IntRectangle)
}

class ViewPlacerRegistry<C> {
    private val map = FreezableRegistry<Class<out View>, ContextViewPlacer<C, *>>()

    fun <V : View> register(viewClass: Class<V>, factory: ContextViewPlacer<C, V>) {
        map[viewClass] = factory
    }

    fun <V : View> place(context: C, view: V, bounds: IntRectangle) {
        val contextViewPlacer = map.freezeAndGet()[view.javaClass]
        if (contextViewPlacer == null) throw IllegalArgumentException("Unsupported view: $view")
        @Suppress("UNCHECKED_CAST")
        contextViewPlacer as ContextViewPlacer<C, V>
        contextViewPlacer.place(context, view, bounds)
    }
}

inline fun <C, reified V : View> ViewPlacerRegistry<C>.register(factory: ContextViewPlacer<C, V>) = this.register(V::class.java, factory)
