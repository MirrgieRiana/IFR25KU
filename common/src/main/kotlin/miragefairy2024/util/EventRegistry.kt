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

/** リスナーごとに最後に通知した値を記録し、通知中の値変更は現在の通知が戻ってから登録順に配り直すのだ～🌱 */
class ObservableValue<T>(defaultValue: T) {
    private class Subscription<T>(val listener: (T, T) -> Unit, var oldValue: T)

    private val subscriptions = linkedMapOf<(T, T) -> Unit, Subscription<T>>()
    val listeners: Set<(T, T) -> Unit> get() = subscriptions.keys.toSet()

    private var notifying = false
    private var pending = false

    var value: T = defaultValue
        set(new) {
            val old = field
            field = new
            if (new == old) return
            pending = true
            if (!notifying) notifyListeners()
        }

    fun register(listener: (T, T) -> Unit): Remover {
        val subscription = subscriptions.getOrPut(listener) { Subscription(listener, value) }
        return Remover {
            if (subscriptions[listener] === subscription) remove(listener)
        }
    }

    fun remove(listener: (T, T) -> Unit) {
        subscriptions.remove(listener)
    }

    internal fun initialize(listener: (T, T) -> Unit) {
        val current = value
        subscriptions[listener]?.oldValue = current
        if (notifying) {
            listener(current, current)
        } else {
            notifyListeners(listener)
        }
    }

    private fun notifyListeners(initialListener: ((T, T) -> Unit)? = null) {
        notifying = true
        try {
            val current = value
            initialListener?.invoke(current, current)
            while (pending) {
                pending = false
                val new = value
                for (subscription in subscriptions.values.toList()) {
                    if (subscriptions[subscription.listener] !== subscription) continue
                    val old = subscription.oldValue
                    if (old == new) continue
                    subscription.oldValue = new
                    subscription.listener(old, new)
                    if (pending) break
                }
            }
        } finally {
            pending = false
            notifying = false
        }
    }
}

fun <T> ObservableValue<T>.observe(onClosed: EventRegistry<() -> Unit>, listener: (T, T) -> Unit) {
    this.register(listener).removeOn(onClosed)
}

fun <T> ObservableValue<T>.registerAndInitialize(listener: (T, T) -> Unit): Remover {
    val remover = this.register(listener)
    this.initialize(listener)
    return remover
}

fun <T> ObservableValue<T>.observeAndInitialize(onClosed: EventRegistry<() -> Unit>, listener: (T, T) -> Unit) {
    this.observe(onClosed, listener)
    this.initialize(listener)
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
