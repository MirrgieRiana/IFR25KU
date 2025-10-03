package miragefairy2024

class ModContext

class InitializationEventRegistry<T> {
    private val list = mutableListOf<T>()
    private var frozen = false

    context(ModContext)
    operator fun invoke(listener: T) {
        check(!frozen) { "Cannot register listener to already fired initialization event." }
        list += listener
    }

    fun fire(processor: (T) -> Unit) {
        frozen = true
        list.forEach {
            processor(it)
        }
        list.clear()
    }
}

object Modules {
    private val lock = Any()
    private var initialized = false

    context(ModContext)
    fun init() {
        synchronized(lock) {
            if (initialized) return
            initialized = true
            initModules()
        }
    }
}
