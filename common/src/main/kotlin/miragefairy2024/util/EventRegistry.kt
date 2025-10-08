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
    var onClosedListener: (() -> Unit)? = null
    onClosedListener = {
        onClosed.remove(onClosedListener!!)
        this.remove(listener)
    }
    this.register(listener)
    onClosed.register(onClosedListener)
}

fun <L> EventRegistry<L>.fire(invoker: (L) -> Unit) {
    listeners.toList().forEach {
        invoker(it)
    }
}

fun <E> EventRegistry<(E) -> Unit>.emit(event: E) = this.fire { it(event) }
fun EventRegistry<() -> Unit>.fire() = this.fire { it() }


// ObservableValue

class ObservableValue<T>(defaultValue: T) : EventRegistry<(T, T) -> Unit>() {
    var value: T = defaultValue
        set(new) {
            val old = field
            field = new
            if (new != old) {
                fire { it(old, new) }
            }
        }
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
