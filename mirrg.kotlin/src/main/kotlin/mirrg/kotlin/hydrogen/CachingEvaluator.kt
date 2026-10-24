package mirrg.kotlin.hydrogen

// TODO mirrg

class CachingEvaluator<T : Any>(private val initializer: () -> T) {

    private var cache: T? = null
    private var evaluating = false

    val isEvaluated get() = cache != null

    fun getOrNull() = cache

    fun getOrThrow() = cache ?: throw IllegalStateException("not evaluated")

    /** 同じインスタンスの評価中に再び呼び出すと、[kotlin.IllegalStateException] を投げるのだ～🌱 */
    fun evaluate(): T {
        if (evaluating) throw IllegalStateException("Recursive evaluation of CachingEvaluator")
        val old = cache
        return if (old != null) {
            old
        } else {
            evaluating = true
            try {
                val new = initializer()
                cache = new
                new
            } finally {
                evaluating = false
            }
        }
    }

    fun reset() {
        cache = null
    }

}

class SuspendableCachingEvaluator<T : Any>(private val initializer: suspend () -> T) {

    private var cache: T? = null
    private var evaluating = false

    val isEvaluated get() = cache != null

    fun getOrNull() = cache

    fun getOrThrow() = cache ?: throw IllegalStateException("not evaluated")

    /** 同じインスタンスの評価中に再び呼び出すと、[kotlin.IllegalStateException] を投げるのだ～🌱 */
    suspend fun evaluate(): T {
        if (evaluating) throw IllegalStateException("Recursive evaluation of SuspendableCachingEvaluator")
        val old = cache
        return if (old != null) {
            old
        } else {
            evaluating = true
            try {
                val new = initializer()
                cache = new
                new
            } finally {
                evaluating = false
            }
        }
    }

    fun reset() {
        cache = null
    }

}
