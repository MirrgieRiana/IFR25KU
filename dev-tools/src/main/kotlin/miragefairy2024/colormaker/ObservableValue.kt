package miragefairy2024.colormaker

import java.awt.Component

class ObservableValue<T>(initialValue: T) {

    var modifying = false
        private set

    private var value = initialValue

    fun get() = value

    fun set(newValue: T, source: Component? = null) {
        if (modifying) throw IllegalStateException("Cannot modify value while modifying.")
        if (value == newValue) return
        val oldValue = value
        value = newValue
        modifying = true
        listeners.forEach {
            it.onChange(oldValue, newValue, source)
        }
        modifying = false
    }

    fun fire(source: Component? = null) {
        modifying = true
        listeners.forEach {
            it.onChange(value, value, source)
        }
        modifying = false
    }


    fun interface Listener<T> {
        fun onChange(oldValue: T, newValue: T, source: Component?)
    }

    private val listeners = mutableListOf<Listener<T>>()

    fun register(listener: Listener<T>) {
        listeners.add(listener)
    }

}
