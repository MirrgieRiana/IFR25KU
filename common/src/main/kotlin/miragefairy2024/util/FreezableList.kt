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
