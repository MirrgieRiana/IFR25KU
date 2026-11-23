package miragefairy2024.util

// TODO mirrg

// EventRegistry

open class EventRegistry<L> {
    private val listeners = mutableSetOf<L>()

    open fun add(listener: L): Boolean = listeners.add(listener)
    open fun remove(listener: L): Boolean = listeners.remove(listener)
    fun toList(): List<L> = listeners.toList()
}

fun <L> EventRegistry<L>.register(listener: L): Remover {
    this.add(listener)
    return Remover {
        this.remove(listener)
    }
}

fun <L> EventRegistry<L>.observe(onClosed: EventRegistry<() -> Unit>, listener: L) {
    this.register(listener).removeOn(onClosed)
}

inline fun <L> EventRegistry<L>.fire(invoker: (L) -> Unit) {
    toList().forEach {
        invoker(it)
    }
}

fun <E> EventRegistry<(E) -> Unit>.emit(event: E) = this.fire { it(event) }
fun EventRegistry<() -> Unit>.fire() = this.fire { it() }


// ObservableValue

/** 互いに矛盾するようなバリデータが相互に値を修正し合うパターンは、想定しないのだ～🌱 */
class ObservableValue<T>(defaultValue: T) : EventRegistry<(T, T) -> Unit>() {
    private class ListenerState<T>(val value: T, val revision: Long)

    private var updating = false
    private var revision = 0L
    private val listenerStateTable = mutableMapOf<(T, T) -> Unit, ListenerState<T>>()

    override fun add(listener: (T, T) -> Unit): Boolean {
        val added = super.add(listener)
        if (added && updating) listenerStateTable[listener] = ListenerState(value, revision)
        return added
    }

    override fun remove(listener: (T, T) -> Unit): Boolean {
        val removed = super.remove(listener)
        if (removed && updating) listenerStateTable -= listener
        return removed
    }

    var value: T = defaultValue
        set(new) {
            val old = field
            field = new
            if (new === old || new == old) return
            val oldRevision = revision
            revision++
            if (!updating) {
                updating = true
                try {
                    // リスナーのステートの初期化
                    toList().forEach {
                        listenerStateTable[it] = ListenerState(old, oldRevision)
                    }

                    run finish@{
                        while (true) {
                            run next@{
                                // 各リスナーにイベントを配る
                                val oldRevision2 = revision
                                toList().forEach { listener ->
                                    val listenerState = listenerStateTable[listener] ?: return@forEach // このリスナーは登録解除済み
                                    if (listenerState.revision == oldRevision2) return@forEach // このリスナーには既に通知済み

                                    // 通知
                                    val old2 = field
                                    listenerStateTable[listener] = ListenerState(old2, oldRevision2)
                                    listener(listenerState.value, old2)

                                    if (revision != oldRevision2) return@next // 通知中に値が代入されたためイベントの配布をやり直す
                                }

                                // リビジョンが更新されなかったので無事通知完了
                                return@finish
                            }
                        }
                    }
                } finally {
                    updating = false
                    listenerStateTable.clear()
                }
            }
        }
}

fun <T> ObservableValue<T>.registerAndInitialize(listener: (T, T) -> Unit): Remover {
    val remover = this.register(listener)
    listener(this.value, this.value)
    return remover
}

fun <T> ObservableValue<T>.observeAndInitialize(onClosed: EventRegistry<() -> Unit>, listener: (T, T) -> Unit) {
    this.observe(onClosed, listener)
    listener(this.value, this.value)
}


// Remover

fun interface Remover {
    companion object {
        val NONE = Remover { }
    }

    fun remove()
}

fun Remover.removeOn(onClosed: EventRegistry<() -> Unit>) {
    var onClosedListener: (() -> Unit)? = null
    onClosedListener = {
        onClosed.remove(onClosedListener!!)
        this.remove()
    }
    onClosed.register(onClosedListener)
}

fun (() -> Unit).asRemover() = Remover {
    this()
}

operator fun Remover.plus(other: Remover) = Remover {
    this.remove()
    other.remove()
}

fun Iterable<Remover>.flatten() = Remover {
    this.forEach {
        it.remove()
    }
}
