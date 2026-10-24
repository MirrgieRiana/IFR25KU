package mirrg.kotlin.hydrogen

// TODO mirrg

class CachingEvaluator<T : Any>(private val initializer: () -> T) {

    private var cache: T? = null

    fun evaluate(): T {
        val old = cache
        return if (old != null) {
            old
        } else {
            val new = initializer()
            cache = new
            new
        }
    }

    fun reset() {
        cache = null
    }

}
