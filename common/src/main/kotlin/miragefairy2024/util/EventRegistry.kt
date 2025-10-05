package miragefairy2024.util


// EventRegistry

open class EventRegistry<L> {
    val listeners = mutableSetOf<L>()
}

fun <L> EventRegistry<L>.register(listener: L) {
    this.listeners += listener
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
