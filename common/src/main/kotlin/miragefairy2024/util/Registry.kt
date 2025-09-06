package miragefairy2024.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import miragefairy2024.ModContext
import mirrg.kotlin.hydrogen.or
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

object RegistryEvents {
    val registrations = mutableListOf<Registration<*, *>>()
}

class Registration<T : Any, U : T>(val registry: Registry<T>, val identifier: ResourceLocation, val creator: suspend () -> U) : () -> U {

    private val value = CompletableDeferred<U>()
    private val holder = CompletableDeferred<Holder<T>>()

    fun complete(value: U, holder: Holder<T>) {
        this.value.complete(value)
        this.holder.complete(holder)
    }

    suspend fun await() = value.await()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke() = value.getCompleted()

    suspend fun awaitHolder() = holder.await()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHolder() = holder.getCompleted()

    val key = registry.key() with identifier

}

context(ModContext)
fun Registration<*, *>.register() {
    RegistryEvents.registrations += this
}


val <T> Registry<T>.sortedEntrySet: List<Map.Entry<ResourceKey<T>, T>> get() = this.entrySet().sortedBy { it.key.location() }

fun <T : Any> Registry<T>.getResourceKeyOrNull(value: T): ResourceKey<T>? = this.getResourceKey(value).orNull

fun <T : Any> Registry<T>.getHolderOrNull(key: ResourceKey<T>): Holder.Reference<T>? = this.getHolder(key).orNull

fun <T : Any> Registry<T>.getHolderOfOrNull(value: T) = this.getResourceKeyOrNull(value).or { return null }.let { this.getHolderOrNull(it) }

fun <T : Any> Registry<T>.isIn(value: T, tag: TagKey<T>) = this.getHolderOfOrNull(value).or { return false } isIn tag


operator fun <T> HolderLookup.Provider.get(registry: ResourceKey<Registry<T>>): HolderLookup.RegistryLookup<T> = this.lookupOrThrow(registry)

operator fun <T> HolderLookup.Provider.get(registry: ResourceKey<Registry<T>>, key: ResourceKey<T>): Holder.Reference<T> = this.lookupOrThrow(registry).getOrThrow(key)


infix fun <T, H : Holder<T>> H.isIn(key: ResourceKey<T>) = this.`is`(key)
infix fun <T, H : Holder<T>> H.isNotIn(key: ResourceKey<T>) = !(this isIn key)

infix fun <T, H : Holder<T>> H.isIn(tag: TagKey<T>) = this.`is`(tag)
infix fun <T, H : Holder<T>> H.isNotIn(tag: TagKey<T>) = !(this isIn tag)
