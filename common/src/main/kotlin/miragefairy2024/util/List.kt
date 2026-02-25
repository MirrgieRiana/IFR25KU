package miragefairy2024.util

import net.minecraft.core.NonNullList

fun <T : Any> List<T>.toNonNullList(): NonNullList<T> = this.toCollection(NonNullList.create<T>())
