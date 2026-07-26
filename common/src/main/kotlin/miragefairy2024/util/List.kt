package miragefairy2024.util

import net.minecraft.core.NonNullList
import net.minecraft.util.RandomSource

fun <T : Any> List<T>.toNonNullList(): NonNullList<T> = this.toCollection(NonNullList.create<T>())

fun <T> List<T>.randomOrNull(random: RandomSource): T? = if (this.isEmpty()) null else this[random.nextInt(this.size)]
fun <T> List<T>.randomOrThrow(random: RandomSource): T = if (this.isEmpty()) throw NoSuchElementException("List is empty.") else this[random.nextInt(this.size)]
