package miragefairy2024.util

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

val ResourceLocation.string get() = this.toString()
val ResourceLocation.pathString get() = "${this.namespace}.${this.path}"

fun String.toIdentifier(): ResourceLocation = ResourceLocation.parse(this)

operator fun String.times(identifier: ResourceLocation) = ResourceLocation(identifier.namespace, this + identifier.path)
operator fun ResourceLocation.times(string: String) = ResourceLocation(this.namespace, this.path + string)

infix fun <T> ResourceKey<out Registry<T>>.with(value: ResourceLocation): ResourceKey<T> = ResourceKey.create(this, value)

fun ResourceLocation(namespace: String, path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, path)
fun ResourceLocation(path: String): ResourceLocation = ResourceLocation.withDefaultNamespace(path)
