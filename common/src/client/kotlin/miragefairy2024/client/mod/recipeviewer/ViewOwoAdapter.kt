package miragefairy2024.client.mod.recipeviewer

import io.wispforest.owo.ui.core.Component
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.plusAssign

fun interface ViewOwoAdapter<V : View> {
    fun createOwoComponent(view: V, cotext: ViewOwoAdapterContext): Component
}

interface ViewOwoAdapterContext {
    fun prepare()
    fun wrap(view: View): Component
}

object ViewOwoAdapterRegistry {
    val registry = SubscribableBuffer<Entry<*>>()

    fun <V : View> register(viewClass: Class<V>, viewOwoAdapter: ViewOwoAdapter<V>) {
        registry += Entry(viewClass, viewOwoAdapter)
    }

    class Entry<V : View>(val viewClass: Class<V>, val viewOwoAdapter: ViewOwoAdapter<V>)
}



