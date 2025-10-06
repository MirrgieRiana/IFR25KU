package miragefairy2024.util

// TODO mirrg

class FreezableList<T> {
    private val list = mutableListOf<T>()
    private var cache: List<T>? = null

    fun add(item: T) {
        check(cache == null) { "Already frozen." }
        list += item
    }

    fun freezeAndGet(): List<T> {
        val oldCache = cache
        return if (oldCache == null) {
            val newCache = list.toList()
            cache = newCache
            newCache
        } else {
            oldCache
        }
    }
}

operator fun <T> FreezableList<T>.plusAssign(item: T) = this.add(item)


class FreezableRegistry<K, V> {
    private val map = mutableMapOf<K, V>()
    private var cache: Map<K, V>? = null

    fun register(key: K, value: V) {
        check(cache == null) { "Already frozen." }
        check(key !in map) { "Key already exists: $key" }
        map[key] = value
    }

    fun freezeAndGet(): Map<K, V> {
        val oldCache = cache
        return if (oldCache == null) {
            val newCache = map.toMap()
            cache = newCache
            newCache
        } else {
            oldCache
        }
    }
}

operator fun <K, V> FreezableRegistry<K, V>.set(key: K, value: V) = this.register(key, value)
