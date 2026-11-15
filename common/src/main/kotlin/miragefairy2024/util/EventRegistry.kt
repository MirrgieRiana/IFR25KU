package miragefairy2024.util


// TODO mirrg

// EventRegistry

open class EventRegistry<L> {
    val listeners = mutableSetOf<L>()
}

fun <L> EventRegistry<L>.register(listener: L): Remover {
    this.listeners += listener
    return Remover {
        this.listeners -= listener
    }
}

fun <L> EventRegistry<L>.remove(listener: L) {
    this.listeners -= listener
}

fun <L> EventRegistry<L>.observe(onClosed: EventRegistry<() -> Unit>, listener: L) {
    this.register(listener).removeOn(onClosed)
}

inline fun <L> EventRegistry<L>.fire(invoker: (L) -> Unit) {
    listeners.toList().forEach {
        invoker(it)
    }
}

fun <E> EventRegistry<(E) -> Unit>.emit(event: E) = this.fire { it(event) }
fun EventRegistry<() -> Unit>.fire() = this.fire { it() }


// ObservableValue

class ObservableValue<T>(defaultValue: T) : EventRegistry<(T, T) -> Unit>() {
    private var updating = false
    private var dirty = false
    var value: T = defaultValue
        set(new) {
            val old = field
            field = new
            if (new !== old && new != old) {
                if (updating) {
                    dirty = true
                } else {
                    updating = true
                    try {
                        data class Entry(val listener: (T, T) -> Unit, var old: T)

                        val entries = listeners.map { Entry(it, old) }
                        run finish@{
                            while (true) {
                                dirty = false
                                run next@{
                                    entries.forEach {
                                        val new2 = field
                                        if (it.old !== new2 && it.old != new2) {
                                            it.listener(it.old, new2)
                                            it.old = new2
                                            if (dirty) return@next
                                        }
                                    }
                                    return@finish
                                }
                            }
                        }
                    } finally {
                        updating = false
                    }
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
