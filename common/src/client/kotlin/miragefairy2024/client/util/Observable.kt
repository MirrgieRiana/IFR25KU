package miragefairy2024.client.util

import miragefairy2024.client.util.Observable.Remover

class Observable<T>(defaultValue: T) {

    var value: T = defaultValue
        set(new) {
            val old = field
            field = new
            if (new != old) {
                listeners.toList().forEach {
                    it(old, new)
                }
            }
        }

    val listeners = mutableSetOf<(T, T) -> Unit>()

    interface Remover {
        fun remove()
    }

}

fun <T> Observable<T>.register(listener: (T, T) -> Unit) {
    this.listeners += listener
}

fun <T> Observable<T>.remove(listener: (T, T) -> Unit) {
    this.listeners -= listener
}

@JvmName("observe2")
fun <T> Observable<T>.observe(listener: (T, T) -> Unit): Remover {
    this.register(listener)
    return object : Remover {
        override fun remove() {
            this@observe.remove(listener)
        }
    }
}

@JvmName("observe0")
fun <T> Observable<T>.observe(listener: () -> Unit) = this.observe { _, _ -> listener }

@JvmName("observeAndInitialize2")
fun <T> Observable<T>.observeAndInitialize(listener: (T, T) -> Unit): Remover {
    val remover = this.observe(listener)
    listener(this.value, this.value)
    return remover
}

@JvmName("observeAndInitialize0")
fun <T> Observable<T>.observeAndInitialize(listener: () -> Unit) = this.observeAndInitialize { _, _ -> listener }
