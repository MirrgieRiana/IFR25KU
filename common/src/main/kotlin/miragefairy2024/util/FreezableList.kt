package miragefairy2024.util

import mirrg.kotlin.helium.CachingEvaluator

// TODO mirrg

class FreezableList<T> {
    private val list = mutableListOf<T>()
    private val cache = CachingEvaluator { list.toList() }

    fun add(item: T) {
        check(!cache.isEvaluated) { "Already frozen." }
        list += item
    }

    fun freezeAndGet() = cache.evaluate()
}

operator fun <T> FreezableList<T>.plusAssign(item: T) = this.add(item)


class FreezableRegistry<K, V> {
    private val map = mutableMapOf<K, V>()
    private val cache = CachingEvaluator { map.toMap() }

    fun register(key: K, value: V) {
        check(!cache.isEvaluated) { "Already frozen." }
        check(key !in map) { "Key already exists: $key" }
        map[key] = value
    }

    fun freezeAndGet() = cache.evaluate()
}

operator fun <K, V> FreezableRegistry<K, V>.set(key: K, value: V) = this.register(key, value)


class SubscribableBuffer<T> {
    private val list = mutableListOf<T>()
    private val listeners = mutableListOf<(T) -> Unit>()

    fun add(item: T) {
        list += item
        listeners.forEach {
            it(item)
        }
    }

    fun subscribe(listener: (T) -> Unit) {
        listeners += listener
        list.forEach {
            listener(it)
        }
    }

    fun getAllImmediately(): List<T> = list
}

operator fun <T> SubscribableBuffer<T>.plusAssign(item: T) = this.add(item)
