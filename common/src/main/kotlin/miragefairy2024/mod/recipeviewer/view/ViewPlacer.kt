package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.FreezableRegistry
import miragefairy2024.util.set

interface PlaceableView : View

fun interface ViewPlacer<in V : PlaceableView> {
    fun place(view: V, bounds: IntRectangle)
}

fun interface ContextViewPlacer<in C, in V : PlaceableView> {
    fun place(context: C, view: V, bounds: IntRectangle)
}

class ViewPlacerRegistry<C> {
    private val map = FreezableRegistry<Class<out PlaceableView>, ContextViewPlacer<C, *>>()

    fun <V : PlaceableView> register(viewClass: Class<V>, factory: ContextViewPlacer<C, V>) {
        map[viewClass] = factory
    }

    fun <V : PlaceableView> place(context: C, view: V, bounds: IntRectangle) {
        val contextViewPlacer = map.freezeAndGet()[view.javaClass]
        if (contextViewPlacer == null) throw IllegalArgumentException("Unsupported view: $view")
        @Suppress("UNCHECKED_CAST")
        contextViewPlacer as ContextViewPlacer<C, V>
        contextViewPlacer.place(context, view, bounds)
    }
}

inline fun <C, reified V : PlaceableView> ViewPlacerRegistry<C>.register(factory: ContextViewPlacer<C, V>) = this.register(V::class.java, factory)
