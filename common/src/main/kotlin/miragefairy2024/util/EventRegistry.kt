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
    private inner class Subscription(val listener: (T, T) -> Unit, var oldValue: T) : Remover {
        override fun remove() {
            subscriptions -= this
        }

        fun notifyInitial() {
            val current = oldValue
            listener(current, current)
        }
    }

    private val subscriptions = mutableSetOf<Subscription>()
    private var notifying = false
    private var dirty = false

    var value: T = defaultValue
        set(new) {
            if (new === field || new == field) return
            field = new
            dirty = true
            if (!notifying) notifyListeners(null)
        }

    /** 呼び出しごとに購読を追加し、その購読だけを解除する [miragefairy2024.util.Remover] を返すのだ～🌱 */
    fun register(listener: (T, T) -> Unit): Remover = addSubscription(listener)

    fun registerAndInitialize(listener: (T, T) -> Unit): Remover {
        val subscription = addSubscription(listener)
        initialize(subscription)
        return subscription
    }

    fun observeAndInitialize(onClosed: EventRegistry<() -> Unit>, listener: (T, T) -> Unit) {
        val subscription = addSubscription(listener)
        subscription.removeOn(onClosed)
        initialize(subscription)
    }

    private fun addSubscription(listener: (T, T) -> Unit): Subscription {
        val subscription = Subscription(listener, value)
        subscriptions += subscription
        return subscription
    }

    private fun initialize(subscription: Subscription) {
        if (notifying) {
            subscription.notifyInitial()
        } else {
            notifyListeners(subscription)
        }
    }

    private fun notifyListeners(initialSubscription: Subscription?) {
        notifying = true
        try {
            initialSubscription?.notifyInitial()
            while (dirty) {
                dirty = false
                val new = value
                for (subscription in subscriptions.toList()) {
                    if (subscription !in subscriptions) continue
                    val old = subscription.oldValue
                    if (new === old || new == old) continue
                    subscription.oldValue = new
                    subscription.listener(old, new)
                    if (dirty) break
                }
            }
        } finally {
            dirty = false
            notifying = false
        }
    }
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
