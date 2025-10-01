package miragefairy2024.util

// TODO mirrg

class FreezableRegistry<T> {
    private val list = mutableListOf<T>()
    private var cache: List<T>? = null

    fun add(item: T) {
        check(cache == null) { "Already frozen." }
        list += item
    }

    fun freeze() {
        take()
    }

    fun take(): List<T> {
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

operator fun <T> FreezableRegistry<T>.plusAssign(item: T) = this.add(item)
