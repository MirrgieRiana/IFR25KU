package miragefairy2024.mod.magicplant

import mirrg.kotlin.helium.or

// api

class MutableTraitEffects {
    private val map = mutableMapOf<TraitEffectKey<*>, Any>()

    val keys get() = map.keys

    val effects
        get() = map.entries.map { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            fun <T : Any> a(key: TraitEffectKey<T>, value: Any): TraitEffect<*> = TraitEffect(key, value as T)
            a(key, value)
        }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: TraitEffectKey<T>) = map[key].or { return key.getDefaultValue() } as T
    operator fun <T : Any> set(key: TraitEffectKey<T>, value: T?) {
        if (value == null) {
            map.remove(key)
        } else {
            map[key] = value
        }
    }
}


// util

operator fun MutableTraitEffects.plusAssign(other: MutableTraitEffects) {
    other.keys.forEach { key ->
        fun <T : Any> f(key: TraitEffectKey<T>) {
            this[key] = key.plus(this[key], other[key])
        }
        f(key)
    }
}
