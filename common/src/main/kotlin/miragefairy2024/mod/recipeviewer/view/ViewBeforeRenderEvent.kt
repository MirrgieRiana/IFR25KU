package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.Remover

/** 1 個の描画ルートに配置された View へ、描画前に通知するのだ～🌱 */
class ViewBeforeRenderEvent {
    private val listeners = mutableSetOf<() -> Unit>()
    private var pendingListeners: ArrayDeque<() -> Unit>? = null

    fun register(listener: () -> Unit): Remover {
        val callback = { listener() }
        listeners += callback
        pendingListeners?.addLast(callback)
        return Remover { listeners -= callback }
    }

    fun fire() {
        if (listeners.isEmpty()) return
        check(pendingListeners == null)
        val pending = ArrayDeque(listeners)
        pendingListeners = pending
        try {
            // 通知中に配置された View も、この描画の前に通知するのだ～🌱
            while (pending.isNotEmpty()) {
                val callback = pending.removeFirst()
                if (callback in listeners) callback()
            }
        } finally {
            pendingListeners = null
        }
    }
}
