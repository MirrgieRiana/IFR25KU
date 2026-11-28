package miragefairy2024.util

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier

fun ResourceLocation(namespace: String, path: String): Identifier = Identifier.fromNamespaceAndPath(namespace, path)
fun ResourceLocation(path: String): Identifier = Identifier.withDefaultNamespace(path)
fun String.toIdentifier(): Identifier = Identifier.parse(this)

operator fun String.times(identifier: Identifier) = ResourceLocation(identifier.namespace, this + identifier.path)
operator fun Identifier.times(string: String) = ResourceLocation(this.namespace, this.path + string)

val Identifier.string get() = this.toString()
val Identifier.pathString get() = "${this.namespace}/${this.path}"


infix fun <T> ResourceKey<out Registry<T>>.with(value: Identifier): ResourceKey<T> = ResourceKey.create(this, value)
