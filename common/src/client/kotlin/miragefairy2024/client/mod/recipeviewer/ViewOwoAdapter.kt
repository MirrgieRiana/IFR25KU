package miragefairy2024.client.mod.recipeviewer

import io.wispforest.owo.ui.core.Component
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.util.SubscribableBuffer
import miragefairy2024.util.plusAssign

fun interface ViewOwoAdapter<V : PlaceableView> {
    /**
     * EMIの内部仕様のため、 mouseClicked および keyPressed を除くUIイベントは通知されません。
     */
    fun createOwoComponent(view: V, context: ViewOwoAdapterContext): Component
}

interface ViewOwoAdapterContext {
    fun prepare()
    fun wrap(view: PlaceableView, size: IntPoint): Component
}

object ViewOwoAdapterRegistry {
    val registry = SubscribableBuffer<Entry<*>>()

    fun <V : PlaceableView> register(viewClass: Class<V>, viewOwoAdapter: ViewOwoAdapter<V>) {
        registry += Entry(viewClass, viewOwoAdapter)
    }

    class Entry<V : PlaceableView>(val viewClass: Class<V>, val viewOwoAdapter: ViewOwoAdapter<V>)
}
