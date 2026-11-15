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

class ObservableValue<T>(defaultValue: T) : EventRegistry<(T, T) -> Unit>() {
    private var updating = false
    private var dirty = false
    private val oldTable = mutableMapOf<(T, T) -> Unit, Single<T>>()
    var value: T = defaultValue
        set(new) {
            val old = field
            field = new
            if (new === old || new == old) return
            if (updating) {
                dirty = true
                listeners.forEach {
                    if (it !in oldTable) {
                        oldTable[it] = Single(old)
                    }
                }
            } else {
                updating = true
                try {
                    listeners.forEach {
                        oldTable[it] = Single(old)
                    }
                    run finish@{
                        while (true) {
                            dirty = false
                            run next@{
                                listeners.toList().forEach { listener ->
                                    if (listener !in listeners) {
                                        oldTable -= listener
                                        return@forEach
                                    }
                                    val old2 = oldTable[listener]!!.first
                                    val new2 = field
                                    if (old2 === new2 || old2 == new2) return@forEach
                                    listener(old2, new2)
                                    oldTable[listener] = Single(new2)
                                    if (dirty) return@next
                                }
                                return@finish
                            }
                        }
                    }
                } finally {
                    updating = false
                    oldTable.clear()
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
